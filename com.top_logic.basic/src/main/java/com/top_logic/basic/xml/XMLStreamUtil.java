/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.StringServices;

/**
 * Utilities for Stax XML processing using {@link XMLStreamReader} and
 * {@link XMLStreamWriter}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XMLStreamUtil {

	public static final Pattern WHITESPACES_ONLY = Pattern.compile("\\s*");

	/**
	 * Description of the location of a parse error.
	 * 
	 * @param reader
	 *        The reader from which the location should be extracted.
	 * @return A user-readable description of the position of the given reader.
	 */
	public static String atLocation(XMLStreamReader reader) {
		return atLocation(reader.getLocation());
	}

	/**
	 * Description of the location of a parse error.
	 * 
	 * @param ex
	 *        The exception thrown from a parse.
	 * @return A user-readable description of the position of the given reader.
	 */
	public static String atLocation(XMLStreamException ex) {
		return atLocation(ex.getLocation());
	}

	/**
	 * Description of the location of a parse error.
	 * 
	 * @param location
	 *        The {@link Location} from the reader or exception.
	 * @return A user-readable description of the position of the given reader.
	 */
	public static String atLocation(Location location) {
		String file = location.getSystemId();
		return "at " + (StringServices.isEmpty(file) ? "" : file + " ") + "line " + location.getLineNumber()
			+ " column " + location.getColumnNumber();
	}

	/**
	 * Skip all events up to the next matching
	 * {@link XMLStreamConstants#END_ELEMENT} event.
	 * 
	 * @param reader
	 *        The reader to take events from. The reader is assumed to be
	 *        positioned on a {@link XMLStreamConstants#START_ELEMENT} event.
	 *        All events up to the matching end element are consumed.
	 * @return The last event consumed from the given reader (is always
	 *         {@link XMLStreamConstants#END_ELEMENT}.
	 * @throws XMLStreamException
	 *         If event reading fails.
	 */
	public static int skipUpToMatchingEndTag(XMLStreamReader reader) throws XMLStreamException {
		int depth = 0;
		int skipEvent;
		while (! (((skipEvent = reader.next()) == XMLStreamConstants.END_ELEMENT) && (depth == 0))) {
			switch (skipEvent) {
			case XMLStreamConstants.START_ELEMENT: 
				depth++; 
				break;
				
			case XMLStreamConstants.END_ELEMENT: 
				depth--; 
				break;
			}
		}
		return skipEvent;
	}

	/**
	 * Ensures that {@link XMLStreamReader#nextTag()} returns
	 * {@link XMLStreamConstants#START_ELEMENT}.
	 * 
	 * @param reader
	 *        The reader to read the next tag from.
	 * @throws XMLStreamException
	 *         If reading fails, or no start element is encountered.
	 */
	public static void nextStartTag(XMLStreamReader reader) throws XMLStreamException {
		reader.nextTag();
		ensureStartElement(reader);
	}

	/**
	 * Ensures that {@link XMLStreamReader#getEventType()} currently points to a
	 * {@link XMLStreamConstants#START_ELEMENT}.
	 * 
	 * @param reader
	 *        The reader to read the next tag from.
	 * @throws XMLStreamException
	 *         If reading fails, or no start element is encountered.
	 */
	public static void ensureStartElement(XMLStreamReader reader) throws XMLStreamException {
		int tag = reader.getEventType();
		if (tag != XMLStreamReader.START_ELEMENT) {
			throw new XMLStreamException("Start element expected, found '" + getEventName(tag) + "'.", reader.getLocation());
		}
	}

	/**
	 * Ensures that {@link XMLStreamReader#nextTag()} returns
	 * {@link XMLStreamConstants#END_ELEMENT}.
	 * 
	 * @param reader
	 *        The reader to read the next tag from.
	 * @throws XMLStreamException
	 *         If reading fails, or no start element is encountered.
	 */
	public static void nextEndTag(XMLStreamReader reader) throws XMLStreamException {
		reader.nextTag();
		ensureEndTag(reader);
	}

	/**
	 * Ensures that {@link XMLStreamReader#getEventType()} points to an
	 * {@link XMLStreamConstants#END_ELEMENT}.
	 * 
	 * @param reader
	 *        The reader to read the next tag from.
	 * @throws XMLStreamException
	 *         If reading fails, or no start element is encountered.
	 */
	public static void ensureEndTag(XMLStreamReader reader) throws XMLStreamException {
		int tag = reader.getEventType();
		if (tag != XMLStreamReader.END_ELEMENT) {
			throw new XMLStreamException("End element expected, found '" + getEventName(tag) + "'.", reader.getLocation());
		}
	}

	/**
	 * Reads text contents up to the next element boundary.
	 * 
	 * @param reader
	 *        The reader to get events from.
	 * @return The read text string.
	 * @throws XMLStreamException
	 *         If reading fails.
	 * @throws IllegalStateException
	 *         If the method is called at a position in the document, where no
	 *         text content can be found.
	 */
	public static String nextText(XMLStreamReader reader) throws XMLStreamException {
		String first = null;
		StringBuilder buffer = null;
		while (true) {
			int tag = reader.next();
			switch (tag) {
			case XMLStreamConstants.CDATA: 
			case XMLStreamConstants.CHARACTERS: 
			case XMLStreamConstants.SPACE: {
				String next = reader.getText();
				if (first == null) {
					first = next;
				} else if (buffer == null) {
					buffer = new StringBuilder(first.length() + next.length());
					buffer.append(first);
					buffer.append(next);
				} else {
					buffer.append(next);
				}
				break;
			}
			
			case XMLStreamConstants.START_ELEMENT: 
			case XMLStreamConstants.START_DOCUMENT: 
			case XMLStreamConstants.END_DOCUMENT: 
			case XMLStreamConstants.END_ELEMENT: {
				if (first == null) {
					return "";
				} else if (buffer == null) {
					return first;
				} else {
					return buffer.toString();
				}
			}
			
			case XMLStreamConstants.ENTITY_DECLARATION:
			case XMLStreamConstants.NAMESPACE:
			case XMLStreamConstants.NOTATION_DECLARATION:
			case XMLStreamConstants.ATTRIBUTE: {
				throw new IllegalStateException("No text can be found within an " + getEventName(tag) + " tag.");
			}
				
			default: {
				// Skip
				break;
			}
			}			
		}
	}

	
	/**
	 * Transforms the tag type to a user-readable string.
	 */
	public static String getEventName(int eventType) {
		switch (eventType) {
		case XMLStreamConstants.ATTRIBUTE: return "attribute";
		case XMLStreamConstants.CDATA: return "cdata";
		case XMLStreamConstants.CHARACTERS: return "characters";
		case XMLStreamConstants.COMMENT: return "comment";
		case XMLStreamConstants.DTD: return "dtd";
		case XMLStreamConstants.END_DOCUMENT: return "end of document";
		case XMLStreamConstants.END_ELEMENT: return "end element";
		case XMLStreamConstants.ENTITY_DECLARATION: return "entity declaration";
		case XMLStreamConstants.NAMESPACE: return "namespace";
		case XMLStreamConstants.NOTATION_DECLARATION: return "notation declaration";
		case XMLStreamConstants.PROCESSING_INSTRUCTION: return "procesing instruction";
		case XMLStreamConstants.SPACE: return "space";
		case XMLStreamConstants.START_DOCUMENT: return "start document";
		case XMLStreamConstants.START_ELEMENT: return "start element";
		default: throw new IllegalArgumentException("No such tag type: " + eventType);
		}
	}

	/**
	 * Write the given attribute, if the value does not match its default value.
	 * 
	 * @param writer
	 *        The writer to write to.
	 * @param attr
	 *        the attribute local name.
	 * @param value
	 *        The attribute value to write.
	 * @param defaultValue
	 *        The default value of the attribute.
	 * @throws XMLStreamException
	 *         If the underlying writer fails.
	 */
	public static void writeIntAttribute(XMLStreamWriter writer, String attr, int value, int defaultValue) throws XMLStreamException {
		if (value != defaultValue) {
			writeIntAttribute(writer, attr, value);
		}
	}
	
	/**
	 * Write the given attribute unconditionally.
	 * 
	 * @param writer
	 *        The writer to write to.
	 * @param attr
	 *        the attribute local name.
	 * @param value
	 *        The attribute value to write.
	 * @throws XMLStreamException
	 *         If the underlying writer fails.
	 */
	public static void writeIntAttribute(XMLStreamWriter writer, String attr, int value) throws XMLStreamException {
		writer.writeAttribute(attr, Integer.toString(value));
	}

	/**
	 * Reads an attribute as <code>int</code> value.
	 * 
	 * @param reader
	 *        The {@link XMLStreamReader} to read the attribute from.
	 * @param namespace
	 *        The namespace of the attribute to read.
	 * @param attr
	 *        The local name of the attributes.
	 * @param defaultValue
	 *        The default value to use, if there is no such attribute.
	 * @return The <code>int</code> value of the requested attributes.
	 * @throws XMLStreamException
	 *         If the underlying writer fails.
	 */
	public static int readIntAttribute(XMLStreamReader reader, String namespace, String attr, int defaultValue) throws XMLStreamException {
		String valueString = reader.getAttributeValue(namespace, attr);
		if (valueString == null) {
			return defaultValue;
		} else {
			return Integer.parseInt(valueString);
		}
	}
	
	/**
	 * Write the given attribute, if the value does not match its default value.
	 * 
	 * @param writer
	 *        The writer to write to.
	 * @param attr
	 *        the attribute local name.
	 * @param value
	 *        The attribute value to write.
	 * @param defaultValue
	 *        The default value of the attribute.
	 * @throws XMLStreamException
	 *         If the underlying writer fails.
	 */
	public static void writeLongAttribute(XMLStreamWriter writer, String attr, long value, long defaultValue) throws XMLStreamException {
		if (value != defaultValue) {
			writeLongAttribute(writer, attr, value);
		}
	}
	
	/**
	 * Write the given attribute unconditionally.
	 * 
	 * @param writer
	 *        The writer to write to.
	 * @param attr
	 *        the attribute local name.
	 * @param value
	 *        The attribute value to write.
	 * @throws XMLStreamException
	 *         If the underlying writer fails.
	 */
	public static void writeLongAttribute(XMLStreamWriter writer, String attr, long value) throws XMLStreamException {
		writer.writeAttribute(attr, Long.toString(value));
	}
	
	/**
	 * Reads an attribute as <code>long</code> value.
	 * 
	 * @param reader
	 *        The {@link XMLStreamReader} to read the attribute from.
	 * @param namespace
	 *        The namespace of the attribute to read.
	 * @param attr
	 *        The local name of the attributes.
	 * @param defaultValue
	 *        The default value to use, if there is no such attribute.
	 * @return The <code>long</code> value of the requested attributes.
	 * @throws XMLStreamException
	 *         If the underlying writer fails.
	 */
	public static long readLongAttribute(XMLStreamReader reader, String namespace, String attr, long defaultValue) throws XMLStreamException {
		String valueString = reader.getAttributeValue(namespace, attr);
		if (valueString == null) {
			return defaultValue;
		} else {
			return Long.parseLong(valueString);
		}
	}
	
	/**
	 * Write the given attribute, if the value does not match its default value.
	 * 
	 * @param writer
	 *        The writer to write to.
	 * @param attr
	 *        the attribute local name.
	 * @param value
	 *        The attribute value to write.
	 * @param defaultValue
	 *        The default value of the attribute.
	 * @throws XMLStreamException
	 *         If the underlying writer fails.
	 */
	public static void writeBooleanAttribute(XMLStreamWriter writer, String attr, boolean value, boolean defaultValue) throws XMLStreamException {
		if (value != defaultValue) {
			writeBooleanAttribute(writer, attr, value);
		}
	}
	
	/**
	 * Write the given attribute unconditionally.
	 * 
	 * @param writer
	 *        The writer to write to.
	 * @param attr
	 *        the attribute local name.
	 * @param value
	 *        The attribute value to write.
	 * @throws XMLStreamException
	 *         If the underlying writer fails.
	 */
	public static void writeBooleanAttribute(XMLStreamWriter writer, String attr, boolean value) throws XMLStreamException {
		writer.writeAttribute(attr, Boolean.toString(value));
	}

	/**
	 * Write the given attribute, if the value does not match its default value.
	 * 
	 * @param writer
	 *        The writer to write to.
	 * @param attr
	 *        the attribute local name.
	 * @param names
	 *        Mapping of values to XML token names.
	 * @param value
	 *        The attribute value to write.
	 * @param defaultValue
	 *        The default value of the attribute.
	 * @throws XMLStreamException
	 *         If the underlying writer fails.
	 */
	public static <T> void writeEnumAttribute(XMLStreamWriter writer, String attr, Map<? super T, String> names, T value, T defaultValue) throws XMLStreamException {
		if (value != defaultValue) {
			writeEnumAttribute(writer, attr, names, value);
		}
	}
	
	/**
	 * Write the given attribute unconditionally.
	 * 
	 * @param writer
	 *        The writer to write to.
	 * @param attr
	 *        the attribute local name.
	 * @param value
	 *        The attribute value to write.
	 * @throws XMLStreamException
	 *         If the underlying writer fails.
	 */
	public static <T> void writeEnumAttribute(XMLStreamWriter writer, String attr, Map<? super T, String> names, T value) throws XMLStreamException {
		String valueName = names.get(value);
		assert valueName != null : "Enum name mapping is not complete: " + value;
		writer.writeAttribute(attr, valueName);
	}

	/**
	 * Starts the given element either with regular start tag, or with an empty
	 * tag.
	 * 
	 * @param writer
	 *        The writer to write to.
	 * @param hasContents
	 *        Whether the element has contents an must opened with a regular
	 *        start tag.
	 * @param namespaceURI
	 *        The elements's namespace URI.
	 * @param localName
	 *        The elements local name.
	 * @throws XMLStreamException
	 *         If the underlying writer fails.
	 */
	public static void writeStartElement(XMLStreamWriter writer, boolean hasContents, String namespaceURI, String localName) throws XMLStreamException {
		if (hasContents) {
			writer.writeStartElement(namespaceURI, localName);
		} else {
			writer.writeEmptyElement(namespaceURI, localName);
		}
	}

	/**
	 * Closes the current element wither with a regular end tag, or implicitly.
	 * 
	 * @param writer
	 *        The writer to write to.
	 * @param hasContents
	 *        Whether the element has contents an must closed explicitly using
	 *        an end tag.
	 */
	public static void writeEndElement(XMLStreamWriter writer, boolean hasContents) throws XMLStreamException {
		if (hasContents) {
			writer.writeEndElement();
		}
	}

	/**
	 * Class loader lock for initializating factories.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private static class Lock {

		public static final XMLOutputFactory OUTPUT_FACTORY;
		public static final XMLInputFactory INPUT_FACTORY;
		
		static {
			OUTPUT_FACTORY = XMLOutputFactory.newInstance();
			OUTPUT_FACTORY.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
			INPUT_FACTORY = XMLInputFactory.newInstance();
			INPUT_FACTORY.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
			INPUT_FACTORY.setProperty(XMLInputFactory.IS_COALESCING, true);
			hardenAgainsXXEAttacks(INPUT_FACTORY);
		}

	}

	/**
	 * XML eXternal Entity injection (XXE), which is now part of the OWASP Top 10, is a type of
	 * attack against an application that parses XML input. This attack occurs when untrusted XML
	 * input containing a reference to an external entity is processed by a weakly configured XML
	 * parser. This attack may lead to the disclosure of confidential data, denial of service,
	 * Server Side Request Forgery (SSRF), port scanning from the perspective of the machine where
	 * the parser is located, and other system impacts.
	 * 
	 * @see "https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#Java"
	 */
	public static void hardenAgainsXXEAttacks(XMLInputFactory inputFactory) {
		// This disables DTDs entirely for that factory
		inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		
		// disable external entities
		inputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
	}
	
	/**
	 * The systems default {@link XMLOutputFactory}.
	 * 
	 * <p>
	 * The result must not be modified in any way.
	 * </p>
	 */
	public static XMLOutputFactory getDefaultOutputFactory() {
		return Lock.OUTPUT_FACTORY;
	}
	
	/**
	 * The systems default {@link XMLInputFactory}.
	 * 
	 * <p>
	 * The result must not be modified in any way.
	 * </p>
	 */
	public static XMLInputFactory getDefaultInputFactory() {
		return Lock.INPUT_FACTORY;
	}

	/**
	 * Skips {@link XMLStreamConstants#SPACE} and {@link XMLStreamConstants#COMMENT} events and
	 * {@link XMLStreamConstants#CHARACTERS} events containing only whitespaces.
	 * <p>
	 * <b>Does nothing, if the current tag is already no whitespace and comment.</b>
	 * </p>
	 */
	public static void skipWhitespaceAndComments(XMLStreamReader reader) {
		while ((reader.getEventType() == XMLStreamConstants.COMMENT) || isAtWhitespaceText(reader)) {
			nextEvent(reader);
		}
	}

	private static boolean isAtWhitespaceText(XMLStreamReader reader) {
		if (reader.getEventType() == XMLStreamConstants.SPACE) {
			return true;
		}
		if (reader.getEventType() == XMLStreamConstants.CHARACTERS) {
			return WHITESPACES_ONLY.matcher(reader.getText()).matches();
		}
		return false;
	}

	/**
	 * Skips all events until the next tag with the given name and namespace is found.
	 * <p>
	 * Only the event types {@link XMLStreamConstants#START_ELEMENT} and
	 * {@link XMLStreamConstants#END_ELEMENT} are considered as matches. <br/>
	 * If the current event already matches the tag name and the namespace, nothing is done.
	 * </p>
	 * 
	 * @throws RuntimeException
	 *         If anything goes wrong, like the end of the file is hit.
	 */
	public static void skipToTag(XMLStreamReader reader, String tagName, String namespace) {
		while (!isAtTag(reader, tagName, namespace)) {
			skipToNextTag(reader);
		}
	}

	/**
	 * Skips all events until the next start tag with the given name and namespace is found.
	 * <p>
	 * Only the event type {@link XMLStreamConstants#START_ELEMENT} is considered as match. <br/>
	 * If the current event already matches the tag name and the namespace, nothing is done.
	 * </p>
	 * 
	 * @throws RuntimeException
	 *         If anything goes wrong, like the end of the file is hit.
	 */
	public static void skipToStartTag(XMLStreamReader reader, String tagName, String namespace) {
		while ((!isAtStartTag(reader)) || !isAtTag(reader, tagName, namespace)) {
			skipToNextTag(reader);
		}
	}

	/**
	 * Skips all events until the next end tag with the given name and namespace is found.
	 * <p>
	 * Only the event type {@link XMLStreamConstants#END_ELEMENT} is considered as match. <br/>
	 * If the current event already matches the tag name and the namespace, nothing is done.
	 * </p>
	 * 
	 * @throws RuntimeException
	 *         If anything goes wrong, like the end of the file is hit.
	 */
	public static void skipToEndTag(XMLStreamReader reader, String tagName, String namespace) {
		while ((!isAtEndTag(reader)) || !isAtTag(reader, tagName, namespace)) {
			skipToNextTag(reader);
		}
	}

	/**
	 * Returns <code>true</code> if the current event is {@link XMLStreamConstants#START_ELEMENT} or
	 * {@link XMLStreamConstants#END_ELEMENT} and has the given tag name and namespace.
	 * 
	 * @param tagName
	 *        Is allowed to be <code>null</code>.
	 * @param namespace
	 *        Is allowed to be <code>null</code>.
	 */
	public static boolean isAtTag(XMLStreamReader reader, String tagName, String namespace) {
		return isAtTag(reader)
			&& StringServices.equals(reader.getLocalName(), tagName)
			&& StringServices.equals(reader.getNamespaceURI(), namespace);
	}

	/**
	 * Skips all events until the next {@link XMLStreamConstants#START_ELEMENT} or
	 * {@link XMLStreamConstants#END_ELEMENT}.
	 * <p>
	 * If the current event is already a matching event, nothing is done.
	 * </p>
	 * 
	 * @throws RuntimeException
	 *         If anything goes wrong, like the end of the file is hit.
	 */
	public static void skipToTag(XMLStreamReader reader) {
		if (!isAtTag(reader)) {
			skipToNextTag(reader);
		}
	}

	/**
	 * Skips all events until the next {@link XMLStreamConstants#START_ELEMENT} or
	 * {@link XMLStreamConstants#END_ELEMENT}.
	 * <p>
	 * The current event is not considered. That means, {@link XMLStreamReader#next()} is called at
	 * least once.
	 * </p>
	 * 
	 * @throws RuntimeException
	 *         If something goes wrong, like the end of the file is hit.
	 */
	public static void skipToNextTag(XMLStreamReader reader) {
		do {
			ensureNotEOF(reader);
			nextEvent(reader);
		} while (!isAtTag(reader));
	}

	/**
	 * Calls {@link #skipUpToMatchingEndTag(XMLStreamReader)} but throws a {@link RuntimeException}
	 * instead of an {@link XMLStreamException}.
	 */
	public static int skipToMatchingEndTag(XMLStreamReader reader) {
		try {
			return skipUpToMatchingEndTag(reader);
		} catch (XMLStreamException exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * Calls {@link XMLStreamReader#next()} but throws a {@link RuntimeException} instead of an
	 * {@link XMLStreamException}.
	 */
	public static int nextEvent(XMLStreamReader reader) {
		try {
			return reader.next();
		} catch (XMLStreamException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * @throws RuntimeException
	 *         If {@link #hasNext(XMLStreamReader)} returns false.
	 */
	public static void ensureNotEOF(XMLStreamReader reader) {
		if (!hasNext(reader)) {
			throw new RuntimeException("Unexpected end of file.");
		}
	}

	/**
	 * Calls {@link XMLStreamReader#hasNext()} but catches the {@link XMLStreamException} and throws
	 * a {@link RuntimeException} instead.
	 */
	public static boolean hasNext(XMLStreamReader reader) {
		try {
			return reader.hasNext();
		} catch (XMLStreamException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Is the given {@link XMLStreamReader} currently at a (start or end) tag?
	 */
	public static boolean isAtTag(XMLStreamReader reader) {
		return isAtStartTag(reader) || isAtEndTag(reader);
	}

	/**
	 * Is the given {@link XMLStreamReader} currently at a start tag?
	 */
	public static boolean isAtStartTag(XMLStreamReader reader) {
		return reader.getEventType() == XMLStreamConstants.START_ELEMENT;
	}

	/**
	 * Is the given {@link XMLStreamReader} currently at an end tag?
	 */
	public static boolean isAtEndTag(XMLStreamReader reader) {
		return reader.getEventType() == XMLStreamConstants.END_ELEMENT;
	}

	/**
	 * Indirection for compatibility with certain `java.xml.stream` libraries that erroneously
	 * return the empty string as namespace for attributes without namespace.
	 * 
	 * @see XMLStreamReader#getAttributeNamespace(int)
	 */
	public static String getAttributeNamespace(XMLStreamReader xmlStreamReader, int index) {
		return StringServices.nonEmpty(xmlStreamReader.getAttributeNamespace(index));
	}

}
