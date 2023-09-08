/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import java.util.Date;

import com.top_logic.mig.html.HTMLFormatter;

/**
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ShortDateTimeFormat  extends AbstractDateLabelProvider {

    /** Default renderer for dates. */
    public static final ShortDateTimeFormat INSTANCE = new ShortDateTimeFormat();

    /** Renderer, which will display all dates except of Date(0). */
    public static final ShortDateTimeFormat DATE_0_INSTANCE = new ShortDateTimeFormat(false);

    /**
	 * Creates a {@link ShortDateTimeFormat}.
	 */
    public ShortDateTimeFormat() {
        super();
    }
    
    /**
	 * Creates a {@link ShortDateTimeFormat}.
	 * 
	 * @param aFlag
	 *        Flag, if date(0) should be written.
	 */
    public ShortDateTimeFormat(boolean aFlag) {
        super(aFlag);
    }

	@Override
	protected String getDateTimeString(Date aDate) {
		return HTMLFormatter.getInstance().formatShortDateTime(aDate);
	}
    
    
}