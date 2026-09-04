/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.DerivedProperty;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.provider.label.ClassLabelProvider;
import com.top_logic.layout.react.control.form.ReactSelectFormFieldControl;

/**
 * Adapter that looks up the polymorphic options for a {@link PropertyDescriptor} through
 * {@link ConfigPropertyOptions#optionProvider(PropertyDescriptor)} without requiring an
 * {@link com.top_logic.layout.form.values.edit.EditorFactory EditorFactory} / form context.
 *
 * <p>
 * Produces the same option list and {@link OptionMapping} that the declarative form editor would
 * use. The options are returned as opaque {@code Object}s; callers must consult the accompanying
 * {@link OptionMapping} to convert between options and stored configuration values. No assumption
 * is made about whether the options are {@link Class} objects, {@link ConfigurationItem}s, or
 * domain-specific values produced by an {@code @Options} function.
 * </p>
 */
public class PolymorphicOptions {

	private static final ClassLabelProvider CLASS_LABELS = new ClassLabelProvider();

	/**
	 * Result of resolving the polymorphic options for a property: the raw option list and the
	 * {@link OptionMapping} that translates between option and stored configuration value.
	 *
	 * @param options
	 *        The raw option list offered for the property.
	 * @param mapping
	 *        The {@link OptionMapping} translating between an option and the stored configuration
	 *        value.
	 */
	public record Choices(List<Object> options, OptionMapping mapping) {

		/** Empty {@link Choices} (no options, {@code null} mapping). */
		public static final Choices NONE = new Choices(List.of(), null);

		/** Whether the property has polymorphic options at all. */
		public boolean hasOptions() {
			return mapping != null;
		}
	}

	/**
	 * Computes the options available for the given polymorphic property on the given parent
	 * configuration.
	 *
	 * @param parentConfig
	 *        The configuration item owning the property; used as the model context for evaluating
	 *        argument references of {@code @Options} functions.
	 * @param property
	 *        The property whose options are requested.
	 */
	public static Choices compute(ConfigurationItem parentConfig, PropertyDescriptor property) {
		DerivedProperty<? extends Iterable<?>> provider = ConfigPropertyOptions.optionProvider(parentConfig, property);
		if (provider == null) {
			return Choices.NONE;
		}
		List<Object> options = ConfigPropertyOptions.toList(provider.get(parentConfig));
		OptionMapping mapping = Fields.optionMapping(provider);
		return new Choices(options, mapping);
	}

	/**
	 * Renders a human-readable label for an option value.
	 *
	 * <p>
	 * Used by the type-selector select field. Handles the common option types produced by
	 * {@link Fields#optionProvider(DeclarativeFormOptions)}: {@link Class} (default for polymorphic
	 * properties without {@code @Options}), {@link PolymorphicConfiguration} (default instances
	 * carrying an implementation class), or any other object (falls back to {@code toString()}).
	 * </p>
	 */
	public static String labelFor(Object option) {
		if (option == null) {
			return "";
		}
		if (option instanceof Class<?> cls) {
			return CLASS_LABELS.getLabel(cls);
		}
		if (option instanceof PolymorphicConfiguration<?> cfg) {
			Class<?> impl = cfg.getImplementationClass();
			if (impl != null) {
				return CLASS_LABELS.getLabel(impl);
			}
		}
		return option.toString();
	}

	/**
	 * A {@link LabelProvider} resolving option keys to their {@link #labelFor(Object) labels}.
	 *
	 * <p>
	 * A key the given options do not name is labelled from the key itself, not left blank: the key
	 * is a type name, and a selector is given the type an entry actually has even where that type
	 * is no longer offered (see {@link ReactSelectFormFieldControl}).
	 * </p>
	 */
	public static LabelProvider keyLabelProvider(List<?> options) {
		return value -> {
			if (!(value instanceof String key) || key.isEmpty()) {
				return "";
			}
			for (Object option : options) {
				if (key.equals(keyFor(option))) {
					return labelFor(option);
				}
			}
			return labelForTypeName(key);
		};
	}

	/**
	 * The key addressing the given option: the name of the type behind it.
	 *
	 * <p>
	 * A name rather than a position in the option list. A position means something only together
	 * with the list it indexes, so it changes when the list does - between two entries of the same
	 * collection, or between two renderings of the same entry - while a key is quoted in recorded
	 * scripts, sent back by the client, and must mean the same thing every time. A name also exists
	 * for a type the list does not offer at all, which is what lets a selector show the type an
	 * entry actually has.
	 * </p>
	 *
	 * @return The key, or {@code null} for an option that names no type.
	 */
	public static String keyFor(Object option) {
		Class<?> type = typeOf(option);
		return type == null ? null : type.getName();
	}

	/**
	 * Resolves a key back to the option it addresses, or {@code null} if none of the given options
	 * does.
	 */
	public static Object optionForKey(List<?> options, String key) {
		if (key == null) {
			return null;
		}
		for (Object option : options) {
			if (key.equals(keyFor(option))) {
				return option;
			}
		}
		return null;
	}

	/**
	 * The key for the type the given configuration item currently has, using the
	 * {@link OptionMapping} to invert the mapping.
	 *
	 * <p>
	 * Answers even where the item's type is not among {@code options}: the key is the type's name,
	 * which does not depend on what is offered.
	 * </p>
	 */
	public static String keyForItem(List<?> options, OptionMapping mapping, Object item) {
		if (item == null || mapping == null) {
			return null;
		}
		return keyFor(mapping.asOption(options, item));
	}

	/**
	 * The type an option stands for, or {@code null} for an option that stands for none.
	 */
	private static Class<?> typeOf(Object option) {
		if (option instanceof Class<?> cls) {
			return cls;
		}
		if (option instanceof PolymorphicConfiguration<?> cfg) {
			return cfg.getImplementationClass();
		}
		return null;
	}

	/**
	 * The label for a type named by a key, resolved through the class where that is possible and
	 * left as the bare name where it is not.
	 */
	private static String labelForTypeName(String key) {
		try {
			return CLASS_LABELS.getLabel(Class.forName(key));
		} catch (ClassNotFoundException | LinkageError ex) {
			// The key names a type this application does not have. Nothing better than the name
			// itself to say, and saying it beats saying nothing.
			return key;
		}
	}

}
