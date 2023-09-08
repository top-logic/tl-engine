/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.FormComponent;

/**
 * Convenience super-class for pseudo {@link FormField} implementations.
 * 
 * <p>
 * A constant field does not contain a value that is directly editable by the
 * user. A {@link ConstantField} implementation is used to add typed data to a
 * {@link FormContext} for displaying it with a specialized {@link Control}
 * within a {@link FormComponent}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ConstantField extends AbstractFormField {

	/**
	 * Creates a new {@link ConstantField}. It has no {@link Constraint}, is not mandatory, and
	 * {@link #getNormalizeInput() normalizes} the input.
	 * 
	 * @param name
	 *        see {@link #getName()}
	 * @param immutable
	 *        see {@link #isImmutable()}
	 * 
	 * @see AbstractFormField#AbstractFormField(String, boolean, boolean, boolean, Constraint)
	 */
	protected ConstantField(String name, boolean immutable) {
		super(name, !MANDATORY, immutable, NORMALIZE, NO_CONSTRAINT);
	}

	@Override
	protected final Object narrowValue(Object aValue) throws IllegalArgumentException, ClassCastException {
		return aValue;
	}

	@Override
	protected final Object parseRawValue(Object aRawValue) throws CheckException {
		// This field may never receive input directly from the client. The only
		// way to modify its value by modifying its list and selection models.
		// Those changes are reflected at the GUI automatically, if this field
		// is displayed by a list control.
		return aRawValue;
	}

	@Override
	protected final Object unparseValue(Object aValue) {
		// The form field life cycle requests to provide a client-side view of
		// any server-side values set into a field, even if those values are never
		// communicated to the client. Therefore, this field must produce a
		// client-side a legal client-side representation of set values.
		return aValue;
	}

}
