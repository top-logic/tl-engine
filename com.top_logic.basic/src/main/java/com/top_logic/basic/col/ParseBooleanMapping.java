/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * {@link Mapping} of {@link String} to {@link Boolean}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ParseBooleanMapping implements Mapping<String, Boolean> {

	/**
	 * Singleton {@link ParseBooleanMapping} instance.
	 */
	public static final ParseBooleanMapping INSTANCE = new ParseBooleanMapping();

	private ParseBooleanMapping() {
		// Singleton constructor.
	}
	
	@Override
	public Boolean map(String input) {
	    return Boolean.valueOf(input);
	}

}
