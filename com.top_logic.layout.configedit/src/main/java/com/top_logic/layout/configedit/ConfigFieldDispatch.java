/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.Arrays;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.control.form.ReactSelectFormFieldControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.util.Resources;

/**
 * Dispatches a {@link ConfigFieldModel} to the appropriate React form field control based on the
 * property type.
 */
public class ConfigFieldDispatch {

	/**
	 * Creates a {@link ReactControl} for a PLAIN property.
	 *
	 * @param context
	 *        The React context.
	 * @param model
	 *        The field model bound to a configuration property.
	 * @return A React control appropriate for the property's type.
	 */
	public static ReactControl createPlainControl(ReactContext context, ConfigFieldModel model) {
		PropertyDescriptor property = model.getProperty();
		Class<?> type = property.getType();

		if (type == boolean.class || type == Boolean.class) {
			return new ReactCheckboxControl(context, model);
		}
		if (type == int.class || type == Integer.class || type == long.class || type == Long.class) {
			return new ReactNumberInputControl(context, model, 0);
		}
		if (type == double.class || type == Double.class || type == float.class || type == Float.class) {
			return new ReactNumberInputControl(context, model, 2);
		}
		if (type.isEnum()) {
			return createEnumControl(context, model, type);
		}
		// String and everything else: text input.
		return new ReactTextInputControl(context, model);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static ReactControl createEnumControl(ReactContext context, ConfigFieldModel model, Class<?> enumType) {
		Object[] constants = enumType.getEnumConstants();
		ConfigSelectFieldModel selectModel =
			new ConfigSelectFieldModel(model.getConfig(), model.getProperty(), Arrays.asList(constants), false);
		LabelProvider labelProvider = enumLabelProvider(model.getProperty());
		return new ReactSelectFormFieldControl(context, selectModel, labelProvider);
	}

	/**
	 * Creates a {@link LabelProvider} for enum constants of the given property.
	 *
	 * <p>
	 * Labels are resolved via the property's label key with suffix {@code @<constantName>}.
	 * Falls back to the enum constant's {@link Enum#name()} if no resource is found.
	 * </p>
	 */
	static LabelProvider enumLabelProvider(PropertyDescriptor property) {
		return object -> {
			if (object instanceof Enum) {
				String suffix = "@" + ((Enum<?>) object).name();
				String label = Resources.getInstance().getString(property.labelKey(suffix), null);
				if (label != null) {
					return label;
				}
				return ((Enum<?>) object).name();
			}
			return object != null ? object.toString() : "";
		};
	}
}
