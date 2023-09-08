/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.StringValueProvider;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * {@link Handler} assigning the text content of an element a model property.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TextContentImport<C extends TextContentImport.Config<?>> extends StoreHandler<C> {

	/**
	 * Configuration options for {@link TextContentImport}.
	 */
	@TagName("text-content")
	public interface Config<I extends TextContentImport<?>> extends StoreHandler.Config<I> {

		/**
		 * The {@link ConfigurationValueProvider} that converts the XML text value to a model
		 * property value.
		 */
		@ItemDefault(StringValueProvider.class)
		PolymorphicConfiguration<ConfigurationValueProvider<?>> getFormat();

		/**
		 * Whether to use the text value as identifier for the imported object.
		 */
		boolean getAssignId();

	}

	private final ConfigurationValueProvider<?> _format;

	/**
	 * Creates a {@link TextContentImport} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TextContentImport(InstantiationContext context, C config) {
		super(context, config);
		_format = context.getInstance(config.getFormat());
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		try {
			String rawValue = XMLStreamUtil.nextText(in);
			if (!in.isEndElement()) {
				context.error(in.getLocation(), I18NConstants.ERROR_EXPECTED_END_OF_ELEMENT__HANDLER.fill(location()));
				XMLStreamUtil.skipToMatchingEndTag(in);
			}
			if (rawValue != null) {
				C config = getConfig();

				Object applicationValue = _format.getValue(null, rawValue);
				storeProperty(context, applicationValue);

				if (config.getAssignId()) {
					context.assignId(var(context), rawValue);
				}
			}
		} catch (ConfigurationException ex) {
			context.error(in.getLocation(), ex.getErrorKey());
		}
		return null;
	}

}
