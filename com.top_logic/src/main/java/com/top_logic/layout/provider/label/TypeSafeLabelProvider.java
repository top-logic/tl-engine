/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.label;

import com.top_logic.layout.LabelProvider;

/**
 * {@link LabelProvider} returning <code>null</code> for all objects that have the wrong Java type.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TypeSafeLabelProvider<T> implements LabelProvider {

	/**
	 * @see com.top_logic.layout.LabelProvider#getLabel(java.lang.Object)
	 */
	@Override
	public String getLabel(Object object) {
		Class<T> type = getObjectType();
		if (type.isInstance(object)) {
			@SuppressWarnings("unchecked")
			T casted = (T) object;
			return getNonNullLabel(casted);
		}
		return null;
	}

	/**
	 * Java type of the objects this {@link LabelProvider} can handle.
	 */
	protected abstract Class<T> getObjectType();

	/**
	 * Returns the label for given non-null object-
	 * 
	 * @param object
	 *        Object to get label for.
	 * @return The label for the given object.
	 */
	protected abstract String getNonNullLabel(T object);

}

