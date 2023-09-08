/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.wysiwyg.storage;

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

import com.top_logic.basic.Log;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.basic.xml.log.XMLStreamLog;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.io.AttributeValueBinding;

/**
 * {@link AttributeValueBinding} for {@link StructuredText} values.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StructuredTextAttributeValueBinding implements AttributeValueBinding<StructuredText> {

	/**
	 * Singleton {@link StructuredTextAttributeValueBinding} instance.
	 */
	public static final StructuredTextAttributeValueBinding INSTANCE = new StructuredTextAttributeValueBinding();

	private StructuredTextAttributeValueBinding() {
		// Singleton constructor.
	}

	/**
	 * Element containing the HTML contents in a CDATA section.
	 */
	private static final String TEXT = "text";

	/**
	 * Attribute of {@link #TEXT} defining the number of exported images.
	 */
	private static final String IMAGE_COUNT = "img-count";

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
	public StructuredText loadValue(XMLStreamLog log, XMLStreamReader in, TLStructuredTypePart attribute) throws XMLStreamException {
		StructuredText result = new StructuredText();

		int textTag = in.nextTag();
		if (textTag == XMLStreamConstants.END_ELEMENT) {
			return null;
		}

		if (textTag != XMLStreamConstants.START_ELEMENT) {
			throw new XMLStreamException("Expected start of " + TEXT + " element.", in.getLocation());
		}
		if (!in.getLocalName().equals(TEXT)) {
			throw new XMLStreamException("Expected " + TEXT + " element, found: " + in.getLocalName(),
				in.getLocation());
		}
		int expectedImageCount = -1;
		String imgCount = in.getAttributeValue(null, IMAGE_COUNT);
		if (!StringServices.isEmpty(imgCount)) {
			try {
				expectedImageCount = Integer.parseInt(imgCount);
			} catch (NumberFormatException ex) {
				throw new XMLStreamException("Reading image count failed.", in.getLocation(), ex);
			}
		}
		result.setSourceCode(XMLStreamUtil.nextText(in));
		if (in.getEventType() != XMLStreamConstants.END_ELEMENT) {
			throw new XMLStreamException("Expected end of " + TEXT + " element.", in.getLocation());
		}

		int readImages = 0;
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
					readImages++;
					break;

				default:
					break;
			}
		}
		if (expectedImageCount > -1) {
			if( expectedImageCount != readImages) {
				throw new XMLStreamException(
					"Unexpected number of images: Expected '" + expectedImageCount + "', Actual '" + readImages + "'.",
					in.getLocation());
			}
		} else {
			// No expected number of images given.
		}
		return result;
	}

	@Override
	public void storeValue(Log log, XMLStreamWriter out, TLStructuredTypePart attribute, StructuredText value) throws XMLStreamException {
		String sourceCode = value.getSourceCode();
		if (StringServices.isEmpty(sourceCode)) {
			return;
		}

		Set<Entry<String, BinaryData>> images = value.getImages().entrySet();
		out.writeStartElement(TEXT);
		out.writeAttribute(IMAGE_COUNT, Integer.toString(images.size()));
		out.writeCData(sourceCode);
		out.writeEndElement();

		for (Entry<String, BinaryData> image : images) {
			out.writeStartElement(IMAGE);
			out.writeAttribute(NAME_ATTR, image.getKey());
			try (InputStream imageData = image.getValue().getStream()) {
				try (OutputStream xmlData = Base64.getEncoder().wrap(new AsciiOutputStream(out))) {
					StreamUtilities.copyStreamContents(imageData, xmlData);
				}
			} catch (IOException ex) {
				log.error("Cannot convert image in attribute '" + attribute + "'.", ex);
			}
			out.writeEndElement();
		}
	}

	@Override
	public boolean useEmptyElement(TLStructuredTypePart attribute, StructuredText value) {
		return StringServices.isEmpty(value.getSourceCode());
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

	private static final class AsciiOutputStream extends OutputStream {
		private final XMLStreamWriter _out;
	
		private final char[] _buffer = new char[4096];
	
		private int _pos = 0;
	
	
		/**
		 * Creates a {@link AsciiOutputStream}.
		 *
		 * @param out
		 *        The {@link XMLStreamWriter} to write ASCII data to using
		 *        {@link XMLStreamWriter#writeCharacters(char[], int, int)}.
		 */
		public AsciiOutputStream(XMLStreamWriter out) {
			_out = out;
		}
	
		@Override
		public void write(int b) throws IOException {
			flush(1);
			enter(b);
		}

		@Override
		public void write(byte[] b, int off, final int len) throws IOException {
			final int stop = off + len;
			while (off < stop) {
				flush(len);
				for (int limit = Math.min(off + _buffer.length, stop); off < limit; off++) {
					enter(b[off]);
				}
			}
		}

		protected void enter(int b) throws IOException {
			if (b > 127) {
				throw new IOException("Not an ASCII character: " + b);
			}
			_buffer[_pos++] = (char)b;
		}
	
		private void flush(int requiredSize) throws IOException {
			if (_pos + requiredSize > _buffer.length) {
				flush();
			}
		}
	
		@Override
		public void flush() throws IOException {
			try {
				_out.writeCharacters(_buffer, 0, _pos);
				_pos = 0;
			} catch (XMLStreamException ex) {
				throw new IOException(ex);
			}
		}
	
		@Override
		public void close() throws IOException {
			flush();
			super.close();
		}
	}

}
