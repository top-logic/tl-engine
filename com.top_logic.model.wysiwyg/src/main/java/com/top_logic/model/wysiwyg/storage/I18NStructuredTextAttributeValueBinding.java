/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.wysiwyg.storage;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.Log;
import com.top_logic.basic.xml.log.XMLStreamLog;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.io.AttributeValueBinding;

/**
 * {@link AttributeValueBinding} for {@link I18NStructuredText} values.
 */
public class I18NStructuredTextAttributeValueBinding implements AttributeValueBinding<I18NStructuredText> {

	/**
	 * Singleton {@link I18NStructuredTextAttributeValueBinding} instance.
	 */
	public static final I18NStructuredTextAttributeValueBinding INSTANCE =
		new I18NStructuredTextAttributeValueBinding();

	private I18NStructuredTextAttributeValueBinding() {
		// Singleton constructor.
	}

	@Override
	public I18NStructuredText loadValue(XMLStreamLog log, XMLStreamReader in, TLStructuredTypePart attribute)
			throws XMLStreamException {

		Map<Locale, StructuredText> content = new HashMap<>();
		while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
			String languageTag = in.getLocalName();
			Locale locale = Locale.forLanguageTag(languageTag);
			StructuredText structuredText = StructuredTextAttributeValueBinding.INSTANCE.loadValue(log, in, attribute);
			content.put(locale, structuredText);

			if (in.getEventType() != XMLStreamConstants.END_ELEMENT) {
				throw new XMLStreamException("Expecting to find end of tag, found: " + in.getEventType(),
					in.getLocation());
			}
			if (!languageTag.equals(in.getLocalName())) {
				throw new XMLStreamException(
					"Expecting to find end of tag '" + languageTag + "', found: " + in.getLocalName());
			}
		}
		return content.isEmpty() ? null : new I18NStructuredText(content);
	}

	@Override
	public void storeValue(Log log, XMLStreamWriter out, TLStructuredTypePart attribute, I18NStructuredText value)
			throws XMLStreamException {
		for (Entry<Locale, StructuredText> entry : value.getEntries().entrySet()) {
			String languageTag = entry.getKey().toLanguageTag();
			out.writeStartElement(languageTag);
			StructuredTextAttributeValueBinding.INSTANCE.storeValue(log, out, attribute, entry.getValue());
			out.writeEndElement();
		}
	}

}
