/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.filter;

import java.text.DateFormat;
import java.util.Date;

import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.layout.LabelProvider;
import com.top_logic.mig.html.HTMLFormatter;

/**
 * Display a {@link DateInterval} in a proper way.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
@Deprecated
public class DateIntervalLabelProvider implements LabelProvider {

    @Override
	public String getLabel(Object anObject) {
        DateInterval  theMeeting = (DateInterval) anObject;
		Formatter theFormat = HTMLFormatter.getInstance();
        DateFormat    theTimer   = theFormat.getShortTimeFormat();
        DateFormat    theDate    = theFormat.getShortDateFormat();
        Date          theStart   = (Date) theMeeting.getBegin();
        Date          theEnd     = (Date) theMeeting.getEnd();
        String        theTime    = theTimer.format(theStart) + " - " + theTimer.format(theEnd);

        return (theDate.format(theStart) + " (" + theTime + ')');
    }
}

