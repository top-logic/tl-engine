/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import java.io.IOError;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.config.AbstractConfigurationValueBinding;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueBinding;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.xml.XMLStreamUtil;

/**
 * {@link ConfigurationValueBinding} that reads an arbitrary XML fragment into a {@link String}
 * property.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XMLFragmentString extends AbstractConfigurationValueBinding<String> {

	/**
	 * Singleton {@link XMLFragmentString} instance.
	 */
	public static final XMLFragmentString INSTANCE = new XMLFragmentString();

	private XMLFragmentString() {
		// Singleton constructor.
	}

	@Override
	public String loadConfigItem(XMLStreamReader in, String baseValue)
			throws XMLStreamException, ConfigurationException {
		return readFragment(in);
	}

	/**
	 * Reads an XML fragment from the given {@link XMLStreamReader} into an XML string.
	 * 
	 * <p>
	 * After return, the reader has either seen the closing tag corresponding to the opening tag it
	 * has last read when the method was called, or on the end event of the document.
	 * </p>
	 */
	public static String readFragment(XMLStreamReader in) throws XMLStreamException, IOError {
		TagWriter out = new TagWriter();
		try {
			forwardContent(in, out);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		return out.toString().trim();
	}

	private static void forwardContent(XMLStreamReader in, TagWriter out) throws XMLStreamException, IOException {
		while (true) {
			int tag = in.next();
			switch (tag) {
				case XMLStreamConstants.END_ELEMENT:
				case XMLStreamConstants.END_DOCUMENT:
					return;
				case XMLStreamConstants.CDATA: {
					out.writeCDATAContent(in.getText());
					break;
				}
				case XMLStreamConstants.SPACE:
				case XMLStreamConstants.CHARACTERS: {
					out.writeText(in.getText());
					break;
				}
				case XMLStreamConstants.COMMENT: {
					forwardElement(in, out);
					break;
				}
				case XMLStreamConstants.START_ELEMENT: {
					forwardElement(in, out);
					break;
				}
			}
		}
	}

	private static void forwardElement(XMLStreamReader in, TagWriter out) throws XMLStreamException, IOException {
		String localName = in.getLocalName();
		out.beginBeginTag(localName);
		for (int n = 0, cnt = in.getAttributeCount(); n < cnt; n++) {
			out.writeAttribute(in.getAttributeLocalName(n), in.getAttributeValue(n));
		}
		out.endBeginTag();
		forwardContent(in, out);
		out.endTag(localName);
	}

	@Override
	public void saveConfigItem(XMLStreamWriter out, String item) throws XMLStreamException {
		forwardFragment(out, item);
	}

	private static void forwardFragment(XMLStreamWriter out, String xmlFragment) throws XMLStreamException {
		XMLStreamReader in =
			XMLStreamUtil.getDefaultInputFactory().createXMLStreamReader(new StringReader("<r>" + xmlFragment + "</r>"));
		XMLStreamUtil.nextStartTag(in);
		forwardContent(in, out);
	}

	private static void forwardContent(XMLStreamReader in, XMLStreamWriter out) throws XMLStreamException {
		while (true) {
			int tag = in.next();
			switch (tag) {
				case XMLStreamConstants.END_DOCUMENT:
				case XMLStreamConstants.END_ELEMENT:
					return;
				case XMLStreamConstants.CDATA: {
					out.writeCData(in.getText());
					break;
				}
				case XMLStreamConstants.SPACE:
				case XMLStreamConstants.CHARACTERS: {
					out.writeCharacters(in.getText());
					break;
				}
				case XMLStreamConstants.COMMENT: {
					forwardElement(in, out);
					break;
				}
				case XMLStreamConstants.START_ELEMENT: {
					forwardElement(in, out);
					break;
				}
			}
		}
	}

	private static void forwardElement(XMLStreamReader in, XMLStreamWriter out) throws XMLStreamException {
		String localName = in.getLocalName();
		out.writeStartElement(localName);
		for (int n = 0, cnt = in.getAttributeCount(); n < cnt; n++) {
			out.writeAttribute(in.getAttributeLocalName(n), in.getAttributeValue(n));
		}
		forwardContent(in, out);
		out.writeEndElement();
	}

}