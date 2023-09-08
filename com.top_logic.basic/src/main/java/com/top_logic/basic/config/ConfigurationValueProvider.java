/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.config.annotation.Format;

/**
 * Provider for complex configuration values that have a simple string syntax for configuration.
 * 
 * <p>
 * Note: This interface is not intended to be implemented directly by clients. Use
 * {@link AbstractConfigurationValueProvider} instead.
 * </p>
 * 
 * @see Format Annotating a {@link ConfigurationValueProvider}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ConfigurationValueProvider<T> extends ConfigurationValueCheck<T>, Unimplementable {

	/**
	 * The class or interface of configuration values this provider constructs
	 * in its {@link #getValue(String, CharSequence)} method.
	 */
	Class<?> getValueType();

	/**
	 * Parses the given value specification and looks up or constructs a corresponding configuration
	 * value.
	 * 
	 * @param propertyName
	 *        The name of the property currently being read.
	 * @param propertyValue
	 *        The serialized configuration value to parse.
	 */
	T getValue(String propertyName, CharSequence propertyValue) throws ConfigurationException;

	/**
	 * Reconstructs the value specification that must be passed to
	 * {@link #getValue(String, CharSequence)} to lookup or create the given object.
	 * 
	 * @param configValue
	 *        The configuration value to serialize, never <code>null</code>.
	 *        
	 * @throws IllegalArgumentException if the given value was not constructed by this provider. 
	 */
	String getSpecification(T configValue);

}
