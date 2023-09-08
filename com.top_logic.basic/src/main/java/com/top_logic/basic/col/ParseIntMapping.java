/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * {@link Mapping} of {@link String} to {@link Integer} through {@link Integer#parseInt(String)}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ParseIntMapping implements Mapping<String, Integer> {

	/**
	 * Singleton {@link ParseIntMapping} instance.
	 */
	public static final ParseIntMapping INSTANCE = new ParseIntMapping();

	private ParseIntMapping() {
		// Singleton constructor.
	}
	
	@Override
	public Integer map(String input) {
	    return Integer.valueOf(Integer.parseInt(input));
	}

}
