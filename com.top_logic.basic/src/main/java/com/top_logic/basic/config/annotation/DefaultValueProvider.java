/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.annotation;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;

/**
 * Provider creating a default value for a {@link PropertyDescriptor}.
 * 
 * @see ComplexDefault
 * @see PropertyDescriptor#getDefaultValue()
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class DefaultValueProvider {

	/**
	 * Computes the default value for the given property of the given configuration type.
	 */
	public abstract Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName);
	
	/**
	 * Whether the created default of this {@link DefaultValueProvider} can be safely shared between
	 * arbitrary configuration instances.
	 * 
	 * <p>
	 * Note: This method should only be overridden to return <code>true</code>, if a singleton is
	 * returned.
	 * </p>
	 * 
	 * <p>
	 * Instead of overriding this method with a constant <code>true</code> return value,
	 * {@link DefaultValueProviderShared} should be used.
	 * </p>
	 * 
	 * @param property
	 *        The {@link PropertyDescriptor} for which a default is created.
	 * 
	 * @see DefaultValueProviderShared
	 */
	public boolean isShared(PropertyDescriptor property) {
		// Since there is an explicit algorithm for computing the value, it is not guaranteed that
		// this resolves to the same value, each time it is executed (no matter if the type of the
		// property is primitive or not).
		return false;
	}

}
