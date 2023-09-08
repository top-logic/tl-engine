/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.annotation.Annotation;
import java.util.Collection;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Id;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.basic.config.internal.ItemFactory;
import com.top_logic.basic.util.ResKey;

/**
 * Type description of a {@link ConfigurationItem}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ConfigurationDescriptor {

	/** Empty {@link ConfigurationDescriptor} array. */
	ConfigurationDescriptor[] NO_CONFIGURATION_DESCRIPTORS = {};

	/**
	 * {@link ConfigurationDescriptor}s which this descriptor extends.
	 * 
	 * @return Not <code>null</code>, but may be empty.
	 */
	ConfigurationDescriptor[] getSuperDescriptors();

	/**
	 * @param other
	 *        Is not allowed to be null.
	 */
	default boolean isSuperDescriptorOf(ConfigurationDescriptor other) {
		return getConfigurationInterface().isAssignableFrom(other.getConfigurationInterface());
	}

	/**
	 * @param other
	 *        Is not allowed to be null.
	 */
	default boolean isSubDescriptorOf(ConfigurationDescriptor other) {
		return other.getConfigurationInterface().isAssignableFrom(getConfigurationInterface());
	}

	Class<?> getConfigurationInterface();

	/**
	 * Looks up an annotation of the given annotation type for this configuration type.
	 *
	 * @param customizations
	 *        {@link AnnotationCustomizations} that may override the declared annotations from the
	 *        Java type.
	 * @param annotationType
	 *        The annotation type requested.
	 * @return The annotation of the given annotation type, or <code>null</code>, if no such
	 *         annotation is present on this descriptor.
	 */
	default <T extends Annotation> T getAnnotation(AnnotationCustomizations customizations, Class<T> annotationType) {
		return customizations.getAnnotation(getConfigurationInterface(), annotationType);
	}

	/**
	 * Returns all known {@link PropertyDescriptor} in some (potentially unstable) order.
	 * <p>
	 * The returned {@link Collection} must not be changed.
	 * </p>
	 * 
	 * @return never <code>null</code>
	 * 
	 * @see #getPropertiesOrdered() List of all properties in a stable order.
	 */
	Collection<? extends PropertyDescriptor> getProperties();

	/**
	 * Returns all known {@link PropertyDescriptor} in a stable order.
	 * 
	 * @return never <code>null</code>.
	 * 
	 * @see PropertyDescriptor#BY_NAME_COMPARATOR For a comparison be name.
	 */
	PropertyDescriptor[] getPropertiesOrdered();

	/**
	 * The {@link PropertyDescriptor} whose contents can be configured directly in the contents of
	 * this item without a container tag.
	 * 
	 * @see DefaultContainer
	 * 
	 * @return May be <code>null</code> when no such property exists.
	 */
	PropertyDescriptor getDefaultContainer();

	/**
	 * Checks whether there is a {@link PropertyDescriptor} with the given name.
	 * If result is <code>true</code> then {@link #getProperty(String)} results
	 * in a non <code>null</code> {@link PropertyDescriptor}.
	 * 
	 * @param propertyName
	 *        the name of the desired property
	 * @return <code>true</code> iff there is a {@link PropertyDescriptor} with
	 *         the given name
	 * 
	 * @see #getProperty(String)
	 */
	default boolean hasProperty(String propertyName) {
		return getProperty(propertyName) != null;
	}

	/**
	 * Lookup the property of this descriptor with the given name.
	 * 
	 * @param name
	 *        The {@link PropertyDescriptor#getPropertyName()} of the searched
	 *        property.
	 * @return The property with the given name, or <code>null</code>, if such
	 *         property does not exist.
	 * 
	 * @see #hasProperty(String)
	 */
	PropertyDescriptor getProperty(String name);

	/**
	 * The {@link ItemFactory} that allocates instances for this descriptor.
	 */
	ItemFactory factory();

	/**
	 * Checks the given {@link ConfigurationItem} for constraint violations.
	 * 
	 * @param protocol
	 *        The {@link Log} to report errors to.
	 * @param item
	 *        The item to check.
	 * 
	 * @see ConfigurationItem#check(Log)
	 */
	void check(Log protocol, ConfigurationItem item);

	/**
	 * Whether this {@link ConfigurationDescriptor} cannot be instantiated because it (potentially)
	 * has {@link Abstract} methods.
	 */
	boolean isAbstract();

	/**
	 * The {@link PropertyDescriptor} whose contents is used to uniquely identify instances created
	 * form this configuration.
	 * 
	 * @see Id
	 */
	PropertyDescriptor getIdProperty();

	/**
	 * The scope of {@link #getIdProperty()}.
	 * 
	 * @see Id#value()
	 */
	Class<?> getIdScope();

	/**
	 * Returns a {@link ResKey label} for the property with the given name.
	 * 
	 * @param property
	 *        The name of the {@link PropertyDescriptor} to get label for.
	 * @param keySuffix
	 *        An additional suffix for the internal form of the result key. May be
	 *        <code>null</code>.
	 * 
	 * @return A {@link ResKey} which can be used as label for the property with the given name.
	 */
	default ResKey getPropertyLabel(String property, String keySuffix) {
		Class<?> concreteType = getConfigurationInterface();
		StringBuilder key = new StringBuilder(concreteType.getName().replace('$', '.'));
		key.append('.');
		key.append(property);
		if (keySuffix != null) {
			key.append(keySuffix);
		}
		return ResKey.legacy(key.toString());
	}

}
