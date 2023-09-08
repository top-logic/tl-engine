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
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * {@link Handler} assigning the value of an {@link Config#getXmlAttribute()} to a model property.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PropertyImport<C extends PropertyImport.Config<?>> extends StoreHandler<C> {

	/**
	 * Configuration options for {@link PropertyImport}.
	 */
	@TagName("property")
	public interface Config<I extends PropertyImport<?>> extends StoreHandler.Config<I> {

		/**
		 * The XML attribute to read.
		 */
		@Mandatory
		String getXmlAttribute();

		/**
		 * The {@link ConfigurationValueProvider} that converts the XML text value to a model
		 * property value.
		 */
		@ItemDefault(StringValueProvider.class)
		PolymorphicConfiguration<ConfigurationValueProvider<?>> getFormat();

	}

	private ConfigurationValueProvider<?> _format;

	/**
	 * Creates a {@link PropertyImport} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public PropertyImport(InstantiationContext context, C config) {
		super(context, config);
		_format = context.getInstance(config.getFormat());
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		try {
			String rawValue = in.getAttributeValue(null, getConfig().getXmlAttribute());
			if (rawValue != null) {
				storeProperty(context, format(rawValue));
			}
		} catch (ConfigurationException ex) {
			context.error(in.getLocation(),
				I18NConstants.ERROR_PROPERTY_FORMAT__HANDLER_MESSAGE.fill(location(), ex.getErrorKey()));
		}
		return null;
	}

	private Object format(String rawValue) throws ConfigurationException {
		return _format.getValue(getConfig().getXmlAttribute(), rawValue);
	}

}
