/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.base.services.simpleajax.XMLValueConstants;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.FormField;

/**
 * Static helpes for otherwise protected functionality in the
 * {@link AbstractFormField} hierarchy.
 * 
 * <p>
 * Note: Methods of this class must only be called within the
 * <code>com.top_logic.layout.form</code> package and its subpackages.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormFieldInternals {

	/**
	 * Updates a field ensuring that a property update event is generated.
	 * 
	 * <p>
	 * Controls translate value property events into client-side updates
	 * (instead of acting as value change listeners). By updating a field with a
	 * raw input value (see {@link FormField#update(Object)}, no property
	 * update event is generated, because the event came from the GUI and must
	 * not be sent back to the GUI. By updating e.g. a select field from a popup
	 * select dialog, such property update event is required, because the view
	 * has to be updated to reflect the new internal state of the field.
	 * </p>
	 * 
	 * @return The parsed application value. 
	 */
	public static Object updateField(AbstractFormField field, Object aRawValue) throws VetoException {
		Object oldRawValue = field.getRawValue();
		field.update(aRawValue);

		// Make sure, the UI is updated, if the test is executed in interactive mode.
		field.firePropertyChanged(FormField.VALUE_PROPERTY, field, oldRawValue, field.getRawValue());

		Object newValue;
		if (field.hasValue()) {
			newValue = field.getValue();
		} else {
			newValue = null;
		}

		return newValue;
	}

	public static void setValue(AbstractFormField field, Object applicationValue) throws VetoException {
		field.deliverValue(applicationValue);
	}

	public static Object parseRawValue(AbstractFormField aFormField, Object aRawValue) throws CheckException {
		return aFormField.parseRawValue(aRawValue);
	}

	/**
	 * Feed the given value that was transmitted from the client as new input to the given
	 * {@link com.top_logic.layout.form.FormField}.
	 * 
	 * <p>
	 * After {@link FormField#update(Object) updating} the given field, it is checked for potential
	 * errors.
	 * </p>
	 * 
	 * <p>
	 * Note: The given field does not fire a {@link FormField#VALUE_PROPERTY} in response to the
	 * update, because it assumes that the given value was transmitted from the client (and the
	 * client therefore is already up-to-date). The field might fire a value event, if the input
	 * changes its application value.
	 * </p>
	 * 
	 * @param field
	 *        The field to update.
	 * @param newClientValue
	 *        The new value transmitted by the client-side view. The value can be any JavaScript
	 *        object that can be transmitted by the protocol defined in {@link XMLValueConstants}.
	 * @throws VetoException
	 *         iff the update method of the given {@link FormField} throws one.
	 */
	public static void updateFieldNoClientUpdate(FormField field, Object newClientValue) throws VetoException {
		// Update a single member with a new values.
		field.update(newClientValue);
	
		// Check the new value only if the current form field requires a
		// check. If the update did deliver exactly the same value that
		// is already stored in the field, there is no new check
		// required/possible. Under normal circumstance, no
		// valueChanged event should be fired, if the 
		// value of the field did not actually change.
		if (! field.isCheckRequired(false)) {
			return; 
		}
	
		field.checkWithAllDependencies();
		
		return;
	}

	/**
	 * The given field's value, regardless of its {@link AbstractFormField#hasValue() validity
	 * state}.
	 * 
	 * @see AbstractFormField#getStoredValue()
	 */
	public static Object getStoredValue(AbstractFormField field) {
		return field.getStoredValue();
	}

}
