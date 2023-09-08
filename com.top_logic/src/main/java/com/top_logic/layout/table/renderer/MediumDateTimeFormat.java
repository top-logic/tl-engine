/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import java.util.Date;

import com.top_logic.mig.html.HTMLFormatter;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class MediumDateTimeFormat  extends AbstractDateLabelProvider {
	
	 /** Default renderer for dates. */
    public static final MediumDateTimeFormat INSTANCE = new MediumDateTimeFormat();

    /** Renderer, which will display all dates except of Date(0). */
    public static final MediumDateTimeFormat DATE_0_INSTANCE = new MediumDateTimeFormat(false);

    /**
	 * Creates a {@link MediumDateTimeFormat}.
	 */
    private MediumDateTimeFormat() {
        super();
    }
    
    /**
	 * Creates a {@link MediumDateTimeFormat}.
	 * 
	 * @param aFlag
	 *        Flag, if date(0) should be written.
	 */
    private MediumDateTimeFormat(boolean aFlag) {
        super(aFlag);
    }

	@Override
	protected String getDateTimeString(Date aDate) {
		return HTMLFormatter.getInstance().formatMediumDateTime(aDate);
	}
    
}
