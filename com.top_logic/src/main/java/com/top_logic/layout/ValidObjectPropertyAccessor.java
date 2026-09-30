/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout;

import com.top_logic.layout.component.ComponentUtil;

/**
 * {@link ReadOnlyPropertyAccessor} checking for valid objects.
 * 
 * <p>
 * If the value for an {@link ComponentUtil#isValid(Object) invalid} object is requested,
 * <code>null</code> is returned.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ValidObjectPropertyAccessor<T> extends ReadOnlyPropertyAccessor<T> {

	@Override
	public Object getValue(T target) {
		if (!ComponentUtil.isValid(target)) {
			return null;
		}
		return getValidValue(target);
	}

	/**
	 * Determines the value for {@link #getValue(Object)} for an
	 * {@link ComponentUtil#isValid(Object) valid} object.
	 * 
	 * @param target
	 *        Argument from {@link #getValue(Object)}.
	 * @return Return value for {@link #getValue(Object)}.
	 */
	protected abstract Object getValidValue(T target);

}

