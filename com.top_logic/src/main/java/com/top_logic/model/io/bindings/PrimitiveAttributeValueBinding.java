/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.io.bindings;

import java.text.Format;

import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.xml.log.XMLStreamLog;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.io.AttributeValueBinding;

/**
 * {@link AttributeValueBinding} delegating to a configured {@link Format} implementation for
 * storing a value as XML attribute value.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PrimitiveAttributeValueBinding<C extends PrimitiveAttributeValueBinding.Config<?>>
		extends AbstractPrimitiveAttributeValueBinding<Object> implements ConfiguredInstance<C> {

	/**
	 * Configuration options for {@link PrimitiveAttributeValueBinding}.
	 */
	public interface Config<I extends PrimitiveAttributeValueBinding<?>> extends PolymorphicConfiguration<I> {
		/**
		 * The format to store the primitive value with.
		 */
		PolymorphicConfiguration<ConfigurationValueProvider<?>> getFormat();
	}

	private C _config;

	private ConfigurationValueProvider<Object> _format;

	/**
	 * Creates a {@link PrimitiveAttributeValueBinding} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public PrimitiveAttributeValueBinding(InstantiationContext context, C config) {
		_config = config;
		@SuppressWarnings("unchecked")
		ConfigurationValueProvider<Object> format =
			(ConfigurationValueProvider<Object>) context.getInstance(config.getFormat());

		_format = format;
	}

	@Override
	public C getConfig() {
		return _config;
	}

	@Override
	protected Object parseValue(XMLStreamLog log, XMLStreamReader in, TLStructuredTypePart attribute, String rawValue) {
		try {
			return _format.getValue(null, rawValue);
		} catch (ConfigurationException ex) {
			log.error(in.getLocation(),
				I18NConstants.INVALID_FORMAT__VALUE_ATTR_DETAILS.fill(rawValue, attribute, ex.getErrorKey()));
			return _format.defaultValue();
		}
	}

	@Override
	protected String serializeValue(TLStructuredTypePart attribute, Object value) {
		return _format.getSpecification(value);
	}

}
