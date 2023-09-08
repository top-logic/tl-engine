/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import java.util.Date;

import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.layout.LabelProvider;
import com.top_logic.mig.html.HTMLFormatter;

/**
 * {@link LabelProvider} using {@link Formatter#formatDateTime(Date)}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class DateTimeFormat extends AbstractDateLabelProvider{

	/**
	 * Singleton {@link DateTimeFormat} instance.
	 */
	public static final DateTimeFormat INSTANCE = new DateTimeFormat();

	private DateTimeFormat() {
		// Singleton constructor.
	}
    
    /**
	 * Creates a {@link DateTimeFormat}.
	 * 
	 * @param aFlag
	 *        Flag, if date(0) should be written.
	 */
    public DateTimeFormat(boolean aFlag) {
        super(aFlag);
    }
    
	@Override
	protected String getDateTimeString(Date aDate) {
		return HTMLFormatter.getInstance().formatDateTime(aDate);
	}
}
