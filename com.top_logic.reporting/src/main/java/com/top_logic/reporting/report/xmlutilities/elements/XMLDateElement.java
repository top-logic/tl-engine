/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.xmlutilities.elements;

import com.top_logic.reporting.report.model.partition.function.DatePartitionFunction;

/**
 * The XMLDateElement describes the XML-representation of a {@link DatePartitionFunction}.
 *
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class XMLDateElement implements XMLElement {
//	private DatePartitionFunction	function;
//
//	// Constructors
//
//	/**
//	 * Creates a {@link XMLDateElement}.
//	 *
//	 */
//	public XMLDateElement( PartitionFunction aFunction ) {
//		this.function = (DatePartitionFunction) aFunction;
//	}
//
//	
//
//	/**
//	 * @see com.top_logic.reporting.report.xmlutilities.elements.XMLElement#createElement(org.w3c.dom.Document)
//	 */
//	public Element createElement( Document aDocument ) {
//		String anAttributeName = function.getAttributeName();
//		String ignoreNull = String.valueOf( function.ignoreNullValues() );
//
//		// TODO TBE change handling of null dates to indicate current date
//		String aBegin = DateConstants.XML_DATE_TIME.format( DateCriteriaUtils.nullToNow(function.getBegin() ));
//		String anEnd = DateConstants.XML_DATE_TIME.format( DateCriteriaUtils.nullToNow(function.getEnd() ));
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
//		Element theElement = aDocument.createElement( DATE_PARTITION_FUNCTION );
//		Element theAttribute = XMLReportUtilities.createLeafElement( aDocument, ATTRIBUTE, anAttributeName );
//		Element theIgnore = XMLReportUtilities.createLeafElement( aDocument, IGNORE_NULL, ignoreNull );
//		
//		Element theIntervalCriteria = DateCriteriaUtils.createElement(DATE_INTERVAL_CRITERIA, aDocument, aBegin, anEnd, theTimeRange, aGranularity, relative);
//		
//		theElement.appendChild( theAttribute );
//		theElement.appendChild( theIgnore );
//		theElement.appendChild( theIntervalCriteria );
//
//		return theElement;
//	}
}
