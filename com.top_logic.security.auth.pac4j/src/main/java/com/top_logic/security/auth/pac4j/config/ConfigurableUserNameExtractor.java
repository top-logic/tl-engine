/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.config;

import org.pac4j.core.profile.CommonProfile;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * Configurable {@link UserNameExtractor}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfigurableUserNameExtractor implements UserNameExtractor {

	private Config<?> _config;

	/**
	 * Configuration options for {@link ConfigurableUserNameExtractor}.
	 */
	public interface Config<I extends ConfigurableUserNameExtractor> extends PolymorphicConfiguration<I> {
		/**
		 * Attribute name to retrieve from a {@link CommonProfile} for using as local user name.
		 * 
		 * @see CommonProfile#getAttribute(String)
		 */
		String getUserNameAttribute();
	}

	/**
	 * Creates a {@link ConfigurableUserNameExtractor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConfigurableUserNameExtractor(InstantiationContext context, Config<?> config) {
		_config = config;
	}

	@Override
	public String getUserName(CommonProfile profile) {
		Object configuredValue = profile.getAttribute(_config.getUserNameAttribute());
		if (configuredValue == null) {
			return profile.getId();
		}
		return configuredValue.toString();
	}

}
