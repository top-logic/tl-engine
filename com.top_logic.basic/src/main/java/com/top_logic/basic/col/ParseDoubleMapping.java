/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * {@link Mapping} of {@link String} to {@link Double} through
 * {@link Double#parseDouble(String)}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ParseDoubleMapping implements Mapping<String, Double> {

	/**
	 * Singleton {@link ParseDoubleMapping} instance.
	 */
	public static final ParseDoubleMapping INSTANCE = new ParseDoubleMapping();

	private ParseDoubleMapping() {
		// Singleton constructor.
	}
	
	@Override
	public Double map(String input) {
  		return Double.valueOf((Double.parseDouble(input)));
	}

}
