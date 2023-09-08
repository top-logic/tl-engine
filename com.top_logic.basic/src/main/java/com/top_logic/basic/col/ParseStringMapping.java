/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;


/**
 * {@link Mapping} of {@link Object} to {@link String} using {@link String#valueOf(Object)}.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class ParseStringMapping implements Mapping<Object, String> {

	/**
	 * Singleton {@link ParseStringMapping} instance.
	 */
	public static final ParseStringMapping INSTANCE = new ParseStringMapping();

	private ParseStringMapping() {
		// Singleton constructor.
	}

    @Override
	public String map(Object aInput) {
        return String.valueOf(aInput);
    }

}
