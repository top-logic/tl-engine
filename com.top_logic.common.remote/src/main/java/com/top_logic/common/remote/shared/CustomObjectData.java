/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.shared;

/**
 * {@link ObjectData} for a generalized {@link SharedObject}.
 * 
 * <p>
 * A generalized {@link SharedObject} does not implement the interface {@link SharedObject}.
 * </p>
 * 
 * @see ObjectScope#toData(Object)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CustomObjectData extends SharedObjectData {

	private final Object _handle;

	private final boolean _isHandleSelf;

	/**
	 * Convenience variant of {@link #CustomObjectData(ObjectScope, Object, boolean)} with the last
	 * parameter set to false. That means that "this" is returned in {@link #self()}.
	 */
	public CustomObjectData(ObjectScope scope, Object handle) {
		this(scope, handle, false);
	}

	/**
	 * Creates a {@link CustomObjectData}.
	 *
	 * @param scope
	 *        The {@link ObjectScope} to register this new object in.
	 * @param handle
	 *        The application object for which this container manages instance data.
	 * @param isHandleSelf
	 *        Whether {@link #self()} should return the handle. If not, it returns "this".
	 */
	public CustomObjectData(ObjectScope scope, Object handle, boolean isHandleSelf) {
		super(scope);
		_handle = handle;
		_isHandleSelf = isHandleSelf;
	}

	@Override
	public Object handle() {
		return _handle;
	}

	@Override
	protected Object self() {
		if (_isHandleSelf) {
			return handle();
		}
		return this;
	}

	@Override
	protected void onDelete() {
		// Nothing to do.
	}

}
