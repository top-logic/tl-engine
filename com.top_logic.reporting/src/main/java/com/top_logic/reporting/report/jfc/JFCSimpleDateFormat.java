/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.jfc;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.jfree.data.time.Quarter;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.reporting.common.period.HalfYear;

/**
 * The JFCSimpleDateFormat is a simple date format for jfreechart. Additional
 * you can use 'h' for halfyear and 'q' for quarter.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class JFCSimpleDateFormat extends DateFormat {

    public static final String PATTERN_QUARTER   = "q";
    public static final String PATTERN_HALF_YEAR = "a";

    private String           pattern;
    private SimpleDateFormat dateFormat;

	private TimeZone _timeZone;

	private Locale _locale;
    
	/**
	 * Creates a {@link JFCSimpleDateFormat} with {@link TimeZone} and
	 * {@link Locale} of the current user.
	 */
	public JFCSimpleDateFormat(String aPattern) {
		this(aPattern, TimeZones.systemTimeZone(), ThreadContext.getLocale());
		
	}

	/** Creates a {@link JFCSimpleDateFormat}. */
	public JFCSimpleDateFormat(String aPattern, TimeZone timeZone, Locale locale) {
        this.pattern = aPattern;
		_timeZone = timeZone;
		_locale = locale;
        
        // Init the date format pattern
        if ( (aPattern.equals(PATTERN_QUARTER)) || (aPattern.equals(PATTERN_HALF_YEAR)) ) {
            this.dateFormat = null;
        } else {
			SimpleDateFormat format = new SimpleDateFormat(aPattern, _locale);
			format.setTimeZone(timeZone);
			this.dateFormat = format;

        }
    }


    @Override
	public StringBuffer format(Date aDate, StringBuffer aToAppendTo, FieldPosition aFieldPosition) {
        if (this.pattern.equals(PATTERN_HALF_YEAR)) {
			return aToAppendTo
				.append(HalfYear.getHalfYearFor(aDate, CalendarUtil.createCalendar(aDate, _timeZone, _locale)));
        } else if (this.pattern.equals(PATTERN_QUARTER)) {
			return aToAppendTo.append(new Quarter(aDate, _timeZone, _locale).getQuarter());
        }
        
        return aToAppendTo.append(dateFormat.format(aDate));
    }

    @Override
	public Date parse(String aSource, ParsePosition aPos) {
        return null;
    }
    
}

