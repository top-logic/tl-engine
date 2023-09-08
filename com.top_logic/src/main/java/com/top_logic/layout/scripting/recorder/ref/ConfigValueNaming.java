/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.scripting.recorder.ref.ConfigValueNaming.ConfigValueName;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} for {@link ConfigValueName}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfigValueNaming extends UnrecordableNamingScheme<Object, ConfigValueName> {

	/**
	 * {@link ModelName} of a property value of a referenced {@link ConfigurationItem}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface ConfigValueName extends ModelName {

		/**
		 * Name that evaluates to a {@link ConfigurationItem}.
		 */
		@Mandatory
		ModelName getConfigName();

		/**
		 * Setter for {@link #getConfigName()}.
		 */
		void setConfigName(ModelName config);

		/**
		 * The name of the property to access in the {@link ConfigurationItem} resolved from
		 * {@link #getConfigName()}.
		 */
		@Mandatory
		String getProperty();

		/**
		 * Setter for {@link #getProperty()}.
		 */
		void setProperty(String property);
	}

	/**
	 * Creates a new {@link ConfigValueNaming}.
	 */
	public ConfigValueNaming() {
		super(Object.class, ConfigValueName.class);
	}

	@Override
	public Object locateModel(ActionContext context, ConfigValueName name) {
		ConfigurationItem config = (ConfigurationItem) context.resolve(name.getConfigName());
		String propertyName = name.getProperty();
		PropertyDescriptor property = config.descriptor().getProperty(propertyName);
		return config.value(property);
	}

	/**
	 * Creates a new {@link ConfigValueName} with the given value.
	 * 
	 * @param configName
	 *        See {@link ConfigValueName#getConfigName()}.
	 * @param property
	 *        See {@link ConfigValueName#getProperty()}.
	 */
	public static ConfigValueName newConfigValueName(ModelName configName, String property) {
		ConfigValueName name = TypedConfiguration.newConfigItem(ConfigValueName.class);
		name.setConfigName(configName);
		name.setProperty(property);
		return name;
	}

}

