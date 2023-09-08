/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.xml;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;

import com.top_logic.basic.xml.XMLStreamUtil;

/**
 * Test case for {@link XMLStreamUtil}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestXMLStreamUtil extends TestCase {

	/**
	 * Test that start an end offsets differ, if non-empty elements are read.
	 */
	public void testNonEmptyElementDetection() throws XMLStreamException {
		XMLStreamReader in = createReader("<a attr='v1'></a>");
		
		assertEquals(XMLStreamConstants.START_ELEMENT, in.nextTag());
		int startOffset = in.getLocation().getCharacterOffset();
		
		assertEquals(XMLStreamConstants.END_ELEMENT, in.nextTag());
		int endOffset = in.getLocation().getCharacterOffset();
		
		assertTrue(startOffset < endOffset);
	}
	
	/**
	 * Test that start an end offsets are the same, if empty elements are read.
	 */
	public void testEmptyElementDetection() throws XMLStreamException {
		XMLStreamReader in = createReader("<a attr='v1'/>");
		
		assertEquals(XMLStreamConstants.START_ELEMENT, in.nextTag());
		int startOffset = in.getLocation().getCharacterOffset();
		
		assertEquals(XMLStreamConstants.END_ELEMENT, in.nextTag());
		int endOffset = in.getLocation().getCharacterOffset();
		
		assertTrue(startOffset == endOffset);
	}

	/**
	 * Test reading and writing long attributes.
	 */
	public void testReadWriteLongAttr() throws XMLStreamException {
		long l = Long.MIN_VALUE;
		XMLStreamReader in = createReader("<a attr='" + l + "'/>");

		assertEquals(XMLStreamConstants.START_ELEMENT, in.nextTag());
		long readValue = XMLStreamUtil.readLongAttribute(in, null, "attr", 0);
		assertEquals(l, readValue);
		
		StringWriter stringOut = new StringWriter();
		XMLStreamWriter writer = createWriter(stringOut);
		writer.writeStartElement("a");
		XMLStreamUtil.writeLongAttribute(writer, "attr", l);
		writer.writeEndElement();

		assertTrue(stringOut.toString().contains(Long.toString(l)));
	}

	/**
	 * Regression test for https://bugs.openjdk.java.net/browse/JDK-8058175
	 */
	public void testUnicode() throws XMLStreamException {
		XMLStreamReader in = createReader("<a attr='\uD841\uDDAA\uD841\uDDAA\uE066'/>");

		in.nextTag();
		String value = in.getAttributeValue(null, "attr");

		while (in.next() != XMLStreamConstants.END_DOCUMENT) {
			// Skip contents.
		}

		assertEquals("\uD841\uDDAA\uD841\uDDAA\uE066", value);
	}

	private XMLStreamReader createReader(String xml) throws XMLStreamException {
		return XMLStreamUtil.getDefaultInputFactory().createXMLStreamReader(reader(xml));
	}

	private Reader reader(String xml) {
		return new StringReader(xml);
	}

	private XMLStreamWriter createWriter(Writer out) throws XMLStreamException {
		return XMLStreamUtil.getDefaultOutputFactory().createXMLStreamWriter(out);
	}

}
