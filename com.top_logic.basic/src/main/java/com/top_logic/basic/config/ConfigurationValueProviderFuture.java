/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.Protocol;

/**
 * A provider for {@link ConfigurationValueProvider} if further informations
 * from a {@link PropertyDescriptor} are needed to construct it.
 * 
 * @see ConfigurationValueBindingFuture
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ConfigurationValueProviderFuture {

	/**
	 * Creates a {@link ConfigurationValueProvider} for the given
	 * {@link PropertyDescriptor}
	 * 
	 * @param protocol
	 *        {@link Protocol} to publish information
	 * @param property
	 *        the property to create binding for
	 * 
	 * @return {@link ConfigurationValueProvider} given in
	 *         {@link PropertyDescriptor#getValueProvider()}
	 */
	ConfigurationValueProvider<?> resolveFor(Protocol protocol, PropertyDescriptor property);

}
