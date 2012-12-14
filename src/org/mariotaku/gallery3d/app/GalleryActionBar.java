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

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.OnMenuVisibilityListener;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import org.mariotaku.gallery3d.R;
import org.mariotaku.gallery3d.common.ApiHelper;

import java.util.ArrayList;

public class GalleryActionBar implements OnNavigationListener {

    private Context mContext;
    private LayoutInflater mInflater;
    private AbstractGalleryActivity mActivity;
    private ActionBar mActionBar;
    private int mCurrentIndex;

    private static class ActionItem {
        public int action;
        public boolean enabled;
        public boolean visible;
        public int spinnerTitle;
        public int dialogTitle;
        public int clusterBy;

        public ActionItem(int action, boolean applied, boolean enabled, int title,
                int clusterBy) {
            this(action, applied, enabled, title, title, clusterBy);
        }

        public ActionItem(int action, boolean applied, boolean enabled, int spinnerTitle,
                int dialogTitle, int clusterBy) {
            this.action = action;
            this.enabled = enabled;
            this.spinnerTitle = spinnerTitle;
            this.dialogTitle = dialogTitle;
            this.clusterBy = clusterBy;
            this.visible = true;
        }
    }

    public GalleryActionBar(AbstractGalleryActivity activity) {
        mActionBar = activity.getActionBar();
        mContext = activity.getAndroidContext();
        mActivity = activity;
        mInflater = ((Activity) mActivity).getLayoutInflater();
        mCurrentIndex = 0;
    }

    public int getHeight() {
        return mActionBar != null ? mActionBar.getHeight() : 0;
    }
	
    public void setDisplayOptions(boolean displayHomeAsUp, boolean showTitle) {
        if (mActionBar == null) return;
        int options = 0;
        if (displayHomeAsUp) options |= ActionBar.DISPLAY_HOME_AS_UP;
        if (showTitle) options |= ActionBar.DISPLAY_SHOW_TITLE;

        mActionBar.setDisplayOptions(options,
                ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setHomeButtonEnabled(displayHomeAsUp);
    }

    public void setTitle(String title) {
        if (mActionBar != null) mActionBar.setTitle(title);
    }

    public void setTitle(int titleId) {
        if (mActionBar != null) {
            mActionBar.setTitle(mContext.getString(titleId));
        }
    }

    public void setSubtitle(String title) {
        if (mActionBar != null) mActionBar.setSubtitle(title);
    }

    public void show() {
        if (mActionBar != null) mActionBar.show();
    }

    public void hide() {
        if (mActionBar != null) mActionBar.hide();
    }

    public void addOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
        if (mActionBar != null) mActionBar.addOnMenuVisibilityListener(listener);
    }

    public void removeOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
        if (mActionBar != null) mActionBar.removeOnMenuVisibilityListener(listener);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        return false;
    }

    private Menu mActionBarMenu;
    private ShareActionProvider mSharePanoramaActionProvider;
    private ShareActionProvider mShareActionProvider;
    private Intent mSharePanoramaIntent;
    private Intent mShareIntent;

    public void createActionBarMenu(int menuRes, Menu menu) {
        mActivity.getMenuInflater().inflate(menuRes, menu);
        mActionBarMenu = menu;
    }

    public Menu getMenu() {
        return mActionBarMenu;
    }

    public void setShareIntents(Intent sharePanoramaIntent, Intent shareIntent) {
        mSharePanoramaIntent = sharePanoramaIntent;
        if (mSharePanoramaActionProvider != null) {
            mSharePanoramaActionProvider.setShareIntent(sharePanoramaIntent);
        }
        mShareIntent = shareIntent;
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }
}
