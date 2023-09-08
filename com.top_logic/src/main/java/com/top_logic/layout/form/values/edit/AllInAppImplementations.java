/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import static com.top_logic.layout.form.values.edit.AllImplementationsWithI18N.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.basic.func.Function0;
import com.top_logic.layout.form.model.utility.DefaultListOptionModel;
import com.top_logic.layout.form.model.utility.ListOptionModel;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.ImplOptionMapping;
import com.top_logic.layout.form.values.ItemOptionMapping;
import com.top_logic.layout.form.values.edit.annotation.AcceptableClassifiers;

/**
 * All {@link InApp} implementations.
 * 
 * <p>
 * Filtering implementations by {@link InApp#classifiers() classifiers} can be done in subclasses by
 * overriding {@link #acceptableImplementations()}, or by using {@link InAppImplementations} in
 * combination with an {@link AcceptableClassifiers} annotation.
 * </p>
 * 
 * @see InAppImplementations
 * @see InApp
 * @see AcceptableClassifiers
 * @see ClassifiedInAppImplementations
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AllInAppImplementations extends Function0<ListOptionModel<Class<?>>> implements OptionMappingProvider {

	private final boolean _instance;

	private final Class<?> _type;

	private AnnotationCustomizations _customizations;

	private final OptionMapping _mapping;

	/**
	 * Creates {@link AllInAppImplementations} for the given type class object.
	 */
	@CalledByReflection
	public AllInAppImplementations(DeclarativeFormOptions options) {
		_customizations = options.getCustomizations();
		PropertyDescriptor property = options.getProperty();
		Class<?> instanceType = property.getInstanceType();
		_instance = instanceType != null;
		if (_instance) {
			_type = instanceType;
			_mapping = ImplOptionMapping.INSTANCE;
		} else {
			_type = property.getElementType();
			_mapping = ItemOptionMapping.INSTANCE;
		}
		assert _type != null : "Property '" + property + "' has neither instance type nor element type.";
	}

	@Override
	public ListOptionModel<Class<?>> apply() {
		List<? extends Class<?>> list = acceptableImplementations().collect(Collectors.toList());

		return new PrioritizedListOptionModel(_customizations, list);
	}

	/**
	 * All implementation classes that can be chosen.
	 */
	protected Stream<? extends Class<?>> acceptableImplementations() {
		return allSubTypes(_instance, _type).filter(this::isInApp);
	}

	private boolean isInApp(Class<?> type) {
		InApp inapp = annotation(type);
		return inapp != null && inapp.value();
	}

	@Override
	public OptionMapping getOptionMapping() {
		return _mapping;
	}

	private InApp annotation(Class<?> type) {
		return _customizations.getAnnotation(type, InApp.class);
	}

	/**
	 * {@link ListOptionModel} that selects the option as default value that has the maximum
	 * {@link InApp#priority()}.
	 */
	public static class PrioritizedListOptionModel extends DefaultListOptionModel<Class<?>> {
		private AnnotationCustomizations _customizations;

		/**
		 * Creates a {@link PrioritizedListOptionModel}.
		 */
		public PrioritizedListOptionModel(AnnotationCustomizations customizations, List<? extends Class<?>> options) {
			super(options);
			_customizations = customizations;
		}

		@Override
		public Class<?> getDefaultValue() {
			return getBaseModel().stream().max(this::maxPriority).orElse(null);
		}

		int maxPriority(Class<?> class1, Class<?> class2) {
			return Integer.compare(priority(class1), priority(class2));
		}

		private int priority(Class<?> clazz) {
			InApp inapp = annotation(clazz);
			return inapp == null ? -1000 : inapp.priority();
		}

		private InApp annotation(Class<?> type) {
			return _customizations.getAnnotation(type, InApp.class);
		}
	}

}
