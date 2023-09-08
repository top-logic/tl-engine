/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.layout.form.FormField;

/**
 * Common interface for all {@link FormField}s that accept a single character sequence as input from
 * the GUI.
 * 
 * @deprecated Just rely on the {@link FormField} API.
 */
@Deprecated
public interface SingleValueField extends FormField {

	/**
	 * Convenience short-cut for
	 * {@link com.top_logic.layout.form.FormField#setValue(Object) setting the fields value}
	 * from the values string representation.
	 * 
	 * @param aValue
	 *     The string represenation of the new value of this field. This
	 *     value is parsed by the field, before it is passed to
	 *     {@link com.top_logic.layout.form.FormField#setValue(Object)}.
	 * 
	 * @see com.top_logic.layout.form.FormField#setValue(Object)
	 */
	public void setAsString(String aValue);

	/**
	 * Convenience short-cut for accessing the string representation of the
	 * {@link com.top_logic.layout.form.FormField#getValue() the field's value}.
	 * 
	 * @return A string representation of the
	 *     {@link com.top_logic.layout.form.FormField#getValue() field's value}.
	 * 
	 * @see com.top_logic.layout.form.FormField#getValue()
	 */
	public String getAsString();
	
	/**
	 * Convenience short-cut for
	 * {@link com.top_logic.layout.form.FormField#getRawValue()} (for fields
	 * that only have at most a single raw value).
	 * 
	 * @see com.top_logic.layout.form.FormField#getValue()
	 */
	public String getRawString();
}
