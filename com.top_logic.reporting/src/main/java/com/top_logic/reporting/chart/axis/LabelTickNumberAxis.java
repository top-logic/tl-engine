/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.axis;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Date;

import org.jfree.chart.axis.NumberAxis;

import com.top_logic.mig.html.HTMLFormatter;

/**
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class LabelTickNumberAxis extends NumberAxis {

    /**
     * Creates a {@link LabelTickNumberAxis}.
     */
    public LabelTickNumberAxis() {
    }

    @Override
	public NumberFormat getNumberFormatOverride() {
        return new DateNumberFormat();
    }

    /**
     * The DateNumberFormat is for special use. The given value is a date.
     */
    public static class DateNumberFormat extends NumberFormat {

        @Override
		public StringBuffer format(double aNumber, StringBuffer aToAppendTo, FieldPosition aPos) {
            DateFormat theDateFormat = HTMLFormatter.getInstance().getDateFormat();
            StringBuffer theBuffer = new StringBuffer();
            theBuffer.append(theDateFormat.format(new Date((long) aNumber)));
            
            return theBuffer;
        }

        @Override
		public StringBuffer format(long aNumber, StringBuffer aToAppendTo, FieldPosition aPos) {
            return format((double) aNumber, aToAppendTo, aPos);
        }

        @Override
		public Number parse(String aSource, ParsePosition aParsePosition) {
            throw new UnsupportedOperationException();
        }
        
    }
    
}

