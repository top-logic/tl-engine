/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.Arrays;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactSelectFormFieldControl;
import com.top_logic.layout.react.field.FieldControl;
import com.top_logic.layout.react.field.FieldControlRegistry;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.util.Resources;

/**
 * Creates the control editing a configuration property.
 *
 * <p>
 * The control comes from the {@link FieldControlRegistry}, keyed by the type of the property's value,
 * so a configuration property is edited by the same control as a model attribute holding the same kind
 * of value. A single property deviates from its type's control through a {@link FieldControl}
 * annotation.
 * </p>
 *
 * <p>
 * Two cases are decided here rather than by value type: a property offering a fixed set of values is
 * edited as a selection, and a property whose values are not strings but that has a textual
 * specification is edited as that text, see {@link ConfigTextFieldModel}.
 * </p>
 */
public class ConfigFieldDispatch {

	/**
	 * Creates the control editing the given property.
	 *
	 * @param context
	 *        The React context.
	 * @param model
	 *        The field model bound to a configuration property.
	 * @param label
	 *        The label of the edited field, which an editor may display inside itself.
	 * @return The control to display.
	 */
	public static ReactControl createPlainControl(ReactContext context, ConfigFieldModel model, String label) {
		PropertyDescriptor property = model.getProperty();
		Class<?> type = property.getType();

		if (type.isEnum()) {
			return createEnumControl(context, model, type);
		}

		FieldSpec field = FieldSpec.of(type, label)
			.setMandatory(model.isMandatory())
			.setEditable(model.isEditable());

		ReactFieldControlProvider annotated = annotatedProvider(property);
		if (annotated != null) {
			return annotated.createControl(context, field, model);
		}

		FieldControlRegistry registry = FieldControlRegistry.getInstance();
		if (registry.lookup(type) == null && ConfigTextFieldModel.isFormatted(property)) {
			// No control edits this kind of value, but the configuration file writes it as text, so
			// that text is edited instead of the value.
			FieldSpec asText = FieldSpec.of(String.class, label)
				.setMandatory(field.isMandatory())
				.setEditable(field.isEditable());
			return registry.createControl(context, asText,
				new ConfigTextFieldModel(model.getConfig(), property));
		}
		return registry.createControl(context, field, model);
	}

	/**
	 * The provider named by a {@link FieldControl} annotation on the property, or {@code null} if the
	 * property is edited by its value type's control.
	 */
	private static ReactFieldControlProvider annotatedProvider(PropertyDescriptor property) {
		FieldControl annotation = property.getAnnotation(FieldControl.class);
		if (annotation == null) {
			return null;
		}
		try {
			return annotation.value().getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException ex) {
			Logger.error("Cannot create the control announced for '" + property.getPropertyName()
				+ "'; editing it by its value type instead.", ex, ConfigFieldDispatch.class);
			return null;
		}
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
