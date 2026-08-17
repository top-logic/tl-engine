/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.element.config.annotation.ConfigType;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;

/**
 * {@link ReactFieldControlProvider} for attributes holding a point in time.
 *
 * <p>
 * A date, a time of day and a date with a time of day all have the model kind
 * {@link com.top_logic.model.TLPrimitive.Kind#DATE} and are all stored as a {@code Date}; which of
 * them an attribute means is said by its {@link ConfigType} annotation ({@code tl.core:Time}
 * carries {@code TIME}, {@code tl.core:DateTime} carries {@code DATE_TIME}). Without that
 * distinction a time of day would be edited in a date picker - the wrong widget for it, and one
 * that offers no way at all to enter the part of the value that matters.
 * </p>
 */
public class DatePickerControlProvider implements ReactFieldControlProvider {

	/** {@link ConfigType} value of a time of day. */
	private static final String CONFIG_TYPE_TIME = "TIME";

	/** {@link ConfigType} value of a date with a time of day. */
	private static final String CONFIG_TYPE_DATE_TIME = "DATE_TIME";

	@Override
	public ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		return new ReactDatePickerControl(context, model, kind(part));
	}

	/**
	 * Which part of a point in time the given attribute holds.
	 *
	 * <p>
	 * Shared with the table layer: a column over such an attribute displays and filters its cells in
	 * the format belonging to this kind.
	 * </p>
	 */
	public static ReactDatePickerControl.Kind kind(TLStructuredTypePart part) {
		switch (configType(part)) {
			case CONFIG_TYPE_TIME:
				return ReactDatePickerControl.Kind.TIME;
			case CONFIG_TYPE_DATE_TIME:
				return ReactDatePickerControl.Kind.DATE_TIME;
			default:
				return ReactDatePickerControl.Kind.DATE;
		}
	}

	/**
	 * The {@link ConfigType} of the given attribute, or the empty string if it has none.
	 *
	 * <p>
	 * An annotation at the attribute wins over the one of its type, as the annotation's redefine
	 * policy prescribes: an attribute may well declare that it means a time of day even though its
	 * type says nothing.
	 * </p>
	 */
	private static String configType(TLStructuredTypePart part) {
		if (part == null) {
			return "";
		}
		ConfigType annotation = part.getAnnotation(ConfigType.class);
		if (annotation == null) {
			TLType type = part.getType();
			annotation = type == null ? null : type.getAnnotation(ConfigType.class);
		}
		return annotation == null || annotation.getValue() == null ? "" : annotation.getValue();
	}

}
