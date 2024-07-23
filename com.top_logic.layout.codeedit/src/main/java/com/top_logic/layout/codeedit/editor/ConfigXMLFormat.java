/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.codeedit.editor;

import java.io.IOError;
import java.io.StringWriter;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.basic.xml.XMLPrettyPrinter.Config;
import com.top_logic.layout.form.model.DescriptiveParsePosition;
import com.top_logic.util.Resources;

/**
 * {@link Format} for parsing/displaying a {@link ConfigurationItem}-valued property as XML.
 */
public final class ConfigXMLFormat extends Format {

	private final Map<String, ConfigurationDescriptor> _descriptorsByTag = new HashMap<>();

	private final Map<ConfigurationDescriptor, String> _tagForDescriptor = new HashMap<>();

	private final PropertyDescriptor _property;

	/**
	 * Creates a {@link ConfigXMLFormat}.
	 */
	public ConfigXMLFormat(PropertyDescriptor property) {
		_property = property;

		for (String elementName : property.getElementNames()) {
			ConfigurationDescriptor elementDescriptor = property.getElementDescriptor(elementName);
			String tag;
			if (elementName == null) {
				// The general descriptor of an item-valued property has no tag name.
				tag = property.getPropertyName();
			} else {
				tag = elementName;
			}
			_descriptorsByTag.put(tag, elementDescriptor);
			_tagForDescriptor.put(elementDescriptor, tag);
		}
	}

	@Override
	public StringBuffer format(Object value, StringBuffer toAppendTo, FieldPosition pos) {
		StringWriter buffer = new StringWriter();

		try {
			ConfigurationItem config = (ConfigurationItem) value;
			try (ConfigurationWriter writer = new ConfigurationWriter(buffer)) {
				String tag = _tagForDescriptor.get(config.descriptor());
				if (tag == null) {
					tag = _property.getPropertyName();
				}
				writer.write(tag, _descriptorsByTag.get(tag), config);
			}
		} catch (XMLStreamException ex) {
			throw new IOError(ex);
		}

		Config prettyConfig = XMLPrettyPrinter.newConfiguration();
		prettyConfig.setXMLHeader(false);
		prettyConfig.setIndentChar(' ');
		prettyConfig.setIndentStep(4);
		prettyConfig.setMaxIndent(80);
		String xml = XMLPrettyPrinter.prettyPrint(prettyConfig, buffer.toString());
		toAppendTo.append(xml);
		return toAppendTo;
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		try {
			DefaultInstantiationContext context = new DefaultInstantiationContext(ConfigXMLEditor.class);
			ConfigurationItem result = new ConfigurationReader(context, _descriptorsByTag)
				.setSource(CharacterContents.newContent(source)).read();
			pos.setIndex(source.length());
			return result;
		} catch (ConfigurationError | ConfigurationException ex) {
			pos.setErrorIndex(0);
			if (pos instanceof DescriptiveParsePosition) {
				((DescriptiveParsePosition) pos)
					.setErrorDescription(Resources.getInstance().getString(ex.getErrorKey()));
			}
			return null;
		}
	}
}