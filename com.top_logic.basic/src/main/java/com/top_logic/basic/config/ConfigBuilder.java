/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.annotation.FrameworkInternal;

/**
 * Mutable version of a {@link ConfigurationItem} for reflective construction.
 * 
 * <p>
 * Even if the {@link ConfigurationItem} interface does not declare setter
 * methods, its initial values can be assigned through the creation of the
 * {@link ConfigurationItem} instance through a {@link ConfigBuilder}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ConfigBuilder extends ConfigurationItem {

	/**
	 * Sets the value of the given {@link PropertyDescriptor}.
	 * 
	 * <p>
	 * The same property may be initialized only once. The second call to this
	 * method with the same property results in an {@link IllegalStateException}.
	 * </p>
	 * 
	 * @param property
	 *        the descriptor of the property to set.
	 * @param value
	 *        The value to initialize the property with.
	 */
	public void initValue(PropertyDescriptor property, Object value);

	/**
	 * Creates a (potentially) immutable {@link ConfigurationItem} instance from
	 * the builder.
	 * <p>
	 * <b>The caller has to check the {@link InstantiationContext} for errors</b>, as they are not
	 * thrown but reported there. The returned {@link ConfigurationItem} might be in an inconsistent
	 * state if the {@link InstantiationContext#hasErrors()}.
	 * </p>
	 * <p>
	 * Whether the returned {@link ConfigurationItem} instance is immutable
	 * depends on the {@link ConfigurationItem} interface defining setter
	 * methods or not.
	 * </p>
	 * 
	 * @param instantiationContext
	 *        For instantiating inner configurations and for error reporting. <b>Has to be checked
	 *        for errors by the caller.</b>
	 * 
	 * @return The {@link ConfigurationItem} with values initialized from the
	 *         values set to this builder.
	 */
	public ConfigurationItem createConfig(InstantiationContext instantiationContext);

	/**
	 * Initializes the location from which this configuration was read.
	 * 
	 * @param file The configuration file.
	 * @param line The line number of the start tag.
	 * @param column The column number of the start tag. 
	 */
	default void initLocation(String file, int line, int column) {
		initLocation(LocationImpl.location(file, line, column));
	}

	/**
	 * Initializes the location from which this configuration was read.
	 * 
	 * @param location
	 *        The source location.
	 */
	public void initLocation(Location location);

	/**
	 * Disable the pre-instantiation checks, especially whether mandatory properties are set.
	 */
	@FrameworkInternal
	void disableChecks();

	void resetValueSet(PropertyDescriptor property);

}