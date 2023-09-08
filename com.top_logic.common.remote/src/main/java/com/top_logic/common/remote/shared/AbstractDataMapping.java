/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.shared;

/**
 * Base class for {@link DataMapping}s handling the common case of {@link SharedObject} instances.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractDataMapping implements DataMapping {

	@Override
	public ObjectData data(ObjectScope scope, Object obj) {
		if (obj instanceof SharedObject) {
			return ((SharedObject) obj).data();
		} else {
			return nonSharedObjectData(scope, obj);
		}
	}

	/**
	 * Handle the case that the given value is not a {@link SharedObject}.
	 * 
	 * @param scope
	 *        See {@link #data(ObjectScope, Object)}.
	 * @param obj
	 *        Potentially a generalized shared object.
	 * @return A new {@link ObjectData}, if the given object is a generalized shared object, or
	 *         <code>null</code> if the given value is a primitive value.
	 */
	protected abstract ObjectData nonSharedObjectData(ObjectScope scope, Object obj);

}
