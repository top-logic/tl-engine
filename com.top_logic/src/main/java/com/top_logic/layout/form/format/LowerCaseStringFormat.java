/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.format;

import com.top_logic.basic.format.AbstractStringFormat;

/**
 * This format changes a string to a lower case string.
 *
 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
 */
public class LowerCaseStringFormat extends AbstractStringFormat {

	/**
	 * Singleton {@link LowerCaseStringFormat} instance.
	 */
	public static final LowerCaseStringFormat INSTANCE = new LowerCaseStringFormat();

	private LowerCaseStringFormat() {
		// Singleton constructor.
	}

    @Override
	public String format(String aString) {
        return aString.trim().toLowerCase();
    }

}
