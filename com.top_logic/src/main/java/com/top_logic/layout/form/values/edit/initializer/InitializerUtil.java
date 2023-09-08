/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.initializer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyInitializer;
import com.top_logic.basic.config.annotation.ValueInitializer;
import com.top_logic.basic.config.customization.AnnotationCustomizations;

/**
 * Utilities for working with {@link Initializer}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class InitializerUtil {

	/**
	 * Calls the {@link Initializer} for every child {@link ConfigurationItem} in the given model.
	 * <p>
	 * If either of the parameters is null, nothing is done.
	 * </p>
	 */
	public static void initAll(Initializer initializer, ConfigurationItem model) {
		if ((initializer == null) || (model == null)) {
			return;
		}
		for (PropertyDescriptor property : model.descriptor().getProperties()) {
			Collection<ConfigurationItem> children = ConfigUtil.getChildConfigs(model, property);
			for (ConfigurationItem child : children) {
				initializer.init(model, property, child);
			}
		}
	}

	/**
	 * Invokes all annotated {@link PropertyInitializer}s on properties of the given model.
	 */
	public static void initProperties(AnnotationCustomizations customizations, ConfigurationItem model) {
		if (model == null) {
			return;
		}
		new PropertyInitialization(customizations).initialize(model);
	}

	static class PropertyInitialization {

		private AnnotationCustomizations _customizations;

		/**
		 * Creates a {@link PropertyInitialization}.
		 */
		public PropertyInitialization(AnnotationCustomizations customizations) {
			_customizations = customizations;
		}

		public void initialize(ConfigurationItem model) {
			for (PropertyDescriptor property : model.descriptor().getProperties()) {
				if (!property.hasExplicitDefault() && !model.valueSet(property)) {
					PropertyInitializer initializer = initializer(property);
					if (initializer != null) {
						Object initialValue = initializer.getInitialValue(property);
						model.update(property, initialValue);
					}
				}

				if (!property.isInstanceValued()) {
					switch (property.kind()) {
						case ITEM:
						case LIST:
						case ARRAY:
						case MAP:
							for (Object element : toCollection(model.value(property))) {
								initialize((ConfigurationItem) element);
							}
							break;
						default:
							// Ignore.
					}
				}
			}
		}

		private PropertyInitializer initializer(PropertyDescriptor property) {
			ValueInitializer annotation = _customizations.getAnnotation(property, ValueInitializer.class);
			if (annotation != null) {
				try {
					return ConfigUtil.getInstance(annotation.value());
				} catch (ConfigurationException ex) {
					Logger.error("Cannot instantiate property initializer: " + annotation.value().getName(), ex,
						InitializerUtil.class);
				}
			}
			return null;
		}

		private static Collection<?> toCollection(Object value) {
			if (value instanceof Collection<?>) {
				return (Collection<?>) value;
			} else if (value instanceof Map<?, ?>) {
				return ((Map<?, ?>) value).values();
			} else if (value == null) {
				return Collections.emptyList();
			} else if (value.getClass().isArray()) {
				return Arrays.asList((Object[]) value);
			} else {
				return Collections.singletonList(value);
			}
		}
	}

}
