/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import java.util.Comparator;

import com.top_logic.basic.col.ComparableComparator;

/**
 * Comparator for form fields, which extracts the values from the given 
 * form fields and hand them over to its inner comparator.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class FormFieldComparator implements Comparator<FormField> {

	/** Default comparator for form fields. */
	public static final FormFieldComparator INSTANCE = new FormFieldComparator();

	/** The comparator to compare the form field values with each other. */
	private Comparator<Object> innerComparator;

	/** 
	 * Creates a {@link FormFieldComparator}.
	 */
	protected FormFieldComparator() {
		this(ComparableComparator.INSTANCE);
	}
	
	/** 
	 * Creates a {@link FormFieldComparator}.
	 * 
	 * @param    aComparator    The inner comparator for the values of the form fields.
	 */
	public FormFieldComparator(Comparator<Object> aComparator) {
		this.innerComparator = aComparator;
	}

	@Override
	public int compare(FormField aField1, FormField aField2) {
		if (aField1 == null) {
			return (aField2 == null) ? 0 : -1;
		}
		else if (aField2 == null) {
			return 1;
		}
		else {
			return this.innerComparator.compare(this.getValue(aField1), this.getValue(aField2));
		}
	}

	/** 
	 * Return the value currently set in the given field.
	 * 
	 * <p>If the given field doesn't have a valid value (e.g. parsing error), this method
	 * will return <code>null</code>.</p>
	 * 
	 * @param    aField    The field to be checked, must not be <code>null</code>.    
	 * @return   The value from the given field, may be <code>null</code>.
	 */
	protected Object getValue(FormField aField) {
		return aField.hasValue() ? aField.getValue() : null;
	}
}

