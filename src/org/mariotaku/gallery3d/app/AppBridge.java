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

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

// This is the bridge to connect a PhotoPage to the external environment.
public abstract class AppBridge implements Parcelable {

	@Override
	public int describeContents() {
		return 0;
	}

	// ////////////////////////////////////////////////////////////////////////
	// These are requests sent from PhotoPage to the app
	// ////////////////////////////////////////////////////////////////////////

	public abstract void detachScreenNail();

	// This is used to notify that the screen nail will be drawn in full screen
	// or not in next draw() call.
	public abstract void onFullScreenChanged(boolean full);

	// Return true if the tap is consumed.
	public abstract boolean onSingleTapUp(int x, int y);

	// If server is null, the services are not available.
	public abstract void setServer(Server server);

	// ////////////////////////////////////////////////////////////////////////
	// These are requests send from app to PhotoPage
	// ////////////////////////////////////////////////////////////////////////

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
	}

	public interface Server {

		// Set the camera frame relative to GLRootView.
		public void setCameraRelativeFrame(Rect frame);

		// Enable or disable the swiping gestures (the default is enabled).
		public void setSwipingEnabled(boolean enabled);

		// Switch to the previous or next picture using the capture animation.
		// The offset is -1 to switch to the previous picture, 1 to switch to
		// the next picture.
		public boolean switchWithCaptureAnimation(int offset);
	}
}
