/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.config.format.BuiltInFormats;

/**
 * Algorithm to resolve a default value for a {@link PropertyDescriptor}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
abstract class DefaultSpec {

	/**
	 * Resolves the default for the given {@link PropertyDescriptor}.
	 */
	public abstract Object getDefaultValue(PropertyDescriptor property) throws ConfigurationException;

	/**
	 * Whether the provided default value can be shared between arbitrary configuration items.
	 */
	public boolean isShared(PropertyDescriptor property) {
		return BuiltInFormats.isPrimitive(property.getType());
	}

}
