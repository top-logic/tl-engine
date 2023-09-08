/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.xmlutilities.elements;

import com.top_logic.reporting.report.xmlutilities.XMLReportTags;

/**
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class DateCriteriaUtils implements XMLReportTags{
	
//    private static final String ABSOLUTE = "absolute";
//    private static final String CURRENT  = "current";
//    
//	public static Element createElement(String anElementName, Document aDocument, String aBegin, String anEnd, String aTimeRange, Object aGranularity, boolean relative) {
//		Element theIntervalCriteria = aDocument.createElement( anElementName );
//		if (relative && !DatePartitionFunction.DATE_RANGE_MANUAL.equals( aTimeRange )) {
////			String timeRange = aTimeRange;
////			if (DateConstants.DAILY.equals( aTimeRange )) {
////				timeRange = DateConstants.DAILY;
////			}
////			else if (DateConstants.WEEKLY.equals( aTimeRange )) {
////				timeRange = DateConstants.WEEKLY;
////			}
////			else if (DateConstants.MONTHLY.equals( aTimeRange )) {
////				timeRange = DateConstants.MONTHLY;
////			}
////			else if (DateConstants.QUARTERLY.equals( aTimeRange )) {
////				timeRange = DateConstants.QUARTERLY;
////			}
////			else {
////				timeRange = DateConstants.ANNUALLY;
////			}
//			Element theBegin = XMLReportUtilities.createLeafElement( aDocument, BEGIN, null );
//			theBegin.setAttribute( XMLReportTags.RELATIVE_ATTRIBUTE, aTimeRange );
//			Element theEnd = XMLReportUtilities.createLeafElement( aDocument, END, null );
//			theEnd.setAttribute( XMLReportTags.RELATIVE_ATTRIBUTE, CURRENT);
//
//			theIntervalCriteria.appendChild( theBegin );
//			theIntervalCriteria.appendChild( theEnd );
//		}
//		else if(!relative  && !StringServices.isEmpty(aTimeRange) && !DatePartitionFunction.DATE_RANGE_MANUAL.equals( aTimeRange )) {
//			String timeRange = "";
//			if (DatePartitionFunction.DATE_RANGE_DAY.equals( aTimeRange )) {
//				timeRange = aTimeRange;
//			}
//			else if (DatePartitionFunction.DATE_RANGE_WEEK.equals( aTimeRange )) {
//				timeRange = aTimeRange;
//			}
//			else if (DatePartitionFunction.DATE_RANGE_MONTH.equals( aTimeRange )) {
//				timeRange = aTimeRange;
//			}
//			else if (DatePartitionFunction.DATE_RANGE_QUARTER.equals( aTimeRange )) {
//				timeRange = aTimeRange;
//			}
//			else {
//				timeRange = DatePartitionFunction.DATE_RANGE_YEAR;
//			}
//			Element theBegin = XMLReportUtilities.createLeafElement( aDocument, BEGIN, null );
//			theBegin.setAttribute( XMLReportTags.RELATIVE_ATTRIBUTE, timeRange );
//			Element theEnd = XMLReportUtilities.createLeafElement( aDocument, END, anEnd );
//			theEnd.setAttribute( XMLReportTags.RELATIVE_ATTRIBUTE, ABSOLUTE);
//
//			theIntervalCriteria.appendChild( theBegin );
//			theIntervalCriteria.appendChild( theEnd );
//		}
//		else {
//			Element theBegin = XMLReportUtilities.createLeafElement( aDocument, BEGIN, aBegin );
//			theBegin.setAttribute( XMLReportTags.RELATIVE_ATTRIBUTE, ABSOLUTE );
//			Element theEnd = XMLReportUtilities.createLeafElement( aDocument, END, anEnd );
//			theEnd.setAttribute( XMLReportTags.RELATIVE_ATTRIBUTE, ABSOLUTE );
//
//			theIntervalCriteria.appendChild( theBegin );
//			theIntervalCriteria.appendChild( theEnd );
//		}
//		Element theGranularity = createGranularityLeaf(aDocument, aGranularity);
//		theIntervalCriteria.appendChild( theGranularity );
//		
//		return theIntervalCriteria;
//	}
//	
//	private static Element createGranularityLeaf(Document aDocument, Object aGranularity) {
//		String theGran = "";
//		if(aGranularity instanceof Integer) {
//			theGran = aGranularity.toString();
//		}
//		else {
//			theGran = (String) aGranularity;
//		}
//		Element theGranularity = XMLReportUtilities.createLeafElement( aDocument, DATE_GRANULARITY, theGran);
//		return theGranularity;
//	}
//	
//	public static Date nullToNow(Date aDate) {
//	    return aDate == null ? new Date() : aDate;
//	}

}
