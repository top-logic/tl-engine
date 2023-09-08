/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.xmlutilities.elements;

import com.top_logic.reporting.report.model.partition.function.NumberPartitionFunction;

/**
 * The XMLNumberElement describes the XML-representation of a {@link NumberPartitionFunction}.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class XMLNumberElement implements XMLElement {
//	private NumberPartitionFunction function;
//
//	// Constructors
//
//	/**
//	 * Creates a {@link XMLNumberElement}.
//	 * 
//	 */
//	public XMLNumberElement( PartitionFunction aFunction ) {
//		this.function = (NumberPartitionFunction) aFunction;
//	}
//
//	/**
//	 * @see com.top_logic.reporting.report.xmlutilities.elements.XMLElement#createElement(org.w3c.dom.Document)
//	 */
//	public Element createElement( Document aDocument ) {
//		String anAttributeName = function.getAttributeName();
//		String ignoreNull = String.valueOf( function.ignoreNullValues() );
//		Number aBegin = (Number) function.getBegin();
//		Number anEnd = (Number) function.getEnd();
//		Number aGranularity = (Number) function.getGranularity();
//		Element theElement = aDocument.createElement( NUMBER_PARTITION_FUNCTION );
//
//		Element theAttribute = XMLReportUtilities.createLeafElement( aDocument, ATTRIBUTE, anAttributeName );
//		Element theIgnore = XMLReportUtilities.createLeafElement( aDocument, IGNORE_NULL, ignoreNull );
//
//		Element theIntervalCriteria = aDocument.createElement( NUMBER_INTERVAL_CRITERIA );
//		Element theBegin = XMLReportUtilities.createLeafElement( aDocument, BEGIN, aBegin.toString() );
//		Element theEnd = XMLReportUtilities.createLeafElement( aDocument, END, anEnd.toString() );
//		Element theGranularity = XMLReportUtilities.createLeafElement( aDocument, NUMBER_GRANULARITY, aGranularity.toString() );
//
//		theIntervalCriteria.appendChild( theBegin );
//		theIntervalCriteria.appendChild( theEnd );
//		theIntervalCriteria.appendChild( theGranularity );
//
//		theElement.appendChild( theAttribute );
//		theElement.appendChild( theIgnore );
//		theElement.appendChild( theIntervalCriteria );
//
//		return theElement;
//	}
}
