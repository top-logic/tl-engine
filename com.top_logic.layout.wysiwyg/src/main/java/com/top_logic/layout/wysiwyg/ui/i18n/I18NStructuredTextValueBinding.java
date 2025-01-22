/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.config.AbstractConfigurationValueBinding;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueBinding;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.StructuredTextValueBinding;

/**
 * {@link ConfigurationValueBinding} for storing {@link I18NStructuredText} to configuration.
 */
public class I18NStructuredTextValueBinding extends AbstractConfigurationValueBinding<I18NStructuredText> {

	/**
	 * Singleton {@link I18NStructuredTextValueBinding} instance.
	 */
	public static final I18NStructuredTextValueBinding INSTANCE = new I18NStructuredTextValueBinding();

	private I18NStructuredTextValueBinding() {
		// Singleton constructor.
	}

	@Override
	public I18NStructuredText loadConfigItem(XMLStreamReader in, I18NStructuredText baseValue)
			throws XMLStreamException, ConfigurationException {
		Map<Locale, StructuredText> result = new HashMap<>();
		read:
		while (true) {
			int next = in.next();
			switch (next) {
				case XMLStreamConstants.END_ELEMENT:
				case XMLStreamConstants.END_DOCUMENT:
					break read;
				case XMLStreamConstants.START_ELEMENT:
					String locale = in.getLocalName();
					StructuredText content = StructuredTextValueBinding.INSTANCE.loadConfigItem(in, null);
					result.put(Locale.forLanguageTag(locale), content);
					break;
			}
		}
		return new I18NStructuredText(result);
	}

	@Override
	public void saveConfigItem(XMLStreamWriter out, I18NStructuredText item) throws XMLStreamException {
		for (Entry<Locale, StructuredText> entry : item.getEntries().entrySet()) {
			out.writeStartElement(entry.getKey().toLanguageTag());
			StructuredTextValueBinding.INSTANCE.saveConfigItem(out, entry.getValue());
			out.writeEndElement();
		}
	}
}
