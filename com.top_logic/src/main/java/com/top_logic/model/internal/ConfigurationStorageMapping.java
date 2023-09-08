/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.internal;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.model.access.StorageMapping;

/**
 * {@link StorageMapping} to store {@link ConfigurationItem}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfigurationStorageMapping
		extends AbstractConfiguredStorageMapping<ConfigurationStorageMapping.Config, ConfigurationItem> {
	
	/**
	 * Configuration of the {@link ConfigurationStorageMapping}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("configuration-storage")
	public interface Config extends PolymorphicConfiguration<ConfigurationStorageMapping> {

		/**
		 * The {@link Object#getClass() class} the application value must have.
		 */
		@Mandatory
		Class<? extends ConfigurationItem> getConfigInterface();

	}

	private static final String CONFIG_TAG = "config";

	private final Map<String, ConfigurationDescriptor> _descriptors;

	/**
	 * Creates a new {@link ConfigurationStorageMapping} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link ConfigurationStorageMapping}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public ConfigurationStorageMapping(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_descriptors = Collections.singletonMap(CONFIG_TAG,
			TypedConfiguration.getConfigurationDescriptor(config.getConfigInterface()));
	}

	@Override
	public Class<? extends ConfigurationItem> getApplicationType() {
		return getConfig().getConfigInterface();
	}

	@Override
	public ConfigurationItem getBusinessObject(Object aStorageObject) {
		try {
			return internalGetBusinessObject(aStorageObject);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(aStorageObject + " can not be parsed to a "
				+ ConfigurationItem.class.getName(), ex);
		}
	}

	private ConfigurationItem internalGetBusinessObject(Object aStorageObject) throws ConfigurationException {
		if (aStorageObject == null) {
			return null;
		}
		String configSrc = (String) aStorageObject;
		CharacterContent content = CharacterContents.newContent(configSrc);
		ConfigurationItem result =
			new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, _descriptors)
				.setSource(content).read();
		if (!getConfig().getConfigInterface().isInstance(result)) {
			StringBuilder wrongConfigType = new StringBuilder();
			wrongConfigType.append("Configuration item '");
			wrongConfigType.append(result);
			wrongConfigType.append("' for value '");
			wrongConfigType.append(configSrc);
			wrongConfigType.append("' of unexpected type: Expected: ");
			wrongConfigType.append(getConfig().getConfigInterface());
			wrongConfigType.append(".");
			throw new ConfigurationException(wrongConfigType.toString());
		}
		return result;

	}

	@Override
	public Object getStorageObject(Object aBusinessObject) {
		if (aBusinessObject == null) {
			return null;
		}
		try {
			StringWriter buffer = new StringWriter();
			new ConfigurationWriter(buffer).write(CONFIG_TAG, ConfigurationItem.class,
				(ConfigurationItem) aBusinessObject);
			return buffer.toString();
		} catch (XMLStreamException ex) {
			throw new IllegalArgumentException("Value cannot be serialized.", ex);
		}
	}

	@Override
	public boolean isCompatible(Object businessObject) {
		return businessObject == null || getConfig().getConfigInterface().isInstance(businessObject);
	}

}

