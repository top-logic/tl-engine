/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.config;

import javax.servlet.ServletContext;

import org.pac4j.core.client.Client;

import com.top_logic.base.accesscontrol.DefaultExternalUserMapping;
import com.top_logic.base.accesscontrol.ExternalUserMapping;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.knowledge.wrap.person.Person;

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
		 * Strategy for extracting a local user name from the authentication result.
		 */
		@InstanceFormat
		@InstanceDefault(DefaultUserNameExtractor.class)
		@ImplementationClassDefault(ConfigurableUserNameExtractor.class)
		UserNameExtractor getUserNameExtractor();

		/**
		 * Algorithm retrieving the {@link Person} for the user name in the authentication system.
		 */
		@InstanceFormat
		@NonNullable
		@InstanceDefault(DefaultExternalUserMapping.class)
		ExternalUserMapping getUserMapping();

	}

	/**
	 * Creates the pac4j {@link Client} from application configuration.
	 */
	Client createClient(ServletContext context);

}
