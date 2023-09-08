/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.xmlutilities;

import org.xml.sax.helpers.DefaultHandler;

/**
 * The XMLReportErrorHandler for parsing XMLDocuments.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class XMLReportErrorHandler extends DefaultHandler {

//	/**
//	 * Warning.
//	 */
//	public void warning(SAXParseException e) {
//		String theSaxMsg = saxMsg("Warning", e);
//		Logger.error(theSaxMsg, this);
//		throw new ImportException(this.getClass(), theSaxMsg);
//	}
//
//	/**
//	 * Recoverable error.
//	 */
//	public void error(SAXParseException e) {
//		String theSaxMsg = saxMsg("Error", e);
//		Logger.error(theSaxMsg, this);
//		throw new ImportException(this.getClass(), theSaxMsg);
//	}
//
//	/**
//	 * Non-recoverable error.
//	 */
//	public void fatalError(SAXParseException e) {
//		String theSaxMsg = saxMsg("Fatal Error", e);
//		Logger.error(theSaxMsg, this);
//		throw new ImportException(this.getClass(), theSaxMsg);
//	}
//
//	/**
//	 * Creates an error message containing detailed information about what went wrong.
//	 * 
//	 * @param level
//	 *            the error level
//	 * @param e
//	 *            the exception thrown
//	 * @return a String with all information.
//	 */
//	private String saxMsg(String level, SAXParseException e) {
//		return level + ": Line: " + e.getLineNumber() + ", Column: " + e.getColumnNumber() + ", Error: "
//				+ e.getMessage();
//	}
}
