/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.Protocol;

/**
 * A provider for {@link ConfigurationValueBinding} if further informations from
 * a {@link PropertyDescriptor} are needed to construct it.
 * 
 * @see ConfigurationValueProviderFuture
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ConfigurationValueBindingFuture {

	/**
	 * Creates a {@link ConfigurationValueBinding} for the given
	 * {@link PropertyDescriptor}
	 * 
	 * @param protocol
	 *        {@link Protocol} to publish information
	 * @param property
	 *        the property to create binding for
	 * 
	 * @return {@link ConfigurationValueBinding} given in
	 *         {@link PropertyDescriptor#getValueBinding()}
	 */
	ConfigurationValueBinding<?> resolveFor(Protocol protocol, PropertyDescriptorImpl property);

}
