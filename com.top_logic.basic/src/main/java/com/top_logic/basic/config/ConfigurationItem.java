/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.annotation.Annotation;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.misc.TypedConfigUtil;

/**
 * Root interface for configuration interfaces.
 * 
 * <p>
 * Defines generic access to the typed configuration item.
 * </p>
 * 
 * @see <a href="http://tl/trac/wiki/TypedConfiguration">http://tl/trac/wiki/TypedConfiguration</a>
 * @see TypedConfiguration
 * @see TypedConfiguration#newConfigItem(Class)
 * @see TypedConfigUtil
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ConfigurationItem {

	/** Name of the immutable property {@link ConfigurationItem#getConfigurationInterface()}. */
	String CONFIGURATION_INTERFACE_NAME = "configuration-interface";

	/**
	 * Name of the {@link Annotation#annotationType()} property of an {@link Annotation} item.
	 */
	String ANNOTATION_TYPE = "annotation-type";

	/**
	 * The descriptor of this configuration item.
	 * 
	 * <p>
	 * Note: This method must not start with the "get" prefix to prevent
	 * confusion with configuration bean properties.
	 * </p>
	 */
	ConfigurationDescriptor descriptor();
	
	/**
	 * Looks up the value of the given property. 
	 * 
	 * <p>
	 * Note: This method must not start with the "get" prefix to prevent
	 * confusion with configuration bean properties.
	 * </p>
	 * 
	 * @param property
	 *        must be known by {@link #descriptor()} or some of its
	 *        {@link ConfigurationDescriptor#getSuperDescriptors()}
	 */
	Object value(PropertyDescriptor property);

	/**
	 * Whether this item got on creation an explicit value for the given property.
	 * 
	 * This can be called also for {@link PropertyDescriptor} which are not known by the
	 * {@link #descriptor()}. In this case <code>false</code> is returned. This can be done to avoid
	 * first asking whether a property exists before checking whether a value was set.
	 * 
	 * @param property
	 *        the property to check.
	 */
	boolean valueSet(PropertyDescriptor property);
	
	/**
	 * Updates the value of the given property with the given value.
	 * 
	 * @param property
	 *        must be known by {@link #descriptor()} or some of its
	 *        {@link ConfigurationDescriptor#getSuperDescriptors()}
	 * @param value
	 *        the new value
	 * 
	 * @return the old value or <code>null</code> if none was set before. This is the same semantics
	 *         as for the old value in
	 *         {@link ConfigurationListener#onChange(ConfigurationChange)}
	 *         .
	 */
	Object update(PropertyDescriptor property, Object value);

	/**
	 * Resets the value of this property.
	 * <p>
	 * After this call, the property will behave as if it has never been set.
	 * </p>
	 * 
	 * @param property
	 *        Is not allowed to be null. Must be known by {@link #descriptor()} or some of its
	 *        {@link ConfigurationDescriptor#getSuperDescriptors()}
	 */
	void reset(PropertyDescriptor property);

	/**
	 * Description of the resource, this {@link ConfigurationItem} was read from.
	 */
	Location location();

	/**
	 * Returns the configuration interface which is implemented by this configuration item.
	 * 
	 * <p>
	 * Note: This method is just declared be able to get an {@link PropertyDescriptor} for
	 * {@link #CONFIGURATION_INTERFACE_NAME} via the general mechanism.
	 * </p>
	 * 
	 * @see #descriptor()
	 */
	@Name(CONFIGURATION_INTERFACE_NAME)
	@Hidden
	Class<?> getConfigurationInterface();

	/**
	 * Adds the given {@link ConfigurationListener} to this configuration.
	 * 
	 * @param property
	 *        The property to observe. <code>null</code> for all properties.
	 * @param listener
	 *        The {@link ConfigurationListener} to notify upon changes.
	 * @return Whether the listener was actually added. <code>false</code> if the listener was
	 *         already registered, or the given {@link PropertyDescriptor} does not belong to this
	 *         configuration.
	 */
	boolean addConfigurationListener(PropertyDescriptor property, ConfigurationListener listener);

	/**
	 * Removes a {@link ConfigurationListener} from this configuration.
	 * 
	 * @param property
	 *        The property that was observed. <code>null</code> for all properties.
	 * @param listener
	 *        The {@link ConfigurationListener} to remove.
	 * @return Whether the listener was actually removed. <code>false</code>, if the listener was
	 *         not registered.
	 */
	boolean removeConfigurationListener(PropertyDescriptor property, ConfigurationListener listener);

	/**
	 * Checks the constraints, for example whether {@link Mandatory} properties are set.
	 */
	void check(Log protocol);

	/**
	 * Prevent implementations outside of this package.
	 */
	Unimplementable unimplementable();

}
