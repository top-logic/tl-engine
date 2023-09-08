/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.config.xdiff;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.AssertionFailedError;

import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.config.xdiff.model.Document;
import com.top_logic.config.xdiff.model.DocumentBuilder;

/**
 * Utility functions for testing XML differences.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestingUtil {

	public static Document fixture(String xml) {
		try {
			XMLInputFactory factory = XMLStreamUtil.getDefaultInputFactory();
			XMLStreamReader in = factory.createXMLStreamReader(new StringReader(xml));
			Document document = DocumentBuilder.parseDocument(in);
			return document;
		} catch (XMLStreamException ex) {
			throw (AssertionFailedError) new AssertionFailedError("Invalid test fixture.").initCause(ex);
		}
	}

}
