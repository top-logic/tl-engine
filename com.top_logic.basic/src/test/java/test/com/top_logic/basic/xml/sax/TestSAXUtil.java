/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.xml.sax;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import junit.framework.TestCase;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.xml.sax.SAXUtil;

/**
 * Test case for {@link SAXUtil}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestSAXUtil extends TestCase {

	/**
	 * Test that factories are configured to prevent XML external entity injection attacks.
	 */
	public void testExternalEntityInjectionPrevention() throws ParserConfigurationException, SAXException, IOException {
		SAXParser parser = SAXUtil.newSAXParserNamespaceAware();

		String xml = "<?xml version='1.0' encoding='utf-8'?>"
			+ "<!DOCTYPE r ["
			+ "<!ELEMENT r ANY>"
			+ "<!ENTITY sp SYSTEM 'file:///'>"
			+ "]>"
			+ "<foo>&sp;</foo>";

		DefaultHandler handler = new DefaultHandler() {
			@Override
			public void characters(char[] ch, int start, int length) throws SAXException {
				super.characters(ch, start, length);
				String text = new String(ch, start, length);
				System.out.println(text);
				assertEquals("", text);
			}
		};
		try {
			parser.parse(new ByteArrayInputStream(xml.getBytes(Charset.forName("utf-8"))), handler);
		} catch (SAXParseException ex) {
			BasicTestCase.assertContains("DOCTYPE", ex.getMessage());
		}
	}

}
