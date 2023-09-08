/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import java.util.Date;

import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.layout.LabelProvider;
import com.top_logic.mig.html.HTMLFormatter;

/**
 * {@link LabelProvider} that formats a {@link Date} using {@link Formatter#formatDateTime(Date)}.
 * 
 * @since TL_5.6.1_19
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DateTimeLabelProvider implements LabelProvider {

	/**
	 * Singleton {@link DateTimeLabelProvider} instance.
	 */
	public static final DateTimeLabelProvider INSTANCE = new DateTimeLabelProvider();

	private DateTimeLabelProvider() {
		// Singleton constructor.
	}

    @Override
	public String getLabel(Object anObject) {
        if (anObject instanceof Date) {
            return HTMLFormatter.getInstance().formatDateTime((Date) anObject);
        } else {
            return null;
        }
    }
}