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
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.basic.config.customization.ConfiguredAnnotationCustomizations;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.DerivedProperty;
import com.top_logic.layout.form.values.Fields;

/**
 * {@link DeclarativeFormOptions} over a single configuration property.
 *
 * <p>
 * Exists so that the option resolution of the classic declarative form
 * ({@link Fields#optionProvider(DeclarativeFormOptions)}) can be reused: it resolves the
 * {@link com.top_logic.layout.form.values.edit.annotation.Options} annotation including its
 * function, mapping and argument references, and the implementation lists of polymorphic
 * properties.
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
		DerivedProperty<? extends Iterable<?>> provider = optionProvider(property);
		if (provider == null) {
			return Collections.emptyList();
		}
		Iterable<?> options = provider.get(config);
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
	 * The option provider for the given property, or {@code null} if it is not edited by selecting.
	 */
	public static DerivedProperty<? extends Iterable<?>> optionProvider(PropertyDescriptor property) {
		return Fields.optionProvider(new ConfigPropertyOptions(property));
	}

	/**
	 * The {@link LabelProvider} for the options of the given property, or {@code null} for the
	 * default labels.
	 */
	public static LabelProvider optionLabels(PropertyDescriptor property) {
		return Fields.optionLabelsOrNull(new ConfigPropertyOptions(property));
	}

}
