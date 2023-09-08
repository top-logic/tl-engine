/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.xml.document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.document.TSDocument;
import com.top_logic.basic.xml.document.TSElement;
import com.top_logic.basic.xml.document.ThreadSafeDOMFactory;

/**
 * The class {@link TestTSElement} tests the {@link TSElement} implementation.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestTSElement extends TestCase {

	public void testAttributes() throws ParserConfigurationException {
		Document document = newDocument();
		Element root = document.createElement("root");
		document.appendChild(root);
		assertFalse(root.hasAttributes());
		assertEquals(0, root.getAttributes().getLength());

		TSDocument tsDocument = ThreadSafeDOMFactory.importDocument(document);
		Element importedRoot = tsDocument.getDocumentElement();
		assertFalse(importedRoot.hasAttributes());
		assertEquals(root.getTagName(), importedRoot.getTagName());
		NamedNodeMap importedAttributes = importedRoot.getAttributes();
		assertNotNull(importedAttributes);
		assertEquals(0, importedAttributes.getLength());
	}

	public void testAttributeNS() throws ParserConfigurationException {
		String ns1Uri = "http://www.top-logic.com/ns1";
		String ns2Uri = "http://www.top-logic.com/ns2";

		Document document = newDocument();
		Element root = document.createElement("root");
		root.setAttributeNS(ns1Uri, "ns1:attr1", "val1");
		root.setAttributeNS(ns2Uri, "ns2:attr2", "val2");
		root.setAttributeNS(null, "attr3", "val3");
		document.appendChild(root);
		assertTrue(root.hasAttributes());
		assertEquals(3, root.getAttributes().getLength());
		assertEquals("val1", root.getAttributeNS(ns1Uri, "attr1"));
		assertEquals("val2", root.getAttributeNS(ns2Uri, "attr2"));
		assertEquals("val3", root.getAttributeNS(null, "attr3"));
		assertEquals("", root.getAttributeNS(null, "doesNotExist"));
		assertEquals("", root.getAttributeNS(ns1Uri, "doesNotExist"));

		TSDocument tsDocument = ThreadSafeDOMFactory.importDocument(document);
		Element importedRoot = tsDocument.getDocumentElement();
		assertTrue(importedRoot.hasAttributes());
		assertEquals(root.getTagName(), importedRoot.getTagName());

		NamedNodeMap importedAttributes = importedRoot.getAttributes();
		assertNotNull(importedAttributes);
		assertEquals(3, importedAttributes.getLength());
		// Access using attribute map
		assertEquals("val1", importedAttributes.getNamedItemNS(ns1Uri, "attr1").getNodeValue());
		assertEquals("val2", importedAttributes.getNamedItemNS(ns2Uri, "attr2").getNodeValue());
		assertEquals("val3", importedAttributes.getNamedItemNS(null, "attr3").getNodeValue());
		// Access using element
		assertEquals("val1", importedRoot.getAttributeNS(ns1Uri, "attr1"));
		assertEquals("val2", importedRoot.getAttributeNS(ns2Uri, "attr2"));
		assertEquals("val3", importedRoot.getAttributeNS(null, "attr3"));
		assertEquals("", importedRoot.getAttributeNS(null, "doesNotExist"));
		assertEquals("", importedRoot.getAttributeNS(ns1Uri, "doesNotExist"));
	}

	private Document newDocument() throws ParserConfigurationException {
		DocumentBuilder docBuilder = DOMUtil.newDocumentBuilder();

		Document document = docBuilder.newDocument();
		return document;
	}

}
