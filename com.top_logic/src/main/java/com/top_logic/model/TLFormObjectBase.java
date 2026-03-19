/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import com.top_logic.layout.form.FormMember;
import com.top_logic.model.form.ConstraintValidationListener;
import com.top_logic.model.form.ValidationResult;

/**
 * Transient {@link TLObject} that represents an object being edited or created in a form.
 */
public interface TLFormObjectBase extends TLObject {

	/**
	 * Whether this is a create overlay for a new object.
	 */
	boolean isCreate();

	/**
	 * The underlying base object, if this is an edit overlay. <code>null</code> if this is
	 * {@link #isCreate() create} overlay.
	 * 
	 * @see #isCreate()
	 */
	TLObject getEditedObject();

	/**
	 * The name of the created object, if this is a {@link #isCreate() create overlay}.
	 * 
	 * <p>
	 * Legal values are <code>null</code> for the default/initial object creation, or any other
	 * string except the string value <code>"null"</code>.
	 * </p>
	 * 
	 * @see #isCreate()
	 */
	String getDomain();

	/**
	 * The value entered in the form field displayed for the given attribute.
	 *
	 * @param attribute
	 *        The attribute to access.
	 * @return The value directly displayed in the form for the given attribute. Note: In case of
	 *         attributes with fallback values, this may differ from the logical value of the given
	 *         attribute.
	 */
	Object getFieldValue(TLStructuredTypePart attribute);

	/**
	 * The field representing the given attribute in the form.
	 * 
	 * @return The form representation of the given attribute or <code>null</code>, if the given
	 *         attribute is not part of the current form.
	 */
	FormMember getField(TLStructuredTypePart attribute);

	/**
	 * The value of the given attribute before the user started editing this object.
	 *
	 * @param attribute
	 *        The attribute to access.
	 * @return The value of the given attribute before editing has started.
	 */
	Object getBaseValue(TLStructuredTypePart attribute);

	/**
	 * Computes the value to use, if the form has no field for the given
	 * {@link TLStructuredTypePart} of this {@link TLObject}.
	 */
	Object defaultValue(TLStructuredTypePart part);

	/**
	 * The validation result for the given attribute.
	 *
	 * @param attribute
	 *        The attribute to get validation for.
	 * @return The validation result, or {@link ValidationResult#VALID} if no validation has been
	 *         set.
	 */
	default ValidationResult getValidation(TLStructuredTypePart attribute) {
		return ValidationResult.VALID;
	}

	/**
	 * Sets the validation result for the given attribute.
	 *
	 * @param attribute
	 *        The attribute whose validation changed.
	 * @param result
	 *        The new validation result.
	 */
	default void setValidation(TLStructuredTypePart attribute, ValidationResult result) {
		// Default no-op, overridden in implementations that support validation.
	}

	/**
	 * Adds a listener for validation state changes.
	 */
	default void addConstraintValidationListener(ConstraintValidationListener listener) {
		// Default no-op, overridden in implementations that support validation.
	}

	/**
	 * Removes a previously added validation listener.
	 */
	default void removeConstraintValidationListener(ConstraintValidationListener listener) {
		// Default no-op, overridden in implementations that support validation.
	}

}
