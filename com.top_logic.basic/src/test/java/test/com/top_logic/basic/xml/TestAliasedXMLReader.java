/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.xml;

import java.io.StringReader;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import com.top_logic.basic.vars.IVariableExpander;
import com.top_logic.basic.xml.AliasedXMLReader;
import com.top_logic.basic.xml.XMLStreamUtil;

/**
 * Test case for {@link AliasedXMLReader}: aliases are resolved in attribute values and in element
 * text, however the text is read.
 */
@SuppressWarnings("javadoc")
public class TestAliasedXMLReader extends TestCase {

	private static final String ALIAS = "%SECRET%";

	private static final String VALUE = "unencrypted:the-secret";

	private static final IVariableExpander EXPANDER = s -> s.replace(ALIAS, VALUE);

	public void testAttributeValue() throws XMLStreamException {
		XMLStreamReader reader = reader("<config secret='" + ALIAS + "'/>");
		assertEquals(XMLStreamConstants.START_ELEMENT, reader.next());
		assertEquals(VALUE, reader.getAttributeValue(null, "secret"));
	}

	public void testElementText() throws XMLStreamException {
		XMLStreamReader reader = reader("<secret>" + ALIAS + "</secret>");
		assertEquals(XMLStreamConstants.START_ELEMENT, reader.next());
		assertEquals(VALUE, reader.getElementText());
	}

	/**
	 * The text of a character event, as read by the formatted content of an item-valued
	 * configuration property (<code>&lt;secret&gt;%PAC_SECRET%&lt;/secret&gt;</code>).
	 */
	public void testText() throws XMLStreamException {
		XMLStreamReader reader = reader("<secret>" + ALIAS + "</secret>");
		assertEquals(XMLStreamConstants.START_ELEMENT, reader.next());
		assertEquals(XMLStreamConstants.CHARACTERS, reader.next());
		assertEquals(VALUE, reader.getText());
	}

	public void testTextWithoutAlias() throws XMLStreamException {
		XMLStreamReader reader = reader("<secret>plain</secret>");
		assertEquals(XMLStreamConstants.START_ELEMENT, reader.next());
		assertEquals(XMLStreamConstants.CHARACTERS, reader.next());
		assertEquals("plain", reader.getText());
	}

	private static XMLStreamReader reader(String xml) throws XMLStreamException {
		return AliasedXMLReader.createAliasedXMLReader(
			XMLStreamUtil.getDefaultInputFactory().createXMLStreamReader(new StringReader(xml)), EXPANDER);
	}

}
