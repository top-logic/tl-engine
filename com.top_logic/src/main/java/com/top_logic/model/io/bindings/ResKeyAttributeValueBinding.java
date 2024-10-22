/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.io.bindings;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.ConfigurationValueBinding;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.basic.xml.log.XMLStreamLog;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.io.AttributeValueBinding;

/**
 * Adapter that allows exporting values of type {@link ResKey} based on their
 * {@link ConfigurationValueBinding}.
 */
public class ResKeyAttributeValueBinding implements AttributeValueBinding<ResKey> {

	/**
	 * Top level tag to write the resource key to. This is required to also allow storing
	 * <code>null</code> values (then this tag is missing).
	 */
	private static final String RESOURCE_TAG = "resource";

	/**
	 * Singleton {@link ResKeyAttributeValueBinding} instance.
	 */
	public static final ResKeyAttributeValueBinding INSTANCE = new ResKeyAttributeValueBinding();

	private ResKeyAttributeValueBinding() {
		// Singleton constructor.
	}

	@Override
	public ResKey loadValue(XMLStreamLog log, XMLStreamReader in, TLStructuredTypePart attribute)
			throws XMLStreamException {
		int tag = in.nextTag();
		if (tag == XMLStreamConstants.START_ELEMENT) {
			if (!RESOURCE_TAG.equals(in.getLocalName())) {
				throw new XMLStreamException("Expected '" + RESOURCE_TAG + "' element, but found: " + in.getLocalName(),
					in.getLocation());
			}

			ResKey result = ResKey.ValueBinding.INSTANCE.loadConfigItem(in, null);

			XMLStreamUtil.ensureEndTag(in);
			if (!RESOURCE_TAG.equals(in.getLocalName())) {
				throw new XMLStreamException(
					"Expected end of '" + RESOURCE_TAG + "' element, but found: " + in.getLocalName(),
					in.getLocation());
			}

			in.nextTag();
			return result;
		} else {
			return null;
		}
	}

	@Override
	public void storeValue(Log log, XMLStreamWriter out, TLStructuredTypePart attribute, ResKey value)
			throws XMLStreamException {
		if (value != null) {
			out.writeStartElement(RESOURCE_TAG);
			ResKey.ValueBinding.INSTANCE.saveConfigItem(out, value);
			out.writeEndElement();
		}
	}

}
