/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer.node.parser;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.reporting.report.exception.ReportingException;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.xmlutilities.ReportReader;

/**
 * TODO FSC
 * 
 * @author     <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 * @deprecated use {@link ReportReader} and {@link ReportConfiguration}
 */
@Deprecated
public class DateTokenHandler {

    private HashMap tokens;
    
    private static DateTokenHandler instance;
    
    private DateTokenHandler() {
        this.tokens  = new HashMap();
		this.tokens.put("currentYear", Integer.valueOf(Calendar.YEAR));
		this.tokens.put("currentMonth", Integer.valueOf(Calendar.MONTH));
		this.tokens.put("currentWeek", Integer.valueOf(Calendar.WEEK_OF_YEAR));
		this.tokens.put("currentDay", Integer.valueOf(Calendar.DAY_OF_YEAR));
    }
    
    public static DateTokenHandler getInstance() {
        if (instance == null) {
            instance = new DateTokenHandler();
        }
        return instance;
    }
    
    public Date handleToken(String aToken) {
       
        String[] theStrings = StringServices.toArray(aToken, ' ');
        
        String theKey = theStrings[0];
        int theAmount = 0;
        
        if (theStrings.length == 2) {
            String theAmountString = theStrings[1];
            theAmountString = theAmountString.replaceAll("\\+", "");
            theAmount = Integer.parseInt(theAmountString);
        }
        
        if (!this.tokens.containsKey(theKey)) {
            throw new ReportingException(getClass(), "Given token '"+aToken+"' is not valid!");
        }
        
		Calendar theCal = CalendarUtil.createCalendar();
        if (theAmount != 0) {
            theCal.add(((Number)this.tokens.get(theKey)).intValue(), theAmount);
        }
        return theCal.getTime();
    }
}
