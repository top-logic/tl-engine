/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.basic.config.customization.ProgrammaticCustomizations;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.DerivedProperty;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.provider.label.ClassLabelProvider;

/**
 * Adapter that looks up the polymorphic options for a {@link PropertyDescriptor} through
 * {@link Fields#optionProvider(DeclarativeFormOptions)} without requiring an
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

	private static final TypedAnnotatable EMPTY_ANNOTATIONS = new LazyTypedAnnotatable();

	private static final AnnotationCustomizations CUSTOMIZATIONS = new ProgrammaticCustomizations();

	private static final ClassLabelProvider CLASS_LABELS = new ClassLabelProvider();

	/**
	 * Result of resolving the polymorphic options for a property: the raw option list and the
	 * {@link OptionMapping} that translates between option and stored configuration value.
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
		DerivedProperty<? extends Iterable<?>> provider = Fields.optionProvider(declarativeOptions(property));
		if (provider == null) {
			return Choices.NONE;
		}
		OptionMapping mapping = Fields.optionMapping(provider);
		Iterable<?> raw = provider.get(parentConfig);
		List<Object> options = new ArrayList<>();
		if (raw != null) {
			for (Object item : raw) {
				options.add(item);
			}
		}
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
	 * A {@link LabelProvider} resolving index-based option keys (as {@link String}) to their
	 * {@link #labelFor(Object) labels}.
	 */
	public static LabelProvider indexLabelProvider(List<?> options) {
		return value -> {
			if (!(value instanceof String key) || key.isEmpty()) {
				return "";
			}
			int idx = parseIndex(key);
			if (idx < 0 || idx >= options.size()) {
				return key;
			}
			return labelFor(options.get(idx));
		};
	}

	/**
	 * Computes the index-based string key for an option at the given position.
	 */
	public static String keyFor(int index) {
		return Integer.toString(index);
	}

	/**
	 * Resolves an index-based key back to the option at that position, or {@code null} if the key
	 * is unknown.
	 */
	public static Object optionForKey(List<?> options, String key) {
		int idx = parseIndex(key);
		if (idx < 0 || idx >= options.size()) {
			return null;
		}
		return options.get(idx);
	}

	/**
	 * Finds the index-based key for the option currently represented by the given configuration
	 * item, using the {@link OptionMapping} to invert the mapping.
	 */
	public static String keyForItem(List<?> options, OptionMapping mapping, Object item) {
		if (item == null || mapping == null) {
			return null;
		}
		Object option = mapping.asOption(options, item);
		if (option == null) {
			return null;
		}
		int idx = options.indexOf(option);
		return idx >= 0 ? keyFor(idx) : null;
	}

	private static int parseIndex(String key) {
		if (key == null) {
			return -1;
		}
		try {
			return Integer.parseInt(key);
		} catch (NumberFormatException ex) {
			return -1;
		}
	}

	private static DeclarativeFormOptions declarativeOptions(PropertyDescriptor property) {
		return new DeclarativeFormOptions() {
			@Override
			public PropertyDescriptor getProperty() {
				return property;
			}

			@Override
			public AnnotationCustomizations getCustomizations() {
				return CUSTOMIZATIONS;
			}

			@Override
			public <T> T get(Property<T> p) {
				return EMPTY_ANNOTATIONS.get(p);
			}

			@Override
			public boolean isSet(Property<?> p) {
				return EMPTY_ANNOTATIONS.isSet(p);
			}

			@Override
			public <T> T set(Property<T> p, T value) {
				return EMPTY_ANNOTATIONS.set(p, value);
			}

			@Override
			public <T> T reset(Property<T> p) {
				return EMPTY_ANNOTATIONS.reset(p);
			}
		};
	}
}
