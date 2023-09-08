/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer.entry.parser;

import java.text.ParseException;
import java.util.Date;

import com.top_logic.basic.DateUtil;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.reporting.report.exception.ImportException;

/**
 * The DateEntryParser parses tags with the tag name 'date'.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public class DateEntryParser extends AbstractEntryParser {

    /** Constant for today. If the value */
    public static final String TODAY = "today";

    /** The single instance of this class. */
    public static final DateEntryParser INSTANCE = new DateEntryParser();

    /**
     * Creates a {@link DateEntryParser}. 
     * Please, use the single instance of this class ({@link #INSTANCE}).
     */
    private DateEntryParser() {
        // Do nothing.
    }
    
    @Override
	public String toString() {
        return getClass() + "[tag name=date]";
    }

    @Override
	protected Object getValue(String aValueAsString) {
        String theTrimmedStr = aValueAsString.trim();
        
        if(theTrimmedStr.equalsIgnoreCase(TODAY)) {
          return DateUtil.adjustToDayBegin(new Date());  
        } 
        
        try {
            return HTMLFormatter.getInstance().getDateFormat().parse(theTrimmedStr);
        }
        catch (ParseException pe) {
            throw new ImportException(getClass(), "Could not parse the date from the string: '" + theTrimmedStr + "'.", pe);
        }
    }

}

