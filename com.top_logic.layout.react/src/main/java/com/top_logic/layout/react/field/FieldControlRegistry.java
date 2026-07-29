/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.field;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;
import com.top_logic.layout.react.control.form.ReactI18NStringInputControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;

/**
 * The {@link ReactFieldControlProvider}s that edit values, by value type.
 *
 * <p>
 * A control is looked up by the {@link FieldSpec#getValueType() type of the edited value}, so a model
 * attribute and a configuration property holding the same kind of value are edited the same way. The
 * lookup considers supertypes, so a provider registered for a base type serves its subtypes.
 * </p>
 *
 * <p>
 * The registry starts out with the providers for the types the platform edits itself. An application
 * or another module registers further ones through {@link #register(Class, ReactFieldControlProvider)}.
 * A single field can deviate from its type's provider; how that is expressed is up to the editing
 * side, which passes the provider it resolved instead of asking the registry.
 * </p>
 */
public class FieldControlRegistry {

	/**
	 * Edits a value as a single- or multi-line text.
	 *
	 * @implNote Declared before {@link #getInstance() the shared registry}, which registers it while
	 *           being created.
	 */
	public static final ReactFieldControlProvider TEXT = (context, field, model) -> {
		ReactTextInputControl control = new ReactTextInputControl(context, model);
		if (field.getMultilineRows() > 0) {
			control.setMultiline(field.getMultilineRows());
		}
		return control;
	};

	private static final FieldControlRegistry INSTANCE = new FieldControlRegistry();

	private final Map<Class<?>, ReactFieldControlProvider> _providers = new LinkedHashMap<>();

	/**
	 * Creates a {@link FieldControlRegistry} holding the platform's providers.
	 */
	protected FieldControlRegistry() {
		register(String.class, TEXT);
		register(Boolean.class, (context, field, model) -> new ReactCheckboxControl(context, model));
		register(Number.class,
			(context, field, model) -> new ReactNumberInputControl(context, model, decimals(field)));
		register(Date.class, (context, field, model) -> new ReactDatePickerControl(context, model));
		// An internationalized text is edited in the current language, with the other languages
		// reachable through the editor's dialog.
		register(ResKey.class, (context, field, model) -> ReactI18NStringInputControl.createEditor(context, model,
			field.getMultilineRows(), field.getLabel()));
	}

	/**
	 * The registry the editing sides consult.
	 */
	public static FieldControlRegistry getInstance() {
		return INSTANCE;
	}

	/**
	 * Registers the provider editing values of the given type and its subtypes.
	 *
	 * @param valueType
	 *        The type of the edited value. A primitive type is registered as its wrapper.
	 * @param provider
	 *        Creates the control.
	 */
	public void register(Class<?> valueType, ReactFieldControlProvider provider) {
		_providers.put(wrapperType(valueType), provider);
	}

	/**
	 * The provider editing values of the given type, or {@code null} if none is registered for it or
	 * any of its supertypes.
	 */
	public ReactFieldControlProvider lookup(Class<?> valueType) {
		if (valueType == null) {
			return null;
		}
		for (Class<?> type = wrapperType(valueType); type != null; type = type.getSuperclass()) {
			ReactFieldControlProvider provider = _providers.get(type);
			if (provider != null) {
				return provider;
			}
			for (Class<?> intf : type.getInterfaces()) {
				ReactFieldControlProvider fromInterface = _providers.get(intf);
				if (fromInterface != null) {
					return fromInterface;
				}
			}
		}
		return null;
	}

	/**
	 * Creates the control editing the given value.
	 *
	 * <p>
	 * Falls back to editing the value as {@link #TEXT text} when no provider is registered for its
	 * type, so an unforeseen type is still displayed.
	 * </p>
	 */
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
		ReactFieldControlProvider provider = lookup(field.getValueType());
		return (provider == null ? TEXT : provider).createControl(context, field, model);
	}

	/**
	 * The number of decimals to display for a numeric value.
	 */
	private static int decimals(FieldSpec field) {
		Class<?> type = wrapperType(field.getValueType());
		return type == Double.class || type == Float.class ? 2 : 0;
	}

	/**
	 * The wrapper type of a primitive type, the type itself otherwise.
	 */
	private static Class<?> wrapperType(Class<?> type) {
		if (!type.isPrimitive()) {
			return type;
		}
		if (type == boolean.class) {
			return Boolean.class;
		}
		if (type == int.class) {
			return Integer.class;
		}
		if (type == long.class) {
			return Long.class;
		}
		if (type == double.class) {
			return Double.class;
		}
		if (type == float.class) {
			return Float.class;
		}
		if (type == short.class) {
			return Short.class;
		}
		if (type == byte.class) {
			return Byte.class;
		}
		if (type == char.class) {
			return Character.class;
		}
		return type;
	}
}
