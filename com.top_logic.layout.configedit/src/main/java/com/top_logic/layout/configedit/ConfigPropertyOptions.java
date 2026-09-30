/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.basic.config.customization.ConfiguredAnnotationCustomizations;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.DerivedProperty;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * {@link DeclarativeFormOptions} over a single configuration property.
 *
 * <p>
 * Exists so that the option resolution of the classic declarative form
 * ({@link Fields#optionProvider(DeclarativeFormOptions)}) can be reused: it resolves the
 * {@link Options} annotation including its function, mapping and argument references, and the
 * implementation lists of polymorphic properties. This is the single place in this package that
 * resolves options for a configuration property; other controls (e.g. the polymorphic type
 * selector) delegate to the static helpers below rather than building their own
 * {@link DeclarativeFormOptions}.
 * </p>
 *
 * <p>
 * Each instance is created fresh for a single lookup and starts with an empty
 * {@link TypedAnnotatable} store: unlike the classic {@link EditorFactory}, it carries no form
 * context supplied by a caller. An {@link Options#fun()} function that expects such context will
 * not find it here.
 * </p>
 */
public class ConfigPropertyOptions extends LazyTypedAnnotatable implements DeclarativeFormOptions {

	private static final AnnotationCustomizations NO_CUSTOMIZATIONS =
		new ConfiguredAnnotationCustomizations();

	private final PropertyDescriptor _property;

	/**
	 * Creates a {@link ConfigPropertyOptions}.
	 *
	 * @param property
	 *        The property whose options are resolved.
	 */
	public ConfigPropertyOptions(PropertyDescriptor property) {
		_property = property;
	}

	/**
	 * Creates a {@link ConfigPropertyOptions} that also offers the edited configuration as the form
	 * model.
	 *
	 * @param formModel
	 *        What is being edited as a whole, or {@code null} if the caller does not know. Becomes
	 *        {@link DeclarativeFormBuilder#FORM_MODEL}, which is what an option function or mapping
	 *        reaches for when it needs to know the surroundings of the property -
	 *        {@code SingletonConfig.LocalTypeMapping}, for one, reads the module's name off it and
	 *        fails outright without it.
	 *
	 *        <p>
	 *        Passed in rather than derived from the property's own item: the way up is
	 *        {@link com.top_logic.basic.config.ConfigPart#container()}, and not every configuration
	 *        on the way is a {@code ConfigPart} - {@code SingletonConfig} is not - so walking up
	 *        stops early and silently at the wrong item.
	 *        </p>
	 */
	public ConfigPropertyOptions(ConfigurationItem formModel, PropertyDescriptor property) {
		this(property);
		if (formModel != null) {
			set(DeclarativeFormBuilder.FORM_MODEL, formModel);
		}
	}

	@Override
	public PropertyDescriptor getProperty() {
		return _property;
	}

	@Override
	public AnnotationCustomizations getCustomizations() {
		// No form-local customizations: the annotations on the property itself decide.
		return NO_CUSTOMIZATIONS;
	}

	/**
	 * The options offered for the given property, or an empty list if it has none.
	 *
	 * @param config
	 *        The configuration item holding the property; option functions may depend on its other
	 *        values.
	 * @param property
	 *        The property to resolve options for.
	 */
	public static List<?> optionsFor(ConfigurationItem config, PropertyDescriptor property) {
		DerivedProperty<? extends Iterable<?>> provider = optionProvider(config, property);
		// The item doubles as the form model here: this helper serves a caller that has nothing
		// else, and the item itself is a better answer than none.
		if (provider == null) {
			return Collections.emptyList();
		}
		return toList(provider.get(config));
	}

	/**
	 * Copies the given (possibly {@code null}) {@link Iterable} of options into a {@link List}.
	 *
	 * <p>
	 * Shared by {@link #optionsFor(ConfigurationItem, PropertyDescriptor)} and other callers of
	 * {@link #optionProvider(PropertyDescriptor)} (such as the polymorphic type selector) so the
	 * copy happens in one place.
	 * </p>
	 *
	 * @param options
	 *        The options as produced by a {@link DerivedProperty} obtained from
	 *        {@link #optionProvider(PropertyDescriptor)}, or {@code null}.
	 * @return An empty list for {@code null} input, otherwise the options in iteration order.
	 */
	public static List<Object> toList(Iterable<?> options) {
		if (options == null) {
			return Collections.emptyList();
		}
		List<Object> result = new ArrayList<>();
		for (Object option : options) {
			result.add(option);
		}
		return result;
	}

	/**
	 * The option provider for the given property, or {@code null} if the property has no options
	 * at all.
	 *
	 * <p>
	 * A non-{@code null} result does not mean the property is edited by a plain select: besides an
	 * {@link Options @Options}-driven value list, this also covers the implementation-class list of
	 * a polymorphic item, array, list or map property (see
	 * {@link Fields#optionProvider(DeclarativeFormOptions)}) — those are edited through a dedicated
	 * type selector, never through a plain select. A caller that must tell the two cases apart
	 * needs to look at what {@link Fields#optionMapping(DerivedProperty)} returns for the provider,
	 * not just whether this method returned non-{@code null}.
	 * </p>
	 */
	public static DerivedProperty<? extends Iterable<?>> optionProvider(PropertyDescriptor property) {
		return optionProvider(null, property);
	}

	/**
	 * The option provider for the given property of the given item.
	 *
	 * <p>
	 * To be preferred over {@link #optionProvider(PropertyDescriptor)} wherever the form model is at
	 * hand: the provider and its mapping are built here, and a mapping that needs the surrounding
	 * configuration can only find it if the form model was passed.
	 * </p>
	 */
	public static DerivedProperty<? extends Iterable<?>> optionProvider(ConfigurationItem formModel,
			PropertyDescriptor property) {
		return Fields.optionProvider(new ConfigPropertyOptions(formModel, property));
	}

	/**
	 * The {@link LabelProvider} for the options of the given property, or {@code null} for the
	 * default labels.
	 */
	public static LabelProvider optionLabels(PropertyDescriptor property) {
		return optionLabels(null, property);
	}

	/**
	 * The labels for the options of the given property, with the form model an option mapping may
	 * need - see {@link #optionProvider(ConfigurationItem, PropertyDescriptor)}.
	 */
	public static LabelProvider optionLabels(ConfigurationItem formModel, PropertyDescriptor property) {
		return Fields.optionLabelsOrNull(new ConfigPropertyOptions(formModel, property));
	}

}
