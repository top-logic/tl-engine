/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfigurationValueBinding;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueBinding;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.AsciiOutputStream;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.xml.XmlTextWriter;

/**
 * {@link ConfigurationValueBinding} including {@link StructuredText} values in typed
 * configurations.
 */
public class StructuredTextValueBinding extends AbstractConfigurationValueBinding<StructuredText> {

	/**
	 * Singleton {@link StructuredTextValueBinding} instance.
	 */
	public static final StructuredTextValueBinding INSTANCE = new StructuredTextValueBinding();

	private StructuredTextValueBinding() {
		// Singleton constructor.
	}

	/**
	 * Element containing the HTML contents in a CDATA section.
	 */
	private static final String SOURCE_CODE = "source-code";

	/**
	 * Element with the {@link #NAME_ATTR} containing the image content as BASE64 encoded text
	 * content.
	 */
	private static final String IMAGE = "image";

	/**
	 * Attribute of the {@link #IMAGE} element holding the image name.
	 */
	private static final String NAME_ATTR = "name";

	@Override
	public StructuredText loadConfigItem(XMLStreamReader in, StructuredText baseValue)
			throws XMLStreamException, ConfigurationException {
		StructuredText result = new StructuredText();

		int textTag = in.nextTag();
		if (textTag == XMLStreamConstants.END_ELEMENT) {
			return null;
		}

		if (textTag != XMLStreamConstants.START_ELEMENT) {
			throw new XMLStreamException("Expected start of " + SOURCE_CODE + " element.", in.getLocation());
		}
		if (!in.getLocalName().equals(SOURCE_CODE)) {
			throw new XMLStreamException("Expected " + SOURCE_CODE + " element, found: " + in.getLocalName(),
				in.getLocation());
		}
		result.setSourceCode(in.getElementText());
		if (in.getEventType() != XMLStreamConstants.END_ELEMENT) {
			throw new XMLStreamException("Expected end of " + SOURCE_CODE + " element.", in.getLocation());
		}

		readImages:
		while (true) {
			switch (in.nextTag()) {
				case XMLStreamConstants.END_ELEMENT:
					if (in.getLocalName().equals(IMAGE)) {
						throw new XMLStreamException(
							"Unexpected " + IMAGE + " close element. Images must have been consumed completely.",
							in.getLocation());
					}
					// all images read
					break readImages;

				case XMLStreamConstants.START_ELEMENT:
					if (!in.getLocalName().equals(IMAGE)) {
						throw new XMLStreamException("Expected " + IMAGE + " element, found: " + in.getLocalName(),
							in.getLocation());
					}
					String name = in.getAttributeValue(null, NAME_ATTR);
					try (AsciiInputStream asciiInput = new AsciiInputStream(in)) {
						BinaryData data =
							BinaryDataFactory.createFileBasedBinaryData(Base64.getDecoder().wrap(asciiInput));
						result.addImage(name, data);
						asciiInput.readToEnd();
					} catch (IOException ex) {
						throw new XMLStreamException("Reading image failed.", in.getLocation(), ex);
					}
					break;

				default:
					break;
			}
		}
		return result;
	}

	@Override
	public void saveConfigItem(XMLStreamWriter out, StructuredText value) throws XMLStreamException {
		String sourceCode = value.getSourceCode();
		if (StringServices.isEmpty(sourceCode)) {
			return;
		}

		Set<Entry<String, BinaryData>> images = value.getImages().entrySet();
		out.writeStartElement(SOURCE_CODE);
		// Note: When writing unescaped HTML, pretty-printing the configuation would break HTML
		// newline semantics!
		out.writeCData(sourceCode);
		out.writeEndElement();

		for (Entry<String, BinaryData> image : images) {
			out.writeStartElement(IMAGE);
			out.writeAttribute(NAME_ATTR, image.getKey());
			try (InputStream imageData = image.getValue().getStream()) {
				try (OutputStream xmlData = Base64.getEncoder().wrap(new AsciiOutputStream(new XmlTextWriter(out)))) {
					StreamUtilities.copyStreamContents(imageData, xmlData);
				}
			} catch (IOException ex) {
				Logger.error("Cannot convert image in structured text.", ex);
			}
			out.writeEndElement();
		}
	}

	private static final class AsciiInputStream extends InputStream {

		private XMLStreamReader _in;

		private char[] _buffer;

		private int _stop;

		private int _pos;

		private boolean _eof;

		/**
		 * Creates a {@link AsciiInputStream}.
		 *
		 * @param in
		 *        The {@link XMLStreamReader} to take input characters from.
		 */
		public AsciiInputStream(XMLStreamReader in) {
			_in = in;
		}

		@Override
		public int read() throws IOException {
			while (_pos >= _stop) {
				if (_eof) {
					return -1;
				}
				fetchChars();
			}
			int result = _buffer[_pos++];
			if (result > 127) {
				throw new IOException("Not an ASCII character: " + result);
			}
			return result;
		}

		protected void fetchChars() throws IOException {
			try {
				switch (_in.next()) {
					case XMLStreamConstants.CHARACTERS:
					case XMLStreamConstants.CDATA:
					case XMLStreamConstants.SPACE:
						_buffer = _in.getTextCharacters();
						_pos = _in.getTextStart();
						_stop = _pos + _in.getTextLength();
						break;
					case XMLStreamConstants.COMMENT:
						break;
					default:
						_eof = true;
						break;
				}
			} catch (XMLStreamException ex) {
				throw new IOException(ex);
			}
		}

		void readToEnd() throws IOException {
			while (!_eof) {
				fetchChars();
			}
		}
	}
}
