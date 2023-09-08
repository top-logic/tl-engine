/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

/**
 * Helper for finding the configuration interface for a concrete implementation
 * class.
 * 
 * <p>
 * Implementations represent a coding convention that allow to guess the
 * configuration interface for a given implementation class.
 * </p>
 * 
 * @see PolymorphicConfiguration#getImplementationClass() The reverse direction pointing from
 *      the configuration item to the concrete implementation class that is
 *      configured.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ConfigurationResolver {

	/**
	 * Resolves the concrete {@link ConfigurationDescriptor} for a given
	 * implementation class.
	 * 
	 * @param implClass
	 *        The concrete implementation class for which a
	 *        {@link ConfigurationDescriptor} is requested.
	 * @return A {@link ConfigurationDescriptor} describing the configuration of
	 *         a concrete impementation class.
	 * @throws ConfigurationException
	 *         If the given class is not a class with a typed configuration.
	 */
	ConfigurationDescriptor getConfigurationDescriptor(Class implClass) throws ConfigurationException;

}
