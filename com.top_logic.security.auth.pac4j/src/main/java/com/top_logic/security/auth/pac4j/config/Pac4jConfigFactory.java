/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.http.url.DefaultUrlResolver;
import org.pac4j.core.http.url.UrlResolver;
import org.pac4j.core.util.Pac4jConstants;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.services.ServletContextService;

/**
 * <i>TopLogic</i> module builing the pac4j configuration.
 * 
 * @see org.pac4j.core.config.Config#INSTANCE
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ServiceDependencies({
	ServletContextService.Module.class
})
public class Pac4jConfigFactory<C extends Pac4jConfigFactory.Config<?>> extends ConfiguredManagedClass<C> {

	/**
	 * Configuration options for {@link Pac4jConfigFactory}.
	 */
	public interface Config<I extends Pac4jConfigFactory<?>> extends ConfiguredManagedClass.Config<I>, HasCallbackUrl {

		@Override
		@StringDefault("/servlet/callback")
		String getCallbackUrl();

		/**
		 * Authentication {@link Client} configurations.
		 */
		@Name("clients")
		@Key(ClientConfigurator.Config.NAME)
		@ImplementationClassDefault(DefaultOidcClientConfigurator.class)
		Map<String, ClientConfigurator.Config<? extends ClientConfigurator>> getClients();

	}

	private Collection<ClientConfigurator> _clientConfigurators;

	/**
	 * Creates a {@link Pac4jConfigFactory} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public Pac4jConfigFactory(InstantiationContext context, C config) {
		super(context, config);

		_clientConfigurators = TypedConfiguration.getInstanceMap(context, config.getClients()).values();
	}

	/**
	 * The {@link UserNameExtractor} for the given {@link Client#getName() client name}.
	 * 
	 * @see ClientConfigurator.Config#getName()
	 */
	public UserNameExtractor getUserNameExtractor(String clientName) {
		ClientConfigurator.Config<?> clientConfig = getConfig().getClients().get(clientName);
		if (clientConfig == null) {
			Logger.error("No such client configured: " + clientName, Pac4jConfigFactory.class);
			return DefaultUserNameExtractor.INSTANCE;
		}
		return clientConfig.getUserNameExtractor();
	}

	/**
	 * The (configured) <i>TopLogic</i> domain name for which the authentication device authenticates
	 * users.
	 * 
	 * @see ClientConfigurator.Config#getDomain()
	 */
	public String getDomain(String clientName) {
		ClientConfigurator.Config<?> clientConfig = getConfig().getClients().get(clientName);
		if (clientConfig == null) {
			Logger.error("No such client configured: " + clientName, Pac4jConfigFactory.class);
			return clientName;
		}
		String result = clientConfig.getDomain();
		if (result == null) {
			// Default as described in the domain config option.
			return clientName;
		}
		return result;
	}

	@Override
	protected void startUp() {
		super.startUp();

		org.pac4j.core.config.Config
			.setConfig(buildPac4jConfig(ServletContextService.getInstance().getServletContext()));
	}

	/**
	 * Creates the pac4j configuration.
	 * 
	 * @see org.pac4j.core.config.Config#INSTANCE
	 */
	protected org.pac4j.core.config.Config buildPac4jConfig(ServletContext context) {
		@SuppressWarnings("rawtypes")
		List<Client> clientList = new ArrayList<>();
		for (ClientConfigurator configurator : _clientConfigurators) {
			Client client = configurator.createClient(context);
			if (client != null) {
				clientList.add(client);
			}
		}

		final Clients clients = new Clients(resolveCallbackUrl(context, getConfig()), clientList);
		clients.setUrlResolver(createUrlResolver(getConfig()));

		// Required, if more than one client is registered. Without that setting, pac4j does not use
		// any client for authentication, even if the request selects a client by setting a request
		// parameter.
		clients.setDefaultSecurityClients(clientNames(clients));

		final org.pac4j.core.config.Config config = new org.pac4j.core.config.Config(clients);

		config.addAuthorizer("admin", new RequireAnyRoleAuthorizer("ROLE_ADMIN"));
		return config;
	}

	/**
	 * Gets the names of the given clients as comma separated string.
	 */
	private String clientNames(Clients clients) {
		StringBuilder sb = new StringBuilder();
		boolean second = false;
		for (Client c : clients.getClients()) {
			if (second) {
				sb.append(Pac4jConstants.ELEMENT_SEPARATOR);
			} else {
				second = true;
			}
			sb.append(c.getName());
		}
		return sb.toString();
	}

	/**
	 * Creates a callback URL from the given configuration.
	 */
	public static String resolveCallbackUrl(ServletContext context, HasCallbackUrl config) {
		String callbackUrl = config.getCallbackUrl();
		if (callbackUrl == null) {
			return null;
		}
		if (!config.isCallbackUrlAbsolute()) {
			callbackUrl = context.getContextPath() + callbackUrl;
		}
		return callbackUrl;
	}

	/**
	 * Creates an {@link UrlResolver} respecting {@link HasCallbackUrl#isCallbackUrlAbsolute()}.
	 */
	public static UrlResolver createUrlResolver(HasCallbackUrl config) {
		return new DefaultUrlResolver(!config.isCallbackUrlAbsolute());
	}

	/**
	 * The singleton instance of {@link Pac4jConfigFactory}.
	 */
	public static Pac4jConfigFactory<?> getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton container of {@link Pac4jConfigFactory}.
	 */
	public static class Module extends TypedRuntimeModule<Pac4jConfigFactory<?>> {

		/**
		 * Singleton {@link Pac4jConfigFactory.Module} instance.
		 */
		public static final Pac4jConfigFactory.Module INSTANCE = new Pac4jConfigFactory.Module();

		private Module() {
			// Singleton constructor.
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Class<Pac4jConfigFactory<?>> getImplementation() {
			return (Class) Pac4jConfigFactory.class;
		}

	}

}
