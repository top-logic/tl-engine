/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.customization;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.customization.CustomizationContainer.CustomizationConfig;
import com.top_logic.basic.config.customization.CustomizationContainer.CustomizationConfig.CustomizationVisitor;
import com.top_logic.basic.config.customization.CustomizationContainer.PropertyCustomizationConfig;
import com.top_logic.basic.config.customization.CustomizationContainer.PropertyCustomizationConfig.PropertyReferenceFormat;
import com.top_logic.basic.config.customization.CustomizationContainer.TypeCustomizationConfig;
import com.top_logic.basic.config.customization.CustomizationContainer.TypeCustomizationConfig.TypeReferenceFormat;

/**
 * {@link AnnotationCustomizations} instantiated from configuration.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfiguredAnnotationCustomizations extends DefaultCustomizations {

	private final Map<Class<?>, TypeCustomization> _customizationsByType;

	/**
	 * Creates a {@link ConfiguredAnnotationCustomizations}.
	 *
	 * @param config
	 *        The customization configuration.
	 */
	public ConfiguredAnnotationCustomizations(CustomizationContainer config) {
		this();
		addCustomizations(config);
	}

	/**
	 * Creates an empty {@link ConfiguredAnnotationCustomizations}.
	 *
	 * <p>
	 * This instance can be populated with customizations later on using e.g.
	 * {@link #addCustomizations(CustomizationContainer)}.
	 * </p>
	 */
	public ConfiguredAnnotationCustomizations() {
		_customizationsByType = new HashMap<>();
	}

	/**
	 * Translates all {@link CustomizationConfig}s in the given {@link CustomizationContainer} and
	 * enters them to this {@link ConfiguredAnnotationCustomizations}.
	 * 
	 * @see #addCustomization(CustomizationContainer.CustomizationConfig)
	 */
	public void addCustomizations(CustomizationContainer config) {
		for (Entry<String, CustomizationConfig> entry : config.getCustomizations().entrySet()) {
			enterCustomization(_customizationsByType, entry.getValue());
		}
	}

	/**
	 * Adds the customization from the given {@link CustomizationConfig}.
	 * 
	 * @see #addCustomizations(CustomizationContainer)
	 */
	public void addCustomization(CustomizationConfig customization) {
		enterCustomization(_customizationsByType, customization);
	}

	private static void enterCustomization(Map<Class<?>, TypeCustomization> result, CustomizationConfig customization) {
		CustomizationAnalyzer.INSTANCE.enterCustomization(result, customization);
	}

	static class CustomizationAnalyzer implements CustomizationVisitor<Void, Map<Class<?>, TypeCustomization>> {

		/**
		 * Singleton {@link CustomizationAnalyzer} instance.
		 */
		public static final CustomizationAnalyzer INSTANCE = new CustomizationAnalyzer();

		private CustomizationAnalyzer() {
			// Singleton constructor.
		}

		/**
		 * Translates the given {@link CustomizationConfig} into a setting in the given result
		 * {@link Map}.
		 *
		 * @param result
		 *        The result to populate.
		 * @param customization
		 *        The {@link CustomizationConfig} to analyze.
		 */
		public void enterCustomization(Map<Class<?>, TypeCustomization> result, CustomizationConfig customization) {
			customization.visit(this, result);
		}

		@Override
		public Void visitType(TypeCustomizationConfig customization, Map<Class<?>, TypeCustomization> arg) {
			try {
				Class<?> configType = TypeReferenceFormat.parseReference(null, customization.getName());
				TypeCustomization typeCustomization = mkTypeCustomization(configType, arg);

				typeCustomization.enter(customization);
				return null;
			} catch (ConfigurationException ex) {
				throw new ConfigurationError(ex);
			}
		}

		@Override
		public Void visitProperty(PropertyCustomizationConfig customization, Map<Class<?>, TypeCustomization> arg) {
			try {
				PropertyDescriptor property = PropertyReferenceFormat.parseReference(null, customization.getName());

				Class<?> configType = property.getDescriptor().getConfigurationInterface();
				TypeCustomization typeCustomization = mkTypeCustomization(configType, arg);

				typeCustomization.enter(property, customization);
				return null;
			} catch (ConfigurationException ex) {
				throw new ConfigurationError(ex);
			}
		}

		private TypeCustomization mkTypeCustomization(Class<?> configType, Map<Class<?>, TypeCustomization> arg) {
			TypeCustomization typeCustomization = arg.get(configType);
			if (typeCustomization == null) {
				typeCustomization = new TypeCustomization(configType);
				arg.put(configType, typeCustomization);
			}
			return typeCustomization;
		}
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<?> type, Class<T> annotationType) {
		TypeCustomization typeCustomization = _customizationsByType.get(type);
		if (typeCustomization != null) {
			return typeCustomization.getAnnotation(annotationType);
		}
		return super.getAnnotation(type, annotationType);
	}

	@Override
	protected <T extends Annotation> T getLocalAnnotation(PropertyDescriptor property, Class<T> annotationType) {
		T result = getDirectAnnotation(property, annotationType);
		if (result != null) {
			return result;
		}
		return findInheritedAnnotation(property, annotationType);
	}

	private <T extends Annotation> T getDirectAnnotation(PropertyDescriptor property, Class<T> annotationType) {
		Class<?> configType = property.getDescriptor().getConfigurationInterface();
		TypeCustomization typeCustomization = _customizationsByType.get(configType);
		if (typeCustomization != null) {
			return typeCustomization.getAnnotation(property, annotationType);
		} else {
			return property.getLocalAnnotation(annotationType);
		}
	}

	private <T extends Annotation> T findInheritedAnnotation(PropertyDescriptor property, Class<T> annotationType) {
		for (PropertyDescriptor superProperty : property.getSuperProperties()) {
			T inheritedAnnotation = getAnnotation(superProperty, annotationType);
			if (inheritedAnnotation != null) {
				return inheritedAnnotation;
			}
		}

		return null;
	}

	static class PropertyCustomization {
	
		private final PropertyDescriptor _property;
	
		private final PropertyCustomizationConfig _customization;
	
		public PropertyCustomization(PropertyDescriptor property, PropertyCustomizationConfig customization) {
			_property = property;
			_customization = customization;
		}
	
		public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
			T customizedAnnotation = getCustomAnnotation(annotationType);
			if (customizedAnnotation != null) {
				return customizedAnnotation;
			}
			return _property.getLocalAnnotation(annotationType);
		}
	
		private <T extends Annotation> T getCustomAnnotation(Class<T> annotationType) {
			@SuppressWarnings("unchecked")
			T result = (T) _customization.getAnnotations().get(annotationType);
			return result;
		}
	
	}

	static class TypeCustomization {
	
		private final Class<?> _configType;
	
		private Map<NamedConstant, PropertyCustomization> _customizationsByProperty = new HashMap<>();
	
		private Map<Class<? extends Annotation>, Annotation> _annotations = Collections.emptyMap();
	
		public TypeCustomization(Class<?> configType) {
			_configType = configType;
		}
	
		public void enter(PropertyDescriptor property, PropertyCustomizationConfig value) {
			_customizationsByProperty.put(property.identifier(), new PropertyCustomization(property, value));
		}
	
		public <T extends Annotation> T getAnnotation(PropertyDescriptor property, Class<T> annotationType) {
			PropertyCustomization customization = _customizationsByProperty.get(property.identifier());
			if (customization != null) {
				return customization.getAnnotation(annotationType);
			}
			return property.getLocalAnnotation(annotationType);
		}
	
		public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
			@SuppressWarnings("unchecked")
			T result = (T) _annotations.get(annotationType);
			if (result != null) {
				return result;
			}
	
			return _configType.getAnnotation(annotationType);
		}
	
		public void enter(TypeCustomizationConfig config) {
			_annotations = config.getAnnotations();
		}
	
	}
}
