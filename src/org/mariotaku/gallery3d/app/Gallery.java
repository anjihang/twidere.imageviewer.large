/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.gallery3d.app;

import org.mariotaku.gallery3d.R;
import org.mariotaku.gallery3d.common.Utils;
import org.mariotaku.gallery3d.data.DataManager;
import org.mariotaku.gallery3d.data.MediaItem;
import org.mariotaku.gallery3d.data.Path;
import org.mariotaku.gallery3d.util.GalleryUtils;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public final class Gallery extends AbstractGalleryActivity implements OnCancelListener {
	public static final String EXTRA_SLIDESHOW = "slideshow";
	public static final String EXTRA_DREAM = "dream";
	public static final String EXTRA_CROP = "crop";

	public static final String ACTION_REVIEW = "com.android.camera.action.REVIEW";
	public static final String KEY_GET_CONTENT = "get-content";
	public static final String KEY_GET_ALBUM = "get-album";
	public static final String KEY_TYPE_BITS = "type-bits";
	public static final String KEY_MEDIA_TYPES = "mediaTypes";
	public static final String KEY_DISMISS_KEYGUARD = "dismiss-keyguard";

	private static final String TAG = "Gallery";
	private Dialog mVersionCheckDialog;

	@Override
	public void onCancel(final DialogInterface dialog) {
		if (dialog == mVersionCheckDialog) {
			mVersionCheckDialog = null;
		}
	}

	public void startDefaultPage() {

	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		if (getIntent().getBooleanExtra(KEY_DISMISS_KEYGUARD, false)) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		}

		setContentView(R.layout.main);

		if (savedInstanceState != null) {
			getStateManager().restoreFromState(savedInstanceState);
		} else {
			initializeByIntent();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mVersionCheckDialog != null) {
			mVersionCheckDialog.dismiss();
		}
	}

	@Override
	protected void onResume() {
		Utils.assertTrue(getStateManager().getStateCount() > 0);
		super.onResume();
		if (mVersionCheckDialog != null) {
			mVersionCheckDialog.show();
		}
	}

	private String getContentType(final Intent intent) {
		final String type = intent.getType();
		if (type != null) return GalleryUtils.MIME_TYPE_PANORAMA360.equals(type) ? MediaItem.MIME_TYPE_JPEG : type;

		final Uri uri = intent.getData();
		try {
			return getContentResolver().getType(uri);
		} catch (final Throwable t) {
			Log.w(TAG, "get type fail", t);
			return null;
		}
	}

	private void initializeByIntent() {
		final Intent intent = getIntent();
		final String action = intent.getAction();

		if (Intent.ACTION_GET_CONTENT.equalsIgnoreCase(action)) {
			startGetContent(intent);
		} else if (Intent.ACTION_PICK.equalsIgnoreCase(action)) {
			// We do NOT really support the PICK intent. Handle it as
			// the GET_CONTENT. However, we need to translate the type
			// in the intent here.
			Log.w(TAG, "action PICK is not supported");
			final String type = Utils.ensureNotNull(intent.getType());
			if (type.startsWith("vnd.android.cursor.dir/")) {
				if (type.endsWith("/image")) {
					intent.setType("image/*");
				}
				if (type.endsWith("/video")) {
					intent.setType("video/*");
				}
			}
			startGetContent(intent);
		} else if (Intent.ACTION_VIEW.equalsIgnoreCase(action) || ACTION_REVIEW.equalsIgnoreCase(action)) {
			startViewAction(intent);
		} else {
			startDefaultPage();
		}
	}

	private void startGetContent(final Intent intent) {
	}

	private void startViewAction(final Intent intent) {
		final Bundle data = new Bundle();
		final DataManager dm = getDataManager();
		final Uri uri = intent.getData();
		final String contentType = getContentType(intent);
		if (contentType == null) {
			Toast.makeText(this, R.string.no_such_item, Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		if (uri == null) {
			finish();
		} else {
			final Path itemPath = dm.findPathByUri(uri, contentType);
			final Path albumPath = dm.getDefaultSetOf(itemPath);

			data.putString(PhotoPage.KEY_MEDIA_ITEM_PATH, itemPath.toString());

			// TODO: Make the parameter "SingleItemOnly" public so other
			// activities can reference it.
			final boolean singleItemOnly = albumPath == null || intent.getBooleanExtra("SingleItemOnly", false);
			if (!singleItemOnly) {
				data.putString(PhotoPage.KEY_MEDIA_SET_PATH, albumPath.toString());
				// when FLAG_ACTIVITY_NEW_TASK is set, (e.g. when intent is
				// fired
				// from notification), back button should behave the same as up
				// button
				// rather than taking users back to the home screen
				if (intent.getBooleanExtra(PhotoPage.KEY_TREAT_BACK_AS_UP, false)
						|| (intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) != 0) {
					data.putBoolean(PhotoPage.KEY_TREAT_BACK_AS_UP, true);
				}
			}

			getStateManager().startState(PhotoPage.class, data);
		}
	}
}
