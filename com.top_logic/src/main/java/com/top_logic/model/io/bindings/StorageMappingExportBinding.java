/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.io.bindings;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.Log;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.basic.xml.log.XMLStreamLog;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.io.AttributeValueBinding;

/**
 * {@link AttributeValueBinding} based on the {@link StorageMapping} of an
 * {@link TLStructuredTypePart} with primitive type.
 * 
 * <p>
 * This binding works only for {@link StorageMapping}s using {@link CharSequence} as their storage
 * type. The value is stored in a CDATA section allowing arbitrary content. The <code>null</code>
 * value is encoded as empty contents. Therefore, the {@link StorageMapping} of the attribute must
 * not use the empty string as storage object for any non-<code>null</code> value.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StorageMappingExportBinding implements AttributeValueBinding<Object> {

	/**
	 * Singleton {@link StorageMappingExportBinding} instance.
	 */
	public static final StorageMappingExportBinding INSTANCE = new StorageMappingExportBinding();

	private StorageMappingExportBinding() {
		// Singleton constructor.
	}

	@Override
	public Object loadValue(XMLStreamLog log, XMLStreamReader in, TLStructuredTypePart attribute) throws XMLStreamException {
		String contents = XMLStreamUtil.nextText(in);
		if (contents.isEmpty()) {
			return null;
		}
		Object result;
		try {
			result = parseValue(attribute, contents);
		} catch (I18NRuntimeException ex) {
			log.error(in.getLocation(),
				I18NConstants.INVALID_FORMAT__VALUE_ATTR_DETAILS.fill(contents, attribute, ex.getErrorKey()));
			return null;
		}
		if (in.getEventType() != XMLStreamConstants.END_ELEMENT) {
			log.error(in.getLocation(), I18NConstants.EXPECTED_END_OF_ELEMENT);
			XMLStreamUtil.skipUpToMatchingEndTag(in);
		}
		return result;
	}

	private Object parseValue(TLStructuredTypePart attribute, String rawValue) {
		return storageMapping(attribute).getBusinessObject(rawValue);
	}

	@Override
	public void storeValue(Log log, XMLStreamWriter out, TLStructuredTypePart attribute, Object value)
			throws XMLStreamException {
		if (value != null) {
			out.writeCData(serializeValue(attribute, value));
		}
	}

	private String serializeValue(TLStructuredTypePart attribute, Object value) {
		CharSequence storageObject = (CharSequence) storageMapping(attribute).getStorageObject(value);
		return storageObject.toString();
	}

	private StorageMapping<?> storageMapping(TLStructuredTypePart attribute) {
		StorageMapping<?> result = ((TLPrimitive) attribute.getType()).getStorageMapping();
		return result;
	}

}
