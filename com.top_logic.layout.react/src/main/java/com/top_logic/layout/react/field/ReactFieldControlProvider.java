/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.field;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;

/**
 * Creates the control that edits a value of a certain type.
 *
 * <p>
 * A provider is registered for the value types it handles, see {@link FieldControlRegistry}. It
 * receives the value through a {@link FieldModel} and its display hints through a {@link FieldSpec},
 * and thus serves both model attributes and configuration properties.
 * </p>
 */
@FunctionalInterface
public interface ReactFieldControlProvider {

	/**
	 * Creates the control editing the given value.
	 *
	 * @param context
	 *        The context to create the control in.
	 * @param field
	 *        What is being edited.
	 * @param model
	 *        Holds the edited value.
	 * @return The control to display.
	 */
	ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model);

	/**
	 * The control editing the given value, displayed as the {@link FieldSpec} asks for.
	 *
	 * <p>
	 * What a caller calls: {@link #createControl(ReactContext, FieldSpec, FieldModel)} produces the
	 * control, and this method applies the display properties of the specification that the
	 * produced control does not read itself. A property that every field control understands is
	 * applied here once, so that neither a provider nor a caller has to pass it on.
	 * </p>
	 *
	 * @param context
	 *        The context to create the control in.
	 * @param field
	 *        What is being edited.
	 * @param model
	 *        Holds the edited value.
	 * @return The control to display.
	 */
	default ReactControl createField(ReactContext context, FieldSpec field, FieldModel model) {
		ReactControl control = createControl(context, field, model);
		if (control instanceof ReactFormFieldControl fieldControl) {
			String placeholder = field.getPlaceholder();
			if (placeholder != null) {
				fieldControl.setPlaceholder(placeholder);
			}
			String icon = field.getIcon();
			if (icon != null) {
				fieldControl.setIcon(icon);
			}
			if (field.isClearable()) {
				fieldControl.setClearable(true);
			}
			Long debounce = field.getDebounce();
			if (debounce != null) {
				fieldControl.setDebounce(debounce);
			}
		}
		return control;
	}

	/**
	 * Whether the control this provider creates edits the whole collection of values of a
	 * {@link FieldSpec#isMultiple() multi-valued} field itself.
	 *
	 * <p>
	 * A select over options does: picking several options is one gesture on one control, and the
	 * value it writes is the collection. An input that takes one value - a text box, a number, a
	 * date, a checkbox - does not: it edits a single value, and the collection around it is built
	 * from one such input per element, see
	 * {@link FieldControlRegistry#createControl(ReactContext, FieldSpec, FieldModel, ReactFieldControlProvider)}.
	 * </p>
	 */
	default boolean editsCollections() {
		return false;
	}

}
