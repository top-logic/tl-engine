/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Settings;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.FileBasedBinaryContent;
import com.top_logic.basic.io.binary.InMemoryBinaryData;
import com.top_logic.basic.xml.document.ThreadSafeDOMFactory;

/**
 * Utility methods for processing DOM {@link Document}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DOMUtil extends com.top_logic.basic.core.xml.DOMUtil {

	/**
	 * Parse the given resource (from {@link FileManager}) as XML.
	 * 
	 * @param aPath The resource path.
	 * @return The parsed XML {@link Document}.
	 */
	public static Document parseResource(String aPath) throws SAXException, IOException {
		DocumentBuilder builder = getDocumentBuilder();
		try (InputStream in = FileManager.getInstance().getStream(aPath)) {
			return builder.parse(in);
		}
	}

	/**
	 * Serializes the given XML document to a file path using the {@link FileManager} path mapping.
	 * 
	 * @param filePath
	 *        The {@link FileManager} file name to write to.
	 * @param pretty
	 *        Whether the file should be written pretty.
	 * @param node
	 *        The node to serialize.
	 * @throws IOException
	 *         If writing to the given stream fails.
	 */
	public static void serializeXMLDocument(String filePath, boolean pretty, Node node) throws IOException {
		serializeXMLDocument(FileManager.getInstance().getIDEFile(filePath), pretty, node);
	}

	/**
	 * Serializes the given XML document to given file.
	 * 
	 * @param file
	 *        The file to write to.
	 * @param pretty
	 *        Whether the file should be written pretty.
	 * @param node
	 *        The node to serialize.
	 * @throws IOException
	 *         If writing to the given stream fails.
	 */
	public static void serializeXMLDocument(File file, boolean pretty, Node node) throws IOException {
		File parent = file.getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}

		try (FileOutputStream out = new FileOutputStream(file)) {
			serializeXMLDocument(out, pretty, node);
		}
	}

	/**
	 * Return a light weight, thread safe, and immutable implementation of a {@link Document}.
	 * 
	 * @param input
	 *        The string to be parsed into a {@link Document}, must not be <code>null</code>
	 * @return The thread safe implementation of the {@link Document}, never <code>null</code>.
	 */
	public static Document parseThreadSafe(String input) {
		return ThreadSafeDOMFactory.importDocument(DOMUtil.parse(input));
	}

	/**
	 * @see #parseStripped(XMLStreamReader)
	 */
	public static Document parseStripped(InputStream stream) throws XMLStreamException {
		XMLStreamReader in = XMLStreamUtil.getDefaultInputFactory().createXMLStreamReader(stream);
		return parseStripped(in);
	}

	/**
	 * Builds a stripped XML document from the given stream.
	 * 
	 * <p>
	 * In a stripped document, all text nodes are (see {@link String#trim() trimmed}. It does only
	 * contain element, text and comment nodes.
	 * </p>
	 * 
	 * <p>
	 * The input XML must not contain the special nodes:
	 * </p>
	 * 
	 * <ul>
	 * <li>{@link XMLStreamConstants#DTD}</li>
	 * <li>{@link XMLStreamConstants#ENTITY_DECLARATION}</li>
	 * <li>{@link XMLStreamConstants#ENTITY_REFERENCE}</li>
	 * <li>{@link XMLStreamConstants#NOTATION_DECLARATION}</li>
	 * <li>{@link XMLStreamConstants#PROCESSING_INSTRUCTION}</li>
	 * </ul>
	 * 
	 * @param in
	 *        The stream to read XML from.
	 * @return The parsed and stripped document.
	 */
	public static Document parseStripped(XMLStreamReader in) throws XMLStreamException {
		Document document = newDocument();
		appendStripped(in, document);
		return document;
	}

	/**
	 * Append the contents read from the given reader to the given node.
	 * 
	 * @param in
	 *        The XML to read.
	 * @param node
	 *        The DOM node to append the contents to.
	 * 
	 * @see #parseStripped(XMLStreamReader)
	 */
	public static void appendStripped(XMLStreamReader in, Node node) throws XMLStreamException {
		Document document = (node instanceof Document) ? (Document) node : node.getOwnerDocument();

		int depth = 0;

		Node current = node;
		loop:
		while (true) {
			int event = in.next();
			switch (event) {
				case XMLStreamConstants.START_DOCUMENT:
					break;
				case XMLStreamConstants.END_DOCUMENT:
					break loop;
				case XMLStreamConstants.START_ELEMENT:
					depth++;
					current = createCurrentElement(in, document, current);
					break;
				case XMLStreamConstants.END_ELEMENT:
					current = current.getParentNode();
					depth--;
					if (depth < 0) {
						return;
					}
					break;
				case XMLStreamConstants.CDATA:
				case XMLStreamConstants.CHARACTERS:
					char[] characters = in.getTextCharacters();
					int start = in.getTextStart();
					int last = start + in.getTextLength() - 1;
					while (start <= last && Character.isWhitespace(characters[start])) {
						start++;
					}
					while (last > start && Character.isWhitespace(characters[last])) {
						last--;
					}
					if (last >= start) {
						String value = String.valueOf(characters, start, last - start + 1);
						Text text = document.createTextNode(value);
						current.appendChild(text);
					}
					break;
				case XMLStreamConstants.COMMENT:
					Comment comment = document.createComment(in.getText());
					current.appendChild(comment);
					break;
				case XMLStreamConstants.SPACE:
					break;
				case XMLStreamConstants.ATTRIBUTE:
				case XMLStreamConstants.DTD:
				case XMLStreamConstants.ENTITY_DECLARATION:
				case XMLStreamConstants.ENTITY_REFERENCE:
				case XMLStreamConstants.NAMESPACE:
				case XMLStreamConstants.NOTATION_DECLARATION:
				case XMLStreamConstants.PROCESSING_INSTRUCTION:
					throw new IllegalArgumentException("Unsupported node type: " + event);
			}
		}
	}

	/**
	 * Serializes the given node to an XML string representation without XML header.
	 * 
	 * @see #serializeXMLDocument(OutputStream, boolean, Node)
	 */
	public static String toStringRaw(Node node) {
		return toString(node, false, false);
	}

	/**
	 * Serializes the given node to an XML string representation.
	 * 
	 * @see #serializeXMLDocument(OutputStream, boolean, Node)
	 */
	public static String toString(Node node) {
		return toString(node, true, false);
	}

	/**
	 * Serializes the given node to an XML string representation pretty printing the contents.
	 * 
	 * @see #serializeXMLDocument(OutputStream, boolean, Node)
	 */
	public static String toStringPretty(Node node) {
		return toString(node, true, true);
	}

	/**
	 * Serializes the given node to an XML string representation.
	 * 
	 * @see #serializeXMLDocument(OutputStream, boolean, Node)
	 */
	public static String toString(Node node, boolean header, boolean pretty) {
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			serializeXMLDocument(buffer, header, pretty, node);
			return buffer.toString("utf-8");
		} catch (IOException ex) {
			throw (AssertionError) new AssertionError("Serialization failure.").initCause(ex);
		}
	}

	/**
	 * Serializes the given node to the given stream.
	 * 
	 * @param out
	 *        The output specification to write to. Note: This stream is not closed by this method.
	 * @param prettyPrint
	 *        Whether the output should be printed pretty.
	 * @param node
	 *        The node to serialize.
	 * 
	 * @throws IOException
	 *         If writing to the given stream fails.
	 */
	public static void serializeXMLDocument(OutputStream out, boolean prettyPrint, Node node) throws IOException {
		serializeXMLDocument(out, true, prettyPrint, node);
	}

	/**
	 * Serializes the given node to the given stream.
	 * 
	 * @param out
	 *        The output specification to write to. Note: This stream is not closed by this method.
	 * @param prettyPrint
	 *        Whether the output should be printed pretty.
	 * @param header
	 *        Whether to print the XML header.
	 * @param node
	 *        The node to serialize.
	 * 
	 * @throws IOException
	 *         If writing to the given stream fails.
	 */
	public static void serializeXMLDocument(OutputStream out, boolean header, boolean prettyPrint, Node node)
			throws IOException {
		XMLPrettyPrinter.Config config = XMLPrettyPrinter.newConfiguration();
		if (!prettyPrint) {
			config.setNoIndent(true);
			/* When white spaces are *not* preserved, then the semantic of text nodes with multiple
			 * lines are changed, because the text is normalized in some kind. */
			config.setPreserveWhitespace(true);
		}
		config.setXMLHeader(header);
		try (XMLPrettyPrinter prettyPrinter = new XMLPrettyPrinter(out, config)) {
			prettyPrinter.write(node);
			prettyPrinter.getOut().flush();
		}
	}

	/**
	 * This method returns a {@link BinaryContent} with the given name, whose {@link InputStream}
	 * returns the serialized given node.
	 * 
	 * @param node
	 *        The node to get {@link BinaryContent} for.
	 * @param name
	 *        The name of the returned {@link BinaryContent}
	 */
	@SuppressWarnings("unused")
	public static BinaryContent toBinaryContent(Node node, String name) {
		if (false) {
			try {
				File f = File.createTempFile(name, ".xml", Settings.getInstance().getTempDir());
				FileOutputStream fos = new FileOutputStream(f);
				serializeXMLDocument(fos, true, node);
				return FileBasedBinaryContent.createBinaryContent(f);
			} catch (IOException ex) {
				throw new IOError(ex);
			}
		}
		InMemoryBinaryData result = new InMemoryBinaryData("text/xml", name);
		try {
			serializeXMLDocument(result, true, node);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		return result;
	}

}
