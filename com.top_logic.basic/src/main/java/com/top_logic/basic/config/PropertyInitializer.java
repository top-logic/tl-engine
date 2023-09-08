/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.config.annotation.ValueInitializer;

/**
 * Algorithm computing an initial value for a {@link PropertyDescriptor property}.
 * 
 * <p>
 * In contrast to the default value of a property, the initial value is stored into a configuration
 * file, if the configuration is serialized. After loading the configuration, no new value is
 * computed, but the initially computed value is kept.
 * </p>
 * 
 * <p>
 * A {@link PropertyInitializer} can be used to e.g. to automatically create unique IDs.
 * </p>
 * 
 * @see ValueInitializer
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface PropertyInitializer {

	/**
	 * Computes an initial value for the given property.
	 */
	Object getInitialValue(PropertyDescriptor property);

}
