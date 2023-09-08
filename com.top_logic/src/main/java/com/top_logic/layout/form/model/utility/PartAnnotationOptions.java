/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.basic.func.Function3;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.edit.AllInAppImplementations.PrioritizedListOptionModel;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;

/**
 * Option provider function for retrieving options of a certain class suitable for the given
 * attribute type.
 * 
 * <p>
 * This function can be used for configuration properties that have either a
 * {@link ConfigurationItem} as value or an instance value.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PartAnnotationOptions extends Function3<OptionModel<Class<?>>, TLType, TLTypeKind, Boolean> {

	private AnnotationCustomizations _customizations;

	/**
	 * Creates a {@link PartAnnotationOptions}.
	 */
	@CalledByReflection
	public PartAnnotationOptions(DeclarativeFormOptions options) {
		_customizations = options.getCustomizations();
	}

	@Override
	public OptionModel<Class<?>> apply(TLType type, TLTypeKind kind, Boolean isOverride) {
		if (type == null) {
			return new DefaultListOptionModel<>(Collections.emptyList());
		} else {
			return createAnnotationOptions(type, kind, isOverride);
		}
	}

	private OptionModel<Class<?>> createAnnotationOptions(TLType type, TLTypeKind kind, Boolean isOverride) {
		Predicate<Class<?>> optionsFilter = createOptionsFilter(type, kind, isOverride);

		return PartAnnotationOptions.findOptions(_customizations, TLAttributeAnnotation.class, optionsFilter);
	}

	private Predicate<Class<?>> createOptionsFilter(TLType type, TLTypeKind kind, Boolean isOverride) {
		Predicate<Class<?>> filter = new InAppFilter(_customizations);

		if (isOverride) {
			filter = filter.and(new OverrideFilter(_customizations));
		}

		return filter.and(new TargetTypeFilter(_customizations, type, kind));
	}

	/**
	 * Finds all classes extending the given <code>optionType</code> and matched by the given
	 * filter.
	 */
	public static OptionModel<Class<?>> findOptions(AnnotationCustomizations customizations, Class<?> optionType,
			Predicate<Class<?>> filter) {
		boolean isConfiguration = ConfigurationItem.class.isAssignableFrom(optionType);
		Collection<Class<?>> options =
			TypeIndex.getInstance().getSpecializations(optionType, true, isConfiguration, false);
		ArrayList<Class<?>> result = new ArrayList<>();
		for (Class<?> option : options) {
			if (!filter.test(option)) {
				continue;
			}
	
			if (!isConfiguration) {
				// Check class can generally be instantiated
				try {
					DefaultConfigConstructorScheme.getFactory(option);
				} catch (NoClassDefFoundError | ConfigurationException ex) {
					// Ignore problematic class.
					continue;
				}
			}
	
			result.add(option);
		}
		return new PrioritizedListOptionModel(customizations, result);
	}

}
