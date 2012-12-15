package org.mariotaku.gallery3d.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import org.mariotaku.gallery3d.common.ApiHelper;
import org.mariotaku.gallery3d.common.Utils;
import org.mariotaku.gallery3d.util.ThreadPool.JobContext;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video;
import android.util.Log;

class BucketHelper {

	private static final String TAG = "BucketHelper";
	private static final String EXTERNAL_MEDIA = "external";

	// BUCKET_DISPLAY_NAME is a string like "Camera" which is the directory
	// name of where an image or video is in. BUCKET_ID is a hash of the path
	// name of that directory (see computeBucketValues() in MediaProvider for
	// details). MEDIA_TYPE is video, image, audio, etc.
	//
	// The "albums" are not explicitly recorded in the database, but each image
	// or video has the two columns (BUCKET_ID, MEDIA_TYPE). We define an
	// "album" to be the collection of images/videos which have the same value
	// for the two columns.
	//
	// The goal of the query (used in loadSubMediaSetsFromFilesTable()) is to
	// find all albums, that is, all unique values for (BUCKET_ID, MEDIA_TYPE).
	// In the meantime sort them by the timestamp of the latest image/video in
	// each of the album.
	//
	// The order of columns below is important: it must match to the index in
	// MediaStore.
	private static final String[] PROJECTION_BUCKET = { ImageColumns.BUCKET_ID, FileColumns.MEDIA_TYPE,
			ImageColumns.BUCKET_DISPLAY_NAME };

	// The indices should match the above projections.
	private static final int INDEX_BUCKET_ID = 0;
	private static final int INDEX_MEDIA_TYPE = 1;
	private static final int INDEX_BUCKET_NAME = 2;

	// We want to order the albums by reverse chronological order. We abuse the
	// "WHERE" parameter to insert a "GROUP BY" clause into the SQL statement.
	// The template for "WHERE" parameter is like:
	// SELECT ... FROM ... WHERE (%s)
	// and we make it look like:
	// SELECT ... FROM ... WHERE (1) GROUP BY 1,(2)
	// The "(1)" means true. The "1,(2)" means the first two columns specified
	// after SELECT. Note that because there is a ")" in the template, we use
	// "(2" to match it.
	private static final String BUCKET_GROUP_BY = "1) GROUP BY 1,(2";

	private static final String BUCKET_ORDER_BY = "MAX(datetaken) DESC";

	// Before HoneyComb there is no Files table. Thus, we need to query the
	// bucket info from the Images and Video tables and then merge them
	// together.
	//
	// A bucket can exist in both tables. In this case, we need to find the
	// latest timestamp from the two tables and sort ourselves. So we add the
	// MAX(date_taken) to the projection and remove the media_type since we
	// already know the media type from the table we query from.
	private static final String[] PROJECTION_BUCKET_IN_ONE_TABLE = { ImageColumns.BUCKET_ID, "MAX(datetaken)",
			ImageColumns.BUCKET_DISPLAY_NAME };

	// We keep the INDEX_BUCKET_ID and INDEX_BUCKET_NAME the same as
	// PROJECTION_BUCKET so we can reuse the values defined before.
	private static final int INDEX_DATE_TAKEN = 1;

	// When query from the Images or Video tables, we only need to group by
	// BUCKET_ID.
	private static final String BUCKET_GROUP_BY_IN_ONE_TABLE = "1) GROUP BY (1";

	public static String getBucketName(final ContentResolver resolver, final int bucketId) {
		if (ApiHelper.HAS_MEDIA_PROVIDER_FILES_TABLE) {
			final String result = getBucketNameInTable(resolver, getFilesContentUri(), bucketId);
			return result == null ? "" : result;
		} else {
			String result = getBucketNameInTable(resolver, Images.Media.EXTERNAL_CONTENT_URI, bucketId);
			if (result != null) return result;
			result = getBucketNameInTable(resolver, Video.Media.EXTERNAL_CONTENT_URI, bucketId);
			return result == null ? "" : result;
		}
	}

	public static BucketEntry[] loadBucketEntries(final JobContext jc, final ContentResolver resolver, final int type) {
		if (ApiHelper.HAS_MEDIA_PROVIDER_FILES_TABLE)
			return loadBucketEntriesFromFilesTable(jc, resolver, type);
		else
			return loadBucketEntriesFromImagesAndVideoTable(jc, resolver, type);
	}

	private static String getBucketNameInTable(final ContentResolver resolver, final Uri tableUri, final int bucketId) {
		final String selectionArgs[] = new String[] { String.valueOf(bucketId) };
		final Uri uri = tableUri.buildUpon().appendQueryParameter("limit", "1").build();
		final Cursor cursor = resolver.query(uri, PROJECTION_BUCKET_IN_ONE_TABLE, "bucket_id = ?", selectionArgs, null);
		try {
			if (cursor != null && cursor.moveToNext()) return cursor.getString(INDEX_BUCKET_NAME);
		} finally {
			Utils.closeSilently(cursor);
		}
		return null;
	}

