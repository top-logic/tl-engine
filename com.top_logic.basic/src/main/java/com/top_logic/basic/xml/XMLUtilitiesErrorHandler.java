/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.top_logic.basic.Logger;

/**
 * {@link ErrorHandler} that logs errors and throws the error.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class XMLUtilitiesErrorHandler implements ErrorHandler {
	
	/**
	 * Singleton {@link XMLUtilitiesErrorHandler} instance.
	 */
	public static final XMLUtilitiesErrorHandler INSTANCE = new XMLUtilitiesErrorHandler();

	private XMLUtilitiesErrorHandler() {
		// Singleton constructor.
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		Logger.error(errorMessage("Error", e), this);
		throw e;
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		Logger.error(errorMessage("Fatal Error", e), this);
		throw e;
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		Logger.error(errorMessage("Warning", e), this);
		throw e;
	}

	private String errorMessage(String level, SAXParseException e) {
		return level + ": " + 
			"Line: " + e.getLineNumber() + ", " + 
			"Column: " + e.getColumnNumber() + ", " + 
			"Error: " + e.getMessage();
	}

}
