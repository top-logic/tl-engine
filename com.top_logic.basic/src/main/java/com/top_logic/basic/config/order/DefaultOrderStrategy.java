/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.order;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;

/**
 * Default {@link OrderStrategy} respecting {@link DisplayOrder} annotations.
 * 
 * @see DisplayOrder
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultOrderStrategy implements OrderStrategy {

	private AnnotationCustomizations _customizations;

	/**
	 * Creates a {@link DefaultOrderStrategy}.
	 *
	 * @param customizations
	 *        The active customizations.
	 */
	public DefaultOrderStrategy(AnnotationCustomizations customizations) {
		_customizations = customizations;
	}

	@Override
	public List<PropertyDescriptor> getDisplayProperties(ConfigurationDescriptor descriptor) {
		return new Collector(_customizations, descriptor).collect();
	}

	/**
	 * Helper class to collect the {@link PropertyDescriptor} for a given
	 * {@link ConfigurationDescriptor}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class Collector {

		private final Map<NamedConstant, PropertyDescriptor> _ignore = new HashMap<>();

		private final ConfigurationDescriptor _topDescriptor;

		private AnnotationCustomizations _customizations;

		/**
		 * Creates a new {@link Collector}.
		 * 
		 * @param customizations
		 *        Determination of the {@link Annotation} of a {@link PropertyDescriptor}.
		 * @param descriptor
		 *        The {@link ConfigurationDescriptor} to collect {@link PropertyDescriptor}s for.
		 */
		public Collector(AnnotationCustomizations customizations, ConfigurationDescriptor descriptor) {
			_customizations = customizations;
			_topDescriptor = descriptor;
		}

		/**
		 * Collects the {@link PropertyDescriptor}s for the descriptor
		 */
		public List<PropertyDescriptor> collect() {
			return internalCollect(_topDescriptor);
		}

		private List<PropertyDescriptor> internalCollect(ConfigurationDescriptor descriptor) {
			DisplayOrder orderAnnotation =
				_customizations.getAnnotation(descriptor, DisplayOrder.class);
			if (orderAnnotation == null) {
				return collectDefaultDisplayProperties(descriptor);
			} else {
				return collectAnnotatedDisplayProperties(descriptor, orderAnnotation);
			}
		}

		private List<PropertyDescriptor> collectAnnotatedDisplayProperties(ConfigurationDescriptor descriptor,
				DisplayOrder orderAnnotation) {
			List<PropertyDescriptor> result = null;

			for (String propertyName : orderAnnotation.value()) {
				PropertyDescriptor property = getProperty(propertyName);
				if (property == null) {
					throw new ConfigurationError(
						I18NConstants.INVALID_DISPLAYORDER_NO_SUCH_PROPERTY__INTERFACE__PROPERTY
							.fill(descriptor.getConfigurationInterface().getName(), propertyName));
				}

				if (!addIgnore(property)) {
					continue;
				}

				result = allocateAndAdd(result, property);
			}

			result = collectInheritedDisplayProperties(result, descriptor);

			result = collectNotDisplayedProperties(result, descriptor);

			return nonNull(result);
		}

		private boolean addIgnore(PropertyDescriptor property) {
			return _ignore.put(property.identifier(), property) == null;
		}

		private List<PropertyDescriptor> collectNotDisplayedProperties(List<PropertyDescriptor> result,
				ConfigurationDescriptor descriptor) {
			if (!collectUnorderedProperties()) {
				return result;
			}
			List<PropertyDescriptor> unorderedDescriptors = null;
			for (PropertyDescriptor localProperty : descriptor.getPropertiesOrdered()) {
				if (localProperty.isInherited()) {
					continue;
				}
				PropertyDescriptor property = getProperty(localProperty.getPropertyName());

				if (!addIgnore(property)) {
					continue;
				}
				unorderedDescriptors = allocateAndAdd(unorderedDescriptors, property);
			}
			if (unorderedDescriptors == null) {
				return result;
			}

			if (result == null) {
				result = new ArrayList<>();
			}
			result.addAll(unorderedDescriptors);
			return result;
		}

		/**
		 * If the annotation {@link DisplayOrder} is given, only those properties given in the
		 * annotation are collected. The result of this method determines whether the properties
		 * that are <b>not</b> given in the {@link DisplayOrder} annotation must be collected.
		 */
		protected boolean collectUnorderedProperties() {
			return false;
		}

		private List<PropertyDescriptor> collectInheritedDisplayProperties(List<PropertyDescriptor> result,
				ConfigurationDescriptor descriptor) {
			DisplayStrategy displayStrategy = getDisplayStrategy(descriptor);
			switch (displayStrategy) {
				case APPEND: {
					ConfigurationDescriptor[] superDescriptors = descriptor.getSuperDescriptors();
					for (ConfigurationDescriptor superDescriptor : superDescriptors) {
						result = addProperties(result, false, superDescriptor);
					}
					break;
				}
				case PREPEND: {
					ConfigurationDescriptor[] superDescriptors = descriptor.getSuperDescriptors();
					for (int n = superDescriptors.length - 1; n >= 0; n--) {
						ConfigurationDescriptor superDescriptor = superDescriptors[n];
						result = addProperties(result, true, superDescriptor);
					}
					break;
				}

				case IGNORE:
					break;
			}
			return result;
		}

		private List<PropertyDescriptor> addProperties(List<PropertyDescriptor> result, boolean prepend,
				ConfigurationDescriptor superDescriptor) {
			List<PropertyDescriptor> superProperties = internalCollect(superDescriptor);
			if (!superProperties.isEmpty()) {
				if (result == null) {
					result = superProperties;
				} else {
					int index = prepend ? 0 : result.size();
					result.addAll(index, superProperties);
				}
			}
			return result;
		}

		/**
		 * Calculates the {@link DisplayStrategy}.
		 * 
		 * @return The local {@link DisplayStrategy}. If none is annotated, the default display
		 *         strategy is taken.
		 */
		protected DisplayStrategy getDisplayStrategy(ConfigurationDescriptor descriptor) {
			DisplayStrategy result = getDisplayStrategyOptional(descriptor);

			return result == null ? defaultDisplayStrategy() : result;
		}

		/**
		 * Hook to statically define {@link #getDisplayStrategy(ConfigurationDescriptor)}.
		 * 
		 * @return A {@link DisplayStrategy} which is default for this {@link DefaultOrderStrategy}.
		 *         Default is {@link DisplayStrategy#PREPEND}.
		 */
		protected final DisplayStrategy defaultDisplayStrategy() {
			return DisplayStrategy.PREPEND;
		}

		private DisplayStrategy getDisplayStrategyOptional(ConfigurationDescriptor descriptor) {
			DisplayStrategy result = getLocalDisplayStrategyAnnotation(descriptor);
			if (result != null) {
				return result;
			}

			return getSuperDisplayStrategy(descriptor);
		}

		private DisplayStrategy getSuperDisplayStrategy(ConfigurationDescriptor descriptor) {
			for (ConfigurationDescriptor superDescriptor : descriptor.getSuperDescriptors()) {
				DisplayStrategy superStrategy = getLocalDisplayStrategyAnnotation(superDescriptor);
				if (superStrategy != null && superStrategy != DisplayStrategy.IGNORE) {
					// It makes no sense to inherit the ignore strategy.
					return superStrategy;
				}

				DisplayStrategy inheritedSuperStrategy = getSuperDisplayStrategy(superDescriptor);
				if (inheritedSuperStrategy != null) {
					return inheritedSuperStrategy;
				}
			}

			return null;
		}

		private DisplayStrategy getLocalDisplayStrategyAnnotation(ConfigurationDescriptor descriptor) {
			DisplayInherited annotation =
				_customizations.getAnnotation(descriptor, DisplayInherited.class);
			if (annotation == null) {
				return null;
			}
			return annotation.value();
		}

		private List<PropertyDescriptor> collectDefaultDisplayProperties(ConfigurationDescriptor descriptor) {
			List<PropertyDescriptor> result = null;

			for (PropertyDescriptor localProperty : descriptor.getPropertiesOrdered()) {
				if (localProperty.isInherited()) {
					continue;
				}
				PropertyDescriptor property = getProperty(localProperty.getPropertyName());

				if (!addIgnore(property)) {
					continue;
				}

				if (isHidden(property)) {
					continue;
				}

				result = allocateAndAdd(result, property);
			}

			result = collectInheritedDisplayProperties(result, descriptor);

			return nonNull(result);
		}

		private PropertyDescriptor getProperty(String propertyName) {
			return _topDescriptor.getProperty(propertyName);
		}

		private static <T> List<T> allocateAndAdd(List<T> result, T property) {
			if (result == null) {
				result = new ArrayList<>();
			}
			result.add(property);
			return result;
		}

		private static <T> List<T> nonNull(List<T> result) {
			return result == null ? Collections.<T> emptyList() : result;
		}

		/**
		 * Determines whether the given property is {@link Hidden}.
		 */
		protected boolean isHidden(PropertyDescriptor property) {
			Hidden annotation = _customizations.getAnnotation(property, Hidden.class);
			if (annotation == null) {
				return false;
			}
			return annotation.value();
		}

	}

}
