/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.io;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.Log;
import com.top_logic.basic.xml.log.XMLStreamLog;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Algorithm exporting and importing a model value to/from XML.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface AttributeValueBinding<T> {

	/**
	 * Reads a model value from the given reader.
	 * 
	 * <p>
	 * When this method is called, the given reader is positioned at the start
	 * tag of the configuration element that describes the configuration value
	 * to load.
	 * </p>
	 * 
	 * <p>
	 * If the method returns normally, the given reader must be positioned at
	 * the corresponding end tag of the element, on whose start tag the reader
	 * was positioned when this method was called.
	 * </p>
	 * 
	 * @param log
	 *        Output of error reports.
	 * @param in
	 *        the reader to read the value from.
	 * @param attribute
	 *        The attribute whose value is loaded.
	 * 
	 * @return the parsed value.
	 * 
	 * @throws XMLStreamException
	 *         If the import stream is not well-formed or does not conform to
	 *         the schema established by this binding.
	 */
	T loadValue(XMLStreamLog log, XMLStreamReader in, TLStructuredTypePart attribute) throws XMLStreamException;

	/**
	 * Marshals the given value to the given writer.
	 * 
	 * <p>
	 * When this method is called, the start tag representing the value has
	 * already been written to the given writer. The method implementation may
	 * write attributes to that start tag and/or additional element contents.
	 * </p>
	 * 
	 * <p>
	 * After this method returns normally, the writer must have closed all tags
	 * except the one that was open at the time this method was called.
	 * </p>
	 * 
	 * @param log
	 *        Output of error reports.
	 * @param out
	 *        The writer to write the given value to.
	 * @param attribute
	 *        The attribute whose value is stored.
	 * @param value
	 *        The value to write.
	 * 
	 * @throws XMLStreamException
	 *         If writing to the given writer fails.
	 */
	void storeValue(Log log, XMLStreamWriter out, TLStructuredTypePart attribute, T value) throws XMLStreamException;

	/**
	 * Whether {@link #storeValue(Log, XMLStreamWriter, TLStructuredTypePart, Object)} does not produce
	 * structured contents.
	 * 
	 * @param attribute
	 *        The attribute whose value is stored.
	 * @param value
	 *        The value to export.
	 */
	default boolean useEmptyElement(TLStructuredTypePart attribute, T value) {
		return false;
	}

}
