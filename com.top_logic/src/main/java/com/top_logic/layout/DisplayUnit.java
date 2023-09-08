/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * {@link Enum} of units for display.
 * 
 * @see #PIXEL
 * @see #PERCENT
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum DisplayUnit implements ExternallyNamed {

	/**
	 * Pixel (px)
	 */
	PIXEL("px"),
	
	/**
	 * Percent (%)
	 */
	PERCENT("%");

	private final String configName;
	

	DisplayUnit(String name) {
		configName = name;
	}
	
	@Override
	public String toString() {
		return configName;
	}

	@Override
	public String getExternalName() {
		return configName;
	}
	
}