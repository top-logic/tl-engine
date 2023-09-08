/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.character.CharacterContent;

/**
 * Mechanism to open abstract {@link Content} as {@link XMLStreamReader}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class XMLContent implements Closeable {

	/**
	 * The {@link XMLStreamReader} reading the opened {@link Content}.
	 */
	public abstract XMLStreamReader reader();

	/**
	 * Utility converting an abstract {@link Content} to a {@link Source} for XML parsing.
	 * 
	 * @param source
	 *        The source {@link Content}.
	 * @return The parser {@link Source}.
	 */
	public static Source toSource(Content source) throws IOException {
		if (source instanceof BinaryContent) {
			return new StreamSource(((BinaryContent) source).getStream());
		}
		if (source instanceof CharacterContent) {
			return new StreamSource(((CharacterContent) source).getReader());
		}
		throw new AssertionError("Content must be either binary or character content.");
	}

	/**
	 * Opens the given abstract {@link Content} for XML parsing.
	 * 
	 * @param source
	 *        The {@link Content} to access.
	 * @return An {@link XMLContent} instance providing a stream.
	 */
	public static XMLContent open(Content source) throws IOException, XMLStreamException {
		return open(XMLStreamUtil.getDefaultInputFactory(), source);
	}

	/**
	 * Opens the given abstract {@link Content} for XML parsing.
	 * 
	 * @param factory
	 *        The {@link XMLInputFactory} to use.
	 * @param source
	 *        The {@link Content} to access.
	 * @return An {@link XMLContent} instance providing a stream.
	 */
	public static XMLContent open(XMLInputFactory factory, Content source) throws IOException, XMLStreamException {
		if (source instanceof BinaryContent) {
			return new FromStream(factory, (BinaryContent) source);
		}
		if (source instanceof CharacterContent) {
			return new FromChars(factory, (CharacterContent) source);
		}
		throw new AssertionError("Content must be either binary or character content.");
	}

	static class FromStream extends XMLContent {

		private InputStream _in;

		private final XMLStreamReader _reader;

		public FromStream(XMLInputFactory factory, BinaryContent source) throws IOException, XMLStreamException {
			boolean ok = false;

			_in = source.getStream();
			try {
				_reader = factory.createXMLStreamReader(source.toString(), _in);
				ok = true;
			} finally {
				if (!ok) {
					_in.close();
					_in = null;
				}
			}
		}

		@Override
		public XMLStreamReader reader() {
			return _reader;
		}

		@Override
		public void close() throws IOException {
			try {
				try {
					if (_reader != null) {
						_reader.close();
					}
				} catch (XMLStreamException ex) {
					// Ignore.
				}
			} finally {
				if (_in != null) {
					_in.close();
				}
			}
		}

	}

	static class FromChars extends XMLContent {

		private Reader _in;

		private final XMLStreamReader _reader;

		public FromChars(XMLInputFactory factory, CharacterContent source) throws IOException, XMLStreamException {
			boolean ok = false;

			_in = source.getReader();
			try {
				_reader = factory.createXMLStreamReader(source.toString(), _in);
				ok = true;
			} finally {
				if (!ok) {
					_in.close();
					_in = null;
				}
			}
		}

		@Override
		public XMLStreamReader reader() {
			return _reader;
		}

		@Override
		public void close() throws IOException {
			try {
				try {
					if (_reader != null) {
						_reader.close();
					}
				} catch (XMLStreamException ex) {
					// Ignore.
				}
			} finally {
				if (_in != null) {
					_in.close();
				}
			}
		}

	}

}