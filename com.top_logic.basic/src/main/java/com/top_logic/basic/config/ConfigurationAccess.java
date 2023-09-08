/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

/**
 * Algorithm to access the configuration of a property that allows sub-configuration.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ConfigurationAccess {

	/**
	 * Look up the configuration of the given value.
	 */
	ConfigurationItem getConfig(Object value);

	/**
	 * Create a value from the given configuration.
	 */
	Object createValue(InstantiationContext context, ConfigurationItem newConfig);

}