	@TargetApi(ApiHelper.VERSION_CODES.HONEYCOMB)
	private static Uri getFilesContentUri() {
		return Files.getContentUri(EXTERNAL_MEDIA);
	}

	private static BucketEntry[] loadBucketEntriesFromFilesTable(final JobContext jc, final ContentResolver resolver,
			final int type) {
		final Uri uri = getFilesContentUri();

		final Cursor cursor = resolver.query(uri, PROJECTION_BUCKET, BUCKET_GROUP_BY, null, BUCKET_ORDER_BY);
		if (cursor == null) {
			Log.w(TAG, "cannot open local database: " + uri);
			return new BucketEntry[0];
		}
		final ArrayList<BucketEntry> buffer = new ArrayList<BucketEntry>();
		int typeBits = 0;
		if ((type & MediaObject.MEDIA_TYPE_IMAGE) != 0) {
			typeBits |= 1 << FileColumns.MEDIA_TYPE_IMAGE;
		}
		if ((type & MediaObject.MEDIA_TYPE_VIDEO) != 0) {
			typeBits |= 1 << FileColumns.MEDIA_TYPE_VIDEO;
		}
		try {
			while (cursor.moveToNext()) {
				if ((typeBits & 1 << cursor.getInt(INDEX_MEDIA_TYPE)) != 0) {
					final BucketEntry entry = new BucketEntry(cursor.getInt(INDEX_BUCKET_ID),
							cursor.getString(INDEX_BUCKET_NAME));
					if (!buffer.contains(entry)) {
						buffer.add(entry);
					}
				}
				if (jc.isCancelled()) return null;
			}
		} finally {
			Utils.closeSilently(cursor);
		}
		return buffer.toArray(new BucketEntry[buffer.size()]);
	}

	private static BucketEntry[] loadBucketEntriesFromImagesAndVideoTable(final JobContext jc,
			final ContentResolver resolver, final int type) {
		final HashMap<Integer, BucketEntry> buckets = new HashMap<Integer, BucketEntry>(64);
		if ((type & MediaObject.MEDIA_TYPE_IMAGE) != 0) {
			updateBucketEntriesFromTable(jc, resolver, Images.Media.EXTERNAL_CONTENT_URI, buckets);
		}
		if ((type & MediaObject.MEDIA_TYPE_VIDEO) != 0) {
			updateBucketEntriesFromTable(jc, resolver, Video.Media.EXTERNAL_CONTENT_URI, buckets);
		}
		final BucketEntry[] entries = buckets.values().toArray(new BucketEntry[buckets.size()]);
		Arrays.sort(entries, new Comparator<BucketEntry>() {
			@Override
			public int compare(final BucketEntry a, final BucketEntry b) {
				// sorted by dateTaken in descending order
				return b.dateTaken - a.dateTaken;
			}
		});
		return entries;
	}

	private static void updateBucketEntriesFromTable(final JobContext jc, final ContentResolver resolver,
			final Uri tableUri, final HashMap<Integer, BucketEntry> buckets) {
		final Cursor cursor = resolver.query(tableUri, PROJECTION_BUCKET_IN_ONE_TABLE, BUCKET_GROUP_BY_IN_ONE_TABLE,
				null, null);
		if (cursor == null) {
			Log.w(TAG, "cannot open media database: " + tableUri);
			return;
		}
		try {
			while (cursor.moveToNext()) {
				final int bucketId = cursor.getInt(INDEX_BUCKET_ID);
				final int dateTaken = cursor.getInt(INDEX_DATE_TAKEN);
				BucketEntry entry = buckets.get(bucketId);
				if (entry == null) {
					entry = new BucketEntry(bucketId, cursor.getString(INDEX_BUCKET_NAME));
					buckets.put(bucketId, entry);
					entry.dateTaken = dateTaken;
				} else {
					entry.dateTaken = Math.max(entry.dateTaken, dateTaken);
				}
			}
		} finally {
			Utils.closeSilently(cursor);
		}
	}

	public static class BucketEntry {
		public String bucketName;
		public int bucketId;
		public int dateTaken;

		public BucketEntry(final int id, final String name) {
			bucketId = id;
			bucketName = Utils.ensureNotNull(name);
		}

		@Override
		public boolean equals(final Object object) {
			if (!(object instanceof BucketEntry)) return false;
			final BucketEntry entry = (BucketEntry) object;
			return bucketId == entry.bucketId;
		}

		@Override
		public int hashCode() {
			return bucketId;
		}
	}
}
