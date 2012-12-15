/*
 * Copyright (C) 2010 The Android Open Source Project
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

package org.mariotaku.gallery3d.ui;

import java.util.ArrayList;

import org.mariotaku.gallery3d.common.BitmapUtils;
import org.mariotaku.gallery3d.data.BitmapPool;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;

public class BitmapTileProvider implements TileImageView.Model {
	private final ScreenNail mScreenNail;
	private final Bitmap[] mMipmaps;
	private final Config mConfig;
	private final int mImageWidth;
	private final int mImageHeight;

	private boolean mRecycled = false;

	public BitmapTileProvider(Bitmap bitmap, final int maxBackupSize) {
		mImageWidth = bitmap.getWidth();
		mImageHeight = bitmap.getHeight();
		final ArrayList<Bitmap> list = new ArrayList<Bitmap>();
		list.add(bitmap);
		while (bitmap.getWidth() > maxBackupSize || bitmap.getHeight() > maxBackupSize) {
			bitmap = BitmapUtils.resizeBitmapByScale(bitmap, 0.5f, false);
			list.add(bitmap);
		}

		mScreenNail = new BitmapScreenNail(list.remove(list.size() - 1));
		mMipmaps = list.toArray(new Bitmap[list.size()]);
		mConfig = Config.ARGB_8888;
	}

	@Override
	public int getImageHeight() {
		return mImageHeight;
	}

	@Override
	public int getImageWidth() {
		return mImageWidth;
	}

	@Override
	public int getLevelCount() {
		return mMipmaps.length;
	}

	@Override
	public ScreenNail getScreenNail() {
		return mScreenNail;
	}

	@Override
	public Bitmap getTile(final int level, int x, int y, final int tileSize, final int borderSize, final BitmapPool pool) {
		x >>= level;
		y >>= level;
		final int size = tileSize + 2 * borderSize;

		Bitmap result = pool == null ? null : pool.getBitmap();
		if (result == null) {
			result = Bitmap.createBitmap(size, size, mConfig);
		} else {
			result.eraseColor(0);
		}

		final Bitmap mipmap = mMipmaps[level];
		final Canvas canvas = new Canvas(result);
		final int offsetX = -x + borderSize;
		final int offsetY = -y + borderSize;
		canvas.drawBitmap(mipmap, offsetX, offsetY, null);
		return result;
	}

	public void recycle() {
		if (mRecycled) return;
		mRecycled = true;
		for (final Bitmap bitmap : mMipmaps) {
			BitmapUtils.recycleSilently(bitmap);
		}
		if (mScreenNail != null) {
			mScreenNail.recycle();
		}
	}
}
