/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.config;

import javax.servlet.ServletContext;

import org.pac4j.core.client.Client;

import com.top_logic.base.security.device.interfaces.SecurityDevice.SecurityDeviceConfig;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;

/**
 * Configurable factory for authentication {@link Client} from the pac4j library.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ClientConfigurator {

	/**
	 * Configuration options for {@link DefaultOidcClientConfigurator}.
	 */
	public interface Config<I extends ClientConfigurator> extends PolymorphicConfiguration<I> {

		/**
		 * @see #getName()
		 */
		String NAME = "name";

		/**
		 * @see #getDomain()
		 */
		String DOMAIN = "domain";

		/**
		 * The {@link Client#getName() name} of the authentication client.
		 * 
		 * <p>
		 * The client name is used to identify the authentication client among other configured
		 * clients.
		 * </p>
		 * 
		 * @see Client#getName()
		 * @see Pac4jConfigFactory#getUserNameExtractor(String)
		 */
		@Name(NAME)
		@Mandatory
		public String getName();

		/**
		 * The <i>TopLogic</i> authentication domain name, the client is used for.
		 * 
		 * <p>
		 * If not specified, the value defaults to {@link #getName()}.
		 * </p>
		 * 
		 * <p>
		 * A user that is authenticated by this client must be found in the <i>TopLogic</i> domain with
		 * the configured name to be accepted by the system.
		 * </p>
		 * 
		 * @see Pac4jConfigFactory#getDomain(String)
		 * @see SecurityDeviceConfig#getDomain()
		 */
		@Name(DOMAIN)
		@Nullable
		public String getDomain();

		/**
		 * Strategy for extracting a local user name from the authentication result.
		 */
		@InstanceFormat
		@InstanceDefault(DefaultUserNameExtractor.class)
		@ImplementationClassDefault(ConfigurableUserNameExtractor.class)
		UserNameExtractor getUserNameExtractor();

	}

	/**
	 * Creates the pac4j {@link Client} from application configuration.
	 */
	Client createClient(ServletContext context);

}
