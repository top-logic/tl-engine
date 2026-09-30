/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.annotation.EnumDefaultValue;

/**
 * Whether a table allows a single or multiple selected rows.
 *
 * @implNote The {@link #getExternalName() external name} is both the value written in a
 *           configuration and the value sent to the client.
 */
public enum SelectionMode implements ExternallyNamed {

	/** At most one row may be selected. */
	SINGLE("single"),

	/** Any number of rows may be selected. */
	MULTI("multi");

	/**
	 * Default value provider answering {@link SelectionMode#SINGLE}, for a configuration property
	 * of a display whose rows are selected one at a time unless it states otherwise.
	 */
	public static class SingleDefault extends EnumDefaultValue {
		@Override
		public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
			return SINGLE;
		}
	}

	private final String _externalName;

	SelectionMode(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}
