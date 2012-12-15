/*
 * Copyright (C) 2011 The Android Open Source Project
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

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.Context;
import android.view.Menu;

public class GalleryActionBar implements OnNavigationListener {

	private final Context mContext;
	private final ActionBar mActionBar;

	private Menu mActionBarMenu;

	public GalleryActionBar(final AbstractGalleryActivity activity) {
		mActionBar = activity.getActionBar();
		mContext = activity.getAndroidContext();
	}

	public int getHeight() {
		return mActionBar != null ? mActionBar.getHeight() : 0;
	}

	public Menu getMenu() {
		return mActionBarMenu;
	}

	@Override
	public boolean onNavigationItemSelected(final int itemPosition, final long itemId) {
		return false;
	}

	public void setDisplayOptions(final boolean displayHomeAsUp, final boolean showTitle) {
		if (mActionBar == null) return;
		int options = 0;
		if (displayHomeAsUp) {
			options |= ActionBar.DISPLAY_HOME_AS_UP;
		}
		if (showTitle) {
			options |= ActionBar.DISPLAY_SHOW_TITLE;
		}

		mActionBar.setDisplayOptions(options, ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
		mActionBar.setHomeButtonEnabled(displayHomeAsUp);
	}

	public void setSubtitle(final String title) {
		if (mActionBar != null) {
			mActionBar.setSubtitle(title);
		}
	}

	public void setTitle(final int titleId) {
		if (mActionBar != null) {
			mActionBar.setTitle(mContext.getString(titleId));
		}
	}

	public void setTitle(final String title) {
		if (mActionBar != null) {
			mActionBar.setTitle(title);
		}
	}

}
