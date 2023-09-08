/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.shared;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link DataMapping} that can store {@link ObjectData} containers independently from generalized
 * shared objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CustomDataMapping extends AbstractDataMapping {

	private final Map<Object, ObjectData> _dataForHandle = new HashMap<>();

	@Override
	protected ObjectData nonSharedObjectData(ObjectScope scope, Object obj) {
		ObjectData data = _dataForHandle.get(obj);
		if (data == null) {
			ObjectData newData = createData(scope, obj);
			if (newData != null) {
				register(newData);
			}
			return newData;
		}
		return data;
	}

	/**
	 * Hook for actually creating an {@link ObjectData} container for a shared object that not yet
	 * has such adapter assigned.
	 * 
	 * @param scope
	 *        See {@link #data(ObjectScope, Object)}.
	 * @param obj
	 *        See {@link #data(ObjectScope, Object)}
	 * @return The newly created {@link ObjectData} for the given shared object, or
	 *         <code>null</code> if the given object is not a generalized shared object.
	 */
	protected ObjectData createData(ObjectScope scope, Object obj) {
		if (obj instanceof Enum<?>) {
			return new ConstantData(scope, obj);
		}
		return null;
	}

	private void register(ObjectData data) {
		Object handle = data.handle();
		ObjectData clash = _dataForHandle.put(handle, data);
		if (clash != null) {
			_dataForHandle.put(handle, clash);
			throw new IllegalArgumentException("The object was already registered: " + handle);
		}
	}

}
