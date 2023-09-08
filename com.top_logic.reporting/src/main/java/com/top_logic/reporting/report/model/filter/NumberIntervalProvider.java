/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.filter;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.reporting.report.exception.ReportingException;

/**
 * The NumberIntervalProvider returns intervals specified by two borders and a
 * granuarity
 * 
 * @author <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
@Deprecated
public class NumberIntervalProvider extends IntervalProvider{

    public NumberIntervalProvider(Double aGranularity) {
        super(aGranularity);
    }
    
	@Override
	public List getIntervals( Object aStart, Object aEnd) {

		double theStart = ((Number)aStart).doubleValue();
		double theEnd   = ((Number)aEnd).doubleValue();
		
		double theIntervalLength = ((Double) this.getGranularity()).doubleValue();
		
		if(theStart == 0.0 && theEnd == 0.0) {
			theStart -= theIntervalLength / 2;
			theEnd += theIntervalLength / 2;
		}
		double theLength = theEnd - theStart;
		double estimatedIntervals = theLength / theIntervalLength;
		if (estimatedIntervals > MAX_INTERVALS) {
			theIntervalLength = theLength / MAX_INTERVALS;
		}

		if (theLength < 0) {
			throw new ReportingException(NumberIntervalProvider.class,
					"Interval has length < 0!");
		}
		
		if(theLength < theIntervalLength) {
			theIntervalLength = theLength;
		}

		int theIntervalCount = (int) Math.round(theLength / theIntervalLength);
		ArrayList theReturn = new ArrayList(theIntervalCount);

		double theInterStart = theStart;
		double theInterEnd = theStart + theIntervalLength;
		while (theInterEnd < theEnd) {
			NumberInterval theEntry = new NumberInterval(theInterStart, theInterEnd);
			theReturn.add(theEntry);
			theInterStart = theInterEnd;
			theInterEnd += theIntervalLength;
		}

		if (theInterEnd >= theEnd) {
			NumberInterval theEntry = new NumberInterval(theInterStart, theEnd);
			theReturn.add(theEntry);
		}

		return theReturn;
	}
}
