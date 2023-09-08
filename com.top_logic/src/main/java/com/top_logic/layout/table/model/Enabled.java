/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.annotation.EnumDefaultValue;

/**
 * Three-state decision whether to enable a feature.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum Enabled {
	/** Disable the feature. */
	never,

	/** Decide dynamically, whether enabling the feature makes sense. */
	auto,

	/** Enable the feature. */
	always;

	public static class AutoDefaultValueProvider extends EnumDefaultValue {
		@Override
		public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
			return auto;
		}
	}
}