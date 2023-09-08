/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import java.util.Date;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.LabelProvider;

/**
 * abstract superclass for renderer of dates. 
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public abstract class AbstractDateLabelProvider implements LabelProvider {

    /** Flag, if date(0) should be written. */
    private final boolean writeDate0;

    /**
	 * Creates a {@link AbstractDateLabelProvider}.
	 * 
	 */
    public AbstractDateLabelProvider() {
        this(true);
    }
    
    /**
	 * Creates a {@link AbstractDateLabelProvider}.
	 * 
	 * @param aFlag
	 *        Flag, if date(0) should be written.
	 */
    public AbstractDateLabelProvider(boolean aFlag) {
        this.writeDate0 = aFlag;
    }

    @Override
	public String getLabel(Object aValue) {
        Date theDate = (Date) aValue;

        if (this.writeDate0 || theDate.getTime() != 0) {
			return getDateTimeString(theDate);
		} else {
			return StringServices.EMPTY_STRING;
        }
    }
    
	/**
	 * The String representation of the given Date
	 */
	protected abstract String getDateTimeString(Date aDate);
    
}
