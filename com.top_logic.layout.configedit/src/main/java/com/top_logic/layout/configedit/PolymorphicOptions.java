/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.basic.config.customization.ProgrammaticCustomizations;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.DerivedProperty;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.edit.OptionMapping;

/**
 * Adapter that looks up the polymorphic options for a {@link PropertyDescriptor} through
 * {@link Fields#optionProvider(DeclarativeFormOptions)} without requiring an
 * {@link com.top_logic.layout.form.values.edit.EditorFactory EditorFactory} / form context.
 *
 * <p>
 * Produces the same option list and {@link OptionMapping} that the declarative form editor would
 * use, including {@link com.top_logic.layout.form.values.edit.annotation.Options @Options}
 * function evaluation, impl-class / config-class branching via
 * {@link PropertyDescriptor#getInstanceType()}, hidden-class filtering and sorting.
 * </p>
 */
public class PolymorphicOptions {

	private static final TypedAnnotatable EMPTY_ANNOTATIONS = new LazyTypedAnnotatable();

	private static final AnnotationCustomizations CUSTOMIZATIONS = new ProgrammaticCustomizations();

	/**
	 * Result of resolving the polymorphic options for a property: the option classes and the
	 * {@link OptionMapping} that translates between option and stored configuration value.
	 */
	public record Choices(List<Class<?>> options, OptionMapping mapping) {

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
		List<Class<?>> classes = collectOptions(raw);
		return new Choices(classes, mapping);
	}

	/**
	 * Converts a user-picked option class to the value to store in the property.
	 */
	public static ConfigurationItem toConfig(OptionMapping mapping, Class<?> selected) {
		return (ConfigurationItem) mapping.toSelection(selected);
	}

	/**
	 * Converts a currently-stored configuration value back to the option class identifying it in
	 * an option list.
	 */
	public static Class<?> fromConfig(OptionMapping mapping, Iterable<Class<?>> allOptions,
			ConfigurationItem item) {
		if (item == null) {
			return null;
		}
		return (Class<?>) mapping.asOption(allOptions, item);
	}

	private static List<Class<?>> collectOptions(Iterable<?> raw) {
		List<Class<?>> result = new ArrayList<>();
		Iterator<?> iterator;
		if (raw instanceof OptionModel<?> model) {
			iterator = model.iterator();
		} else if (raw != null) {
			iterator = raw.iterator();
		} else {
			return result;
		}
		while (iterator.hasNext()) {
			result.add((Class<?>) iterator.next());
		}
		return result;
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
