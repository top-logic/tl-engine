/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.xmlutilities.elements;

import com.top_logic.reporting.report.model.partition.function.SamePartitionFunction;

/**
 * The XMLSameElement describes the XML-representation of a {@link SamePartitionFunction}.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class XMLSameElement implements XMLElement {
//	private SamePartitionFunction function;
//
//	// Constructors
//
//	/**
//	 * Creates a {@link XMLSameElement}.
//	 * 
//	 */
//	public XMLSameElement( PartitionFunction aFunction ) {
//		this.function = (SamePartitionFunction) aFunction;
//	}
//
//	/**
//	 * TODO tbe: implement xml structure for this partition function
//	 * @see com.top_logic.reporting.report.xmlutilities.elements.XMLElement#createElement(org.w3c.dom.Document)
//	 */
//	public Element createElement( Document aDocument ) {
//		Element theElement = aDocument.createElement( SAME_PARTITION_FUNCTION );
//		String anAttributeName = function.getAttributeName();
//		String ignoreNull = String.valueOf( function.ignoreNullValues() );
//		
//		Element theAttribute = XMLReportUtilities.createLeafElement( aDocument, ATTRIBUTE, anAttributeName );
//		Element theIgnore = XMLReportUtilities.createLeafElement( aDocument, IGNORE_NULL, ignoreNull );
//
//		theElement.appendChild( theAttribute );
//		theElement.appendChild( theIgnore );
//		return theElement;
//	}

}
