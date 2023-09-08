/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.io.bindings;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.Log;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.basic.xml.log.XMLStreamLog;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.io.AttributeValueBinding;

/**
 * Base class for {@link AttributeValueBinding}s storing a single string value.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractPrimitiveAttributeValueBinding<T> implements AttributeValueBinding<T> {

	private static final String VALUE_ATTR = "value";

	@Override
	public T loadValue(XMLStreamLog log, XMLStreamReader in, TLStructuredTypePart attribute) throws XMLStreamException {
		String rawValue = in.getAttributeValue(null, VALUE_ATTR);
		T result;
		if (rawValue == null) {
			result = null;
		} else {
			result = parseValue(log, in, attribute, rawValue);
		}
		XMLStreamUtil.skipUpToMatchingEndTag(in);
		return result;
	}

	/**
	 * Parses the given raw value.
	 * 
	 * @param attribute
	 *        The attribute whose value is loaded.
	 * @param rawValue
	 *        The serialized (exported) representation of the value.
	 */
	protected abstract T parseValue(XMLStreamLog log, XMLStreamReader in, TLStructuredTypePart attribute, String rawValue);

	@Override
	public void storeValue(Log log, XMLStreamWriter out, TLStructuredTypePart attribute, T value) throws XMLStreamException {
		if (value != null) {
			out.writeAttribute(VALUE_ATTR, serializeValue(attribute, value));
		}
	}

	/**
	 * Serialized the given attribute value into a string representation.
	 * 
	 * @param attribute
	 *        The attribute whose value is loaded.
	 * @param value
	 *        The value to write.
	 */
	protected abstract String serializeValue(TLStructuredTypePart attribute, T value);

	@Override
	public boolean useEmptyElement(TLStructuredTypePart attribute, T value) {
		return true;
	}
}
