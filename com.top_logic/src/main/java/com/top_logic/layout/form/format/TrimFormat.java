/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.format;

import com.top_logic.basic.format.AbstractStringFormat;

/**
 * A format that trims and simply passes through its string value as output and does
 * no parsing at all.
 *
 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
 */
public class TrimFormat extends AbstractStringFormat {

	/**
	 * Singleton {@link TrimFormat} instance.
	 */
	public static final TrimFormat INSTANCE = new TrimFormat();

	protected TrimFormat() {
		// Singleton constructor.
	}

    @Override
	public String format(String aString) {
        return aString.trim();
    }

}
