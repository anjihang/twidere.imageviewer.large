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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

public class MediaDetails implements Iterable<Entry<Integer, Object>> {

	private final TreeMap<Integer, Object> mDetails = new TreeMap<Integer, Object>();
	private final HashMap<Integer, Integer> mUnits = new HashMap<Integer, Integer>();

	public static final int INDEX_TITLE = 1;
	public static final int INDEX_DESCRIPTION = 2;
	// /public static final int INDEX_DATETIME = 3;
	// public static final int INDEX_LOCATION = 4;
	public static final int INDEX_WIDTH = 5;
	public static final int INDEX_HEIGHT = 6;
	public static final int INDEX_ORIENTATION = 7;
	// public static final int INDEX_DURATION = 8;
	public static final int INDEX_MIMETYPE = 9;
	// public static final int INDEX_SIZE = 10;

	// Put this last because it may be long.
	public static final int INDEX_PATH = 200;

	public void addDetail(final int index, final Object value) {
		mDetails.put(index, value);
	}

	public Object getDetail(final int index) {
		return mDetails.get(index);
	}

	public int getUnit(final int index) {
		return mUnits.get(index);
	}

	public boolean hasUnit(final int index) {
		return mUnits.containsKey(index);
	}

	@Override
	public Iterator<Entry<Integer, Object>> iterator() {
		return mDetails.entrySet().iterator();
	}

	public void setUnit(final int index, final int unit) {
		mUnits.put(index, unit);
	}

	public int size() {
		return mDetails.size();
	}

}
