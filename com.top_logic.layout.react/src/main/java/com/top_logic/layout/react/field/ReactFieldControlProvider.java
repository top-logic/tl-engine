/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.field;

import java.util.Collection;

import com.top_logic.basic.util.Utils;
import com.top_logic.basic.util.WithEmptiness;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.provider.CollectionLabelProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
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

	/**
	 * Whether the control this provider creates for the given field needs more room than a
	 * {@link FieldSpec#isCompact() compact} display offers.
	 *
	 * <p>
	 * A control taller than a single line of text - a multi-line text, a code editor, a rich text
	 * editor - does. In a compact display, such a field is shown as its
	 * {@link #previewText(FieldSpec, Object) preview} together with a button opening the control in
	 * a dialog.
	 * </p>
	 *
	 * @param field
	 *        What is being edited.
	 */
	default boolean isLarge(FieldSpec field) {
		return false;
	}

	/**
	 * Whether the given value has no content to display.
	 *
	 * <p>
	 * Where a field that may not be edited is displayed {@link FieldSpec#isCompact() compactly}, an
	 * empty value offers no dialog: there is nothing to show in it. By default {@code null}, the
	 * empty text, an empty collection, and a value that {@link WithEmptiness says} it is empty, see
	 * {@link Utils#isEmpty(Object)}.
	 * </p>
	 *
	 * <p>
	 * Not the same as an empty {@link #previewText(FieldSpec, Object) preview}: a value may have
	 * content that a line of text cannot show, an image for instance.
	 * </p>
	 *
	 * @param field
	 *        What is being edited.
	 * @param value
	 *        The value to check, or {@code null}.
	 */
	default boolean isEmpty(FieldSpec field, Object value) {
		return Utils.isEmpty(value);
	}

	/**
	 * A single line of text standing for the given value where the control editing it has no room.
	 *
	 * <p>
	 * By default the label of the value, the labels of its elements separated by commas for a
	 * collection, and of a text spanning several lines only the first one. Nothing is displayed for
	 * no value.
	 * </p>
	 *
	 * @param field
	 *        What is being edited.
	 * @param value
	 *        The value to preview, or {@code null}.
	 * @return The preview text, never {@code null}.
	 */
	default String previewText(FieldSpec field, Object value) {
		if (value == null) {
			return "";
		}
		String label;
		if (value instanceof Collection<?>) {
			label = new CollectionLabelProvider(MetaLabelProvider.INSTANCE, ", ").getLabel(value);
		} else {
			label = MetaLabelProvider.INSTANCE.getLabel(value);
		}
		return firstLine(label);
	}

	/**
	 * The first line of the given text.
	 *
	 * @param text
	 *        The text, or {@code null}.
	 * @return The text up to its first line break, the empty string for {@code null}.
	 */
	static String firstLine(String text) {
		if (text == null) {
			return "";
		}
		for (int n = 0, cnt = text.length(); n < cnt; n++) {
			char ch = text.charAt(n);
			if (ch == '\n' || ch == '\r') {
				return text.substring(0, n);
			}
		}
		return text;
	}

	/**
	 * The first line of the given text that holds more than white space, without leading and
	 * trailing white space.
	 *
	 * <p>
	 * Stands for a source text - a script, a program - whose first lines may well be empty or
	 * indented.
	 * </p>
	 *
	 * @param text
	 *        The text, or {@code null}.
	 * @return The first non-blank line, the empty string if there is none.
	 */
	static String firstNonBlankLine(String text) {
		if (text == null) {
			return "";
		}
		return text.lines().map(String::strip).filter(line -> !line.isEmpty()).findFirst().orElse("");
	}

}
