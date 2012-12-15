/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class PhotoPageProgressBar {
	private final ViewGroup mContainer;
	private final View mProgress;

	public PhotoPageProgressBar(final Context context, final RelativeLayout parentLayout) {
		final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// mContainer = (ViewGroup)
		// inflater.inflate(R.layout.photopage_progress_bar, parentLayout,
		// false);
		mContainer = new FrameLayout(context);
		mProgress = new ProgressBar(context);
		mContainer.addView(mProgress);
		parentLayout.addView(mContainer);
		// mProgress =
		// mContainer.findViewById(R.id.photopage_progress_foreground);
	}

	public void hideProgress() {
		mContainer.setVisibility(View.INVISIBLE);
	}

	public void setProgress(final int progressPercent) {
		mContainer.setVisibility(View.VISIBLE);
		final LayoutParams layoutParams = mProgress.getLayoutParams();
		layoutParams.width = mContainer.getWidth() * progressPercent / 100;
		mProgress.setLayoutParams(layoutParams);
	}
}
