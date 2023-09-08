/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.customization;

import java.lang.annotation.Annotation;
import java.util.Map;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Configured {@link AnnotationCustomizations}.
 * 
 * @see ConfiguredAnnotationCustomizations
 */
@Abstract
public interface CustomizationContainer extends ConfigurationItem {

	/**
	 * @see #getCustomizations()
	 */
	String CUSTOMIZATIONS = "customizations";

	/**
	 * Collection of {@link CustomizationConfig}s indexed by the target of the customization.
	 */
	@Name(CUSTOMIZATIONS)
	@Key(CustomizationConfig.NAME_ATTRIBUTE)
	Map<String, CustomizationContainer.CustomizationConfig> getCustomizations();

	/**
	 * Base interface for a customization.
	 * 
	 * <p>
	 * The target of the customization is refered to by the {@link #getName()} attribute.
	 * </p>
	 */
	@Abstract
	interface CustomizationConfig extends NamedConfigMandatory {

		<R, A> R visit(CustomizationConfig.CustomizationVisitor<R, A> v, A arg);

		interface CustomizationVisitor<R, A> {
			R visitType(CustomizationContainer.TypeCustomizationConfig customization, A arg);

			R visitProperty(CustomizationContainer.PropertyCustomizationConfig customization, A arg);
		}
	}

	/**
	 * Customization of the annotations of a {@link Class}.
	 */
	@TagName(TypeCustomizationConfig.TAG_NAME)
	interface TypeCustomizationConfig extends CustomizationContainer.CustomizationConfig, CustomizationContainer.AnnotationContainer {

		/**
		 * Short-cut tag name for a {@link TypeCustomizationConfig}.
		 * 
		 * @see CustomizationContainer#getCustomizations()
		 */
		String TAG_NAME = "type";

		@Override
		@Format(TypeCustomizationConfig.TypeReferenceFormat.class)
		String getName();

		class TypeReferenceFormat extends AbstractConfigurationValueProvider<String> {

			public TypeReferenceFormat() {
				super(String.class);
			}

			@Override
			protected String getValueNonEmpty(String propertyName, CharSequence propertyValue)
					throws ConfigurationException {
				String value = propertyValue.toString();
				parseReference(propertyName, value);

				// Check performed, return unmodified value.
				return value;
			}

			public static Class<?> parseReference(String propertyName, String value)
					throws ConfigurationException {
				return ConfigUtil.getClassForNameMandatory(Object.class, propertyName, value);
			}

			@Override
			protected String getSpecificationNonNull(String configValue) {
				return configValue;
			}

		}
	}

	/**
	 * Customization of the annotations of a {@link PropertyDescriptor typed configuration
	 * property}.
	 */
	@TagName(PropertyCustomizationConfig.TAG_NAME)
	interface PropertyCustomizationConfig extends CustomizationContainer.CustomizationConfig, CustomizationContainer.AnnotationContainer {

		String TAG_NAME = "property";

		@Override
		@Format(PropertyCustomizationConfig.PropertyReferenceFormat.class)
		String getName();

		class PropertyReferenceFormat extends AbstractConfigurationValueProvider<String> {

			public PropertyReferenceFormat() {
				super(String.class);
			}

			@Override
			protected String getValueNonEmpty(String propertyName, CharSequence propertyValue)
					throws ConfigurationException {
				String value = propertyValue.toString();
				parseReference(propertyName, value);

				// Check performed, return unmodified value.
				return value;
			}

			public static PropertyDescriptor parseReference(String propertyName, String value)
					throws ConfigurationException {
				int nameSepIndex = value.indexOf('#');
				if (nameSepIndex < 0) {
					throw new ConfigurationException(
						I18NConstants.ERROR_MISSING_PROPERTY_NAME_SEPARATOR__VALUE.fill(value),
						propertyName, value);
				}
				String targetClassName = value.substring(0, nameSepIndex);
				String targetPropertyName = value.substring(nameSepIndex + 1);

				Class<? extends Object> targetClass =
					ConfigUtil.getClassForNameMandatory(Object.class, propertyName, targetClassName);

				ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(targetClass);

				PropertyDescriptor targetProperty = descriptor.getProperty(targetPropertyName);
				if (targetProperty == null) {
					throw new ConfigurationException(
						I18NConstants.ERROR_NO_SUCH_PROPERTY__TYPE_NAME.fill(targetClassName,
							targetPropertyName),
						propertyName, value);
				}
				return targetProperty;
			}

			@Override
			protected String getSpecificationNonNull(String configValue) {
				return configValue;
			}

		}
	}

	/**
	 * Base for configurations holding {@link Annotation} configurations.
	 */
	@Abstract
	interface AnnotationContainer extends ConfigurationItem {

		/**
		 * @see #getAnnotations()
		 */
		String ANNOTATIONS = "annotations";

		/**
		 * {@link Annotation}s indexed by their {@link Annotation#annotationType() class}.
		 */
		@Name(ANNOTATIONS)
		@Key(ConfigurationItem.CONFIGURATION_INTERFACE_NAME)
		Map<Class<? extends Annotation>, Annotation> getAnnotations();

	}

}
