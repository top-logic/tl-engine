/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Reaction to a change of an attribute value of an object edited in a form.
 *
 * <p>
 * A listener is annotated to an attribute with {@link TLValueListeners}. It is informed when the
 * value of that attribute changes in a form, independent of the user interface technology that
 * displays the form. It may write other attributes of the edited object, e.g. to reset values
 * that depend on the changed one.
 * </p>
 *
 * @see TLValueListeners
 */
@FunctionalInterface
public interface AttributeValueListener {

	/**
	 * Informs this listener about a value change of the given attribute.
	 *
	 * @param object
	 *        The object being edited in the form. Writing to it has the same effect as an input
	 *        of the user.
	 * @param attribute
	 *        The attribute whose value has changed.
	 * @param oldValue
	 *        The value before the change.
	 * @param newValue
	 *        The value after the change.
	 */
	void valueChanged(TLFormObjectBase object, TLStructuredTypePart attribute, Object oldValue, Object newValue);

}
