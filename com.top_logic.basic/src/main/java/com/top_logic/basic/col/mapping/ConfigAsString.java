/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.mapping;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationSchemaConstants;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.character.CharacterContents;

/**
 * Configurable {@link Mapping} rewriting {@link ConfigurationItem}s by applying a function to the
 * XML-serialized version of the configuration.
 * 
 * @see Config#getInner()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfigAsString<C extends ConfigAsString.Config<?>> extends AbstractConfiguredInstance<C>
		implements Mapping<ConfigurationItem, ConfigurationItem> {

	private Mapping<String, String> _inner;
	private String _rootTag;
	private ConfigurationDescriptor _staticType;
	private Map<String, ConfigurationDescriptor> _descriptors;

	/**
	 * Configuration options for {@link RegexPatternMapping}.
	 */
	@TagName("config-as-string")
	public interface Config<I extends ConfigAsString<?>> extends PolymorphicConfiguration<I> {

		/**
		 * The root tag that is given to the XML-serialized configuraiton.
		 */
		@StringDefault("config")
		String getRootTag();

		/**
		 * The expected static type of the value.
		 * 
		 * <p>
		 * If the concrete type matches this value, no type annotation is serialized, see
		 * {@link ConfigurationSchemaConstants#CONFIG_INTERFACE_ATTR}.
		 * </p>
		 */
		@ClassDefault(ConfigurationItem.class)
		Class<? extends ConfigurationItem> getStaticType();

		/**
		 * The function to apply to the XML-serialized version of the configuration.
		 */
		@DefaultContainer
		PolymorphicConfiguration<Mapping<String, String>> getInner();

	}

	/**
	 * Creates a {@link RegexPatternMapping} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConfigAsString(InstantiationContext context, C config) {
		super(context, config);
		_inner = context.getInstance(config.getInner());
		_rootTag = config.getRootTag();
		_staticType = TypedConfiguration.getConfigurationDescriptor(config.getStaticType());
		_descriptors = Collections.singletonMap(_rootTag, _staticType);
	}

	@Override
	public ConfigurationItem map(ConfigurationItem input) {
		try {
			if (input == null) {
				return null;
			}

			StringWriter buffer = new StringWriter();
			new ConfigurationWriter(buffer).write(_rootTag, _staticType, input);

			String xmlString = buffer.toString();
			String transformedXML = _inner.apply(xmlString);
			if (xmlString.equals(transformedXML)) {
				return input;
			}

			ConfigurationItem transformed =
				new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, _descriptors)
					.setSource(CharacterContents.newContent(transformedXML))
					.read();

			return transformed;
		} catch (XMLStreamException ex) {
			throw new IllegalArgumentException("Cannot serialize configuration.", ex);
		} catch (ConfigurationException ex) {
			throw new IllegalArgumentException("Cannot parse transformed configuration.", ex);
		}
	}

}
