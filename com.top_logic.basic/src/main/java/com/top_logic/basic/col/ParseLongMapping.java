/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * {@link Mapping} of {@link String} to {@link Long} through
 * {@link Long#parseLong(String)}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ParseLongMapping implements Mapping<String, Long> {

	/**
	 * Singleton {@link ParseLongMapping} instance.
	 */
	public static final ParseLongMapping INSTANCE = new ParseLongMapping();

	private ParseLongMapping() {
		// Singleton constructor.
	}
	
	@Override
	public Long map(String input) {
	    return Long.valueOf((Long.parseLong(input)));
	}

}
