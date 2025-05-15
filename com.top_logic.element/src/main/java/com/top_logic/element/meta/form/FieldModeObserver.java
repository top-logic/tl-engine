/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form;

import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.ModeSelector;
import com.top_logic.model.form.definition.FormVisibility;

/**
 * Observes the {@link FormVisibility} of a field.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class FieldModeObserver extends ModeObserver {

	private FormField _field;

	/**
	 * Creates a {@link FieldModeObserver}.
	 * 
	 * @param field
	 *        Field to calculate the visibility for.
	 * @param updateContainer
	 *        Container to get the values of.
	 * @param modeSelector
	 *        Calculates the actual {@link FormVisibility}.
	 * @param object
	 *        Model to observe.
	 * @param attribute
	 *        The attribute to observe. Maybe <code>null</code>.
	 */
	public FieldModeObserver(FormField field, AttributeUpdateContainer updateContainer, ModeSelector modeSelector,
			TLObject object, TLStructuredTypePart attribute) {
		super(updateContainer, modeSelector, object, attribute);

		_field = field;
	}

	@Override
	public void valueChanged(FormVisibility mode) {
		mode.applyTo(_field);
		switch (mode) {
			case READ_ONLY:
				// Reset to original value to prevent modifying values by
				// temporarily activating fields.
				_field.reset();
				break;
			case HIDDEN:
				// Clear value to prevent leaking irrelevant values into the
				// model.
				_field.setValue(null);
				break;
			default:
				break;
		}
	}
}
