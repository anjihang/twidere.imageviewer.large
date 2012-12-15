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

package org.mariotaku.gallery3d.data;

public final class DataSourceType {
	public static final int TYPE_NOT_CATEGORIZED = 0;
	public static final int TYPE_LOCAL = 1;
	public static final int TYPE_PICASA = 2;
	public static final int TYPE_MTP = 3;
	public static final int TYPE_CAMERA = 4;

	private static final Path LOCAL_ROOT = Path.fromString("/local");

}
