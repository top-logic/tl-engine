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
public class FirstCharacterMapping implements Mapping<String, Character> {

	/**
	 * Singleton {@link FirstCharacterMapping} instance.
	 */
	public static final FirstCharacterMapping INSTANCE = new FirstCharacterMapping();

	private FirstCharacterMapping() {
		// Singleton constructor.
	}
	
    @Override
	public Character map(String input) {
        return Character.valueOf(input.length() == 0 ? 0 : input.charAt(0));
    }

}
