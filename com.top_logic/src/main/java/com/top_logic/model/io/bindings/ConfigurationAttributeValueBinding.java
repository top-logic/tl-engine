/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.io.bindings;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.base.config.i18n.InternationalizedUtil;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.xml.log.XMLStreamLog;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.io.AttributeValueBinding;

/**
 * {@link AttributeValueBinding} storing a {@link ConfigurationItem} value.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfigurationAttributeValueBinding<C extends ConfigurationAttributeValueBinding.Config<?>>
		extends AbstractConfiguredInstance<C> implements AttributeValueBinding<ConfigurationItem> {

	/**
	 * Configuration options for {@link ConfigurationAttributeValueBinding}.
	 */
	@TagName("store-config")
	public interface Config<I extends ConfigurationAttributeValueBinding<?>> extends PolymorphicConfiguration<I> {
		/**
		 * @see #getRootTag()
		 */
		String ROOT_TAG = "root-tag";

		/**
		 * @see #getRootType()
		 */
		String ROOT_TYPE = "root-type";

		/**
		 * Tag name to use for the top-level element written.
		 */
		@Name(ROOT_TAG)
		@StringDefault("config")
		String getRootTag();

		/**
		 * Expected type of the top-level element.
		 */
		@Name(ROOT_TYPE)
		@ClassDefault(ConfigurationItem.class)
		Class<? extends ConfigurationItem> getRootType();
	}

	private final Map<String, ConfigurationDescriptor> _descriptors;

	/**
	 * Creates a {@link ConfigurationAttributeValueBinding} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConfigurationAttributeValueBinding(InstantiationContext context, C config) {
		super(context, config);
		
		_descriptors = new HashMap<>();
		_descriptors.put("config", TypedConfiguration.getConfigurationDescriptor(ConfigurationItem.class));
		_descriptors.put(config.getRootTag(), TypedConfiguration.getConfigurationDescriptor(config.getRootType()));
	}


	@Override
	public ConfigurationItem loadValue(XMLStreamLog log, XMLStreamReader in, TLStructuredTypePart attribute) throws XMLStreamException {
		int startTag = in.nextTag();
		if (startTag == XMLStreamConstants.START_ELEMENT) {
			InstantiationContext context =
				new DefaultInstantiationContext(log.asLog(() -> in.getLocation()));
			ConfigurationItem result =
				new ConfigurationReader.Handler(context, _descriptors)
					.parseContents(in, null);
			if (result != null && result instanceof ConfigBuilder) {
				result = ((ConfigBuilder) result).createConfig(context);
			}
			try {
				context.checkErrors();
			} catch (ConfigurationException ex) {
				// Read to next tag to have clean state in input reader.
				in.nextTag();
				log.error(in.getLocation(), ex.getErrorKey());
				return null;
			}
			int endTag = in.nextTag();
			if (endTag != XMLStreamConstants.END_ELEMENT) {
				throw new XMLStreamException("Expected end of attribute '" + attribute + "'.", in.getLocation());
			}
			return InternationalizedUtil.storeI18N(result, true);
		} else {
			return null;
		}
	}

	@Override
	public void storeValue(Log log, XMLStreamWriter out, TLStructuredTypePart attribute, ConfigurationItem value) throws XMLStreamException {
		if (value != null) {
			value = InternationalizedUtil.fillLiteralI18N(TypedConfiguration.copy(value), true);
			C config = getConfig();
			new ConfigurationWriter(out).writeRootElement(config.getRootTag(),
				TypedConfiguration.getConfigurationDescriptor(config.getRootType()), value);
		}
	}

	@Override
	public boolean useEmptyElement(TLStructuredTypePart attribute, ConfigurationItem value) {
		return value == null;
	}

}
