/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.xmlutilities.elements;


/**
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class XMLPaymentElement implements XMLElement {

//	private IntervalPartitionFunction function;
//	
//	public XMLPaymentElement(PartitionFunction aFunction) {
//		this.function = (IntervalPartitionFunction) aFunction;
//    }
//	
//	public Element createElement(Document aDocument) {
//		Element theElement = aDocument.createElement( PAYMENT_PARTITION_FUNCTION );
//		String anAttributeName = function.getAttributeName();
//		String ignoreNull = String.valueOf( function.ignoreNullValues() );
//		
//		String aBegin = DateConstants.XML_DATE_TIME.format( DateCriteriaUtils.nullToNow((Date) function.getBegin() ));
//		String anEnd = DateConstants.XML_DATE_TIME.format( DateCriteriaUtils.nullToNow((Date) function.getEnd() ));
//		Object relativeRanges = function.getAddtionalSettings().get( ReportConstants.RELATIVE_RANGES_FIELD );
//		boolean relative;
//		String theTimeRange = "";
//		if(relativeRanges != null) {
//			relative = ( (Boolean) relativeRanges ).booleanValue();
//			theTimeRange = (String) function.getAddtionalSettings().get( ReportConstants.TIME_RANGE_FIELD );
//		}
//		else {
//			relative = false;
//		}
//		Long aGranularity = function.getGranularity();
//		Element theIntervalCriteria = DateCriteriaUtils.createElement(CONST_DATE_INTERVAL_CRITERIA, aDocument, aBegin, anEnd, theTimeRange, aGranularity, relative);
//
//		Element theAttribute = XMLReportUtilities.createLeafElement( aDocument, ATTRIBUTE, anAttributeName );
//		Element theIgnore = XMLReportUtilities.createLeafElement( aDocument, IGNORE_NULL, ignoreNull );
//
//		theElement.appendChild( theAttribute );
//		theElement.appendChild( theIgnore );
//		theElement.appendChild( theIntervalCriteria );
//		
//		return theElement;
//	}
}
