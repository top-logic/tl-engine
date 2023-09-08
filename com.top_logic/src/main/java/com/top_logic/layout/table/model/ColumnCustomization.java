/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.annotation.EnumDefaultValue;

/**
 * How table columns can be customized by the user.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum ColumnCustomization implements ExternallyNamed {

	/**
	 * Columns cannot be customized by the user.
	 */
	NONE("none"),

	/**
	 * The order of columns can be customized.
	 */
	ORDER("order"),

	/**
	 * Columns can be selected and unselected. This includes the customization {@link #ORDER}.
	 */
	SELECTION("selection");

	private final String _externalName;

	private ColumnCustomization(String externalName) {
		_externalName = externalName;
	}
	
	@Override
	public String getExternalName() {
		return _externalName;
	}

	public static class SelectionDefaultValueProvider extends EnumDefaultValue {
		@Override
		public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
			return SELECTION;
		}
	}
}
