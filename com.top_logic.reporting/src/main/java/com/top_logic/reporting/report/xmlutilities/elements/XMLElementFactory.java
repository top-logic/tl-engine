/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.xmlutilities.elements;

import com.top_logic.reporting.report.model.partition.function.PartitionFunction;
import com.top_logic.reporting.report.xmlutilities.XMLReportTags;

/**
 * The XMLElementFactory is a factory to create {@link XMLElement} for a given {@link PartitionFunction}.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class XMLElementFactory implements XMLReportTags{

//	private static XMLElementFactory instance;
//
//	private HashMap constuctors;
//	private HashMap parameters;
//	private Class[] defaultParameters;
//
//	private XMLElementFactory() {
//		constuctors = new HashMap();
//		parameters = new HashMap();
//		defaultParameters = new Class[] { PartitionFunction.class };
//		try {
//			constuctors.put(CLASSIFICATION_PARTITION_FUNCTION, XMLClassificationElement.class.getConstructor(defaultParameters));
//			constuctors.put(DATE_PARTITION_FUNCTION, XMLDateElement.class.getConstructor(defaultParameters));
//			constuctors.put(NUMBER_PARTITION_FUNCTION, XMLNumberElement.class.getConstructor(defaultParameters));
//			constuctors.put(SAME_PARTITION_FUNCTION, XMLSameElement.class.getConstructor(defaultParameters));
//			constuctors.put(STRING_PARTITION_FUNCTION, XMLStringElement.class.getConstructor(defaultParameters));
//			constuctors.put(PAYMENT_PARTITION_FUNCTION, XMLPaymentElement.class.getConstructor(defaultParameters));
//		}
//		catch (NoSuchMethodException ex) {
//			Logger.error("Cannot set up class constructors!", ex, this);
//			throw new ReportingException(this.getClass(), "Cannot set up class constructors!", ex);
//		}
//	}
//
//	public static synchronized XMLElementFactory getInstance() {
//		if (instance == null) {
//			instance = new XMLElementFactory();
//		}
//		return instance;
//	}
//
//	public XMLElement getPartitionElement( Document aDocument, String aType, PartitionFunction aFunction ) {
//		Constructor theConstructor = (Constructor) this.constuctors.get(aType);
//
//		if (theConstructor == null) {
//			throw new ReportingException(this.getClass(), "Given funtion name '" + aType
//					+ "' is not known");
//		}
//		try {
//			Object[] theParameters = (Object[]) parameters.get(aType);
//
//			if (theParameters == null) {
//				theParameters = new Object[] { aFunction };
//			}
//			else {
//				theParameters[0] = aFunction;
//			}
//			XMLElement theElement = (XMLElement) theConstructor.newInstance(theParameters);
//			return theElement;
//		}
//		catch (Exception e) {
//			throw new ReportingException(this.getClass(), "Unable to instanciate XMLElement: " + aType,
//					e);
//		}
//	}
}
