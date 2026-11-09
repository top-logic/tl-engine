/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form;

import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.model.annotate.AttributeValueListener;

/**
 * {@link ValueListener} of a {@link FormField} created for an attribute that forwards each value
 * change to an {@link AttributeValueListener}.
 *
 * <p>
 * The edited object and the attribute passed to the {@link AttributeValueListener} are taken from
 * the {@link AttributeUpdate} of the changed field, see
 * {@link AttributeFormFactory#getAttributeUpdate(com.top_logic.layout.form.FormMember)}.
 * </p>
 */
public class AttributeValueListenerAdapter implements ValueListener {

	private final AttributeValueListener _listener;

	/**
	 * Creates a {@link AttributeValueListenerAdapter}.
	 *
	 * @param listener
	 *        The listener to inform about value changes of the field.
	 */
	public AttributeValueListenerAdapter(AttributeValueListener listener) {
		_listener = listener;
	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		AttributeUpdate update = AttributeFormFactory.getAttributeUpdate(field);
		_listener.valueChanged(update.getOverlay(), update.getAttribute(), oldValue, newValue);
	}

}
