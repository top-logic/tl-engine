/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * {@link Mapping} of {@link String} to {@link Float} through
 * {@link Float#parseFloat(String)}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ParseFloatMapping implements Mapping<String, Float> {

	/**
	 * Singleton {@link ParseFloatMapping} instance.
	 */
	public static final ParseFloatMapping INSTANCE = new ParseFloatMapping();

	private ParseFloatMapping() {
		// Singleton constructor.
	}
	
	@Override
	public Float map(String input) {
  		return Float.valueOf((Float.parseFloat(input)));
	}

}
