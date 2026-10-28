/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.git;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.category.DevelopmentCategory;

/**
 * Service holding the kinds of git hosting services the control application works with, the
 * built-in git connections of the installation, and the {@link GitHostingProvider} of each git
 * connection in use.
 *
 * <p>
 * A git connection is a server of a {@link GitProviderType registered type}, an owner on it the
 * repositories live in, and credentials. The model holds the connections a project chooses from
 * (see {@link GitConnections}); this service knows how to reach them:
 * </p>
 * <ul>
 * <li>A module supporting a kind of hosting service registers its {@link GitProviderType} in
 * {@link Config#getProviderTypes()}, e.g. Gitea by the module {@code tl-factory-gitea}.</li>
 * <li>The installation offers its {@link Config#getConnections() built-in connections} to all
 * tenants. They authenticate with the credentials of the installation, and every tenant gets an
 * organization of its own below the configured owner.</li>
 * <li>{@link #provider(GitConnectionKey)} hands out one provider per connection, created for the
 * settings of the connection and kept until they change.</li>
 * </ul>
 *
 * <p>
 * Without provider types, or without a switched-on built-in connection and connections of the
 * tenants, the projects have no repository on a hosting service.
 * </p>
 *
 * <p>
 * Starting the service does not contact a hosting service, so the application starts also when one
 * is not reachable; the first operation reports that.
 * </p>
 */
@ServiceCategory.Of(DevelopmentCategory.class)
@ServiceDependencies({
	// The credentials of the providers read named secrets.
	SecretStoreService.Module.class,
})
public class GitHostingService extends ConfiguredManagedClass<GitHostingService.Config<?>> {

	/**
	 * Configuration of {@link GitHostingService}.
	 */
	public interface Config<I extends GitHostingService> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration property name of {@link #getProviderTypes()}.
		 */
		String PROVIDER_TYPES = "provider-types";

		/**
		 * Configuration property name of {@link #getConnections()}.
		 */
		String CONNECTIONS = "connections";

		/**
		 * Configuration property name of {@link #getProtectedFiles()}.
		 *
		 * @see #getProtectedFiles()
		 */
		String PROTECTED_FILES = "protected-files";

		/**
		 * The kinds of hosting services a git connection may name, by their name.
		 *
		 * @see #PROVIDER_TYPES
		 */
		@Name(PROVIDER_TYPES)
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		Map<String, GitProviderType.Config<?>> getProviderTypes();

		/**
		 * The built-in connections offered to every tenant, by their name.
		 *
		 * @see #CONNECTIONS
		 */
		@Name(CONNECTIONS)
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		Map<String, BuiltInConnection> getConnections();

		/**
		 * Glob patterns of the files of a project repository that no push may change, e.g. the
		 * build pipeline and the deployment of the application.
		 *
		 * <p>
		 * The default branch of every repository of a project is protected with these patterns
		 * when a workspace of the project has the sources in it and the credentials of its
		 * connection may administer the repository: a change of such a file reaches the branch
		 * only through a reviewed pull request, neither the agent nor the control application
		 * pushes one. Patterns are relative to the root of the repository, {@code *} matches within
		 * a path segment, {@code **} across segments.
		 * </p>
		 *
		 * <p>
		 * The pipeline file of the CI server is protected in addition for a project whose CI is
		 * set up, and not before, even if it is listed here: setting up the CI commits it.
		 * </p>
		 *
		 * @see #PROTECTED_FILES
		 * @see GitHostingProvider#protectBranch(GitRepository, String, List)
		 */
		@Name(PROTECTED_FILES)
		@Format(CommaSeparatedStrings.class)
		@FormattedDefault(DEFAULT_PROTECTED_FILES)
		List<String> getProtectedFiles();

	}

	/**
	 * A git connection of the installation, offered to every tenant.
	 *
	 * <p>
	 * It authenticates with the credentials of the installation. Every tenant gets its own
	 * organization on the server, created on first use and named by the {@link #getOwner() owner}
	 * and the tenant (see {@link GitConnections#tenantOwner(String, com.top_logic.model.TLObject)}),
	 * so that the tenants never see the repositories of each other.
	 * </p>
	 */
	public interface BuiltInConnection extends NamedConfigMandatory {

		/**
		 * Configuration property name of {@link #getProviderType()}.
		 */
		String PROVIDER_TYPE = "provider-type";

		/**
		 * Configuration property name of {@link #getUrl()}.
		 */
		String URL = "url";

		/**
		 * Configuration property name of {@link #getOwner()}.
		 */
		String OWNER = "owner";

		/**
		 * Configuration property name of {@link #getTokenSecret()}.
		 */
		String TOKEN_SECRET = "token-secret";

		/**
		 * Configuration property name of {@link #getCredentials()}.
		 */
		String CREDENTIALS = "credentials";

		/**
		 * The {@link GitProviderType#getName() name} of the type of the server, one of the
		 * {@link Config#getProviderTypes() provider types}.
		 *
		 * @see #PROVIDER_TYPE
		 */
		@Name(PROVIDER_TYPE)
		@Mandatory
		String getProviderType();

		/**
		 * The URL of the server, e.g. {@code https://git.example.com/}; empty switches the
		 * connection off, it is then offered to no tenant.
		 *
		 * @see #URL
		 */
		@Name(URL)
		@Nullable
		String getUrl();

		/**
		 * The start of the names of the organizations of the tenants on the server.
		 *
		 * @see #OWNER
		 */
		@Name(OWNER)
		@Mandatory
		String getOwner();

		/**
		 * The name of the secret holding the access token of the installation on the server,
		 * sent as the {@link GitProviderType#tokenCredentials(String) type} sends an access token.
		 *
		 * <p>
		 * Used if no {@link #getCredentials() credentials} are configured.
		 * </p>
		 *
		 * @see #TOKEN_SECRET
		 */
		@Name(TOKEN_SECRET)
		@Nullable
		String getTokenSecret();

		/**
		 * How the requests to the server are authenticated, if not with the access token in the
		 * {@link #getTokenSecret() token secret}.
		 *
		 * @see #CREDENTIALS
		 */
		@Name(CREDENTIALS)
		@Nullable
		PolymorphicConfiguration<? extends CredentialProvider> getCredentials();

		/**
		 * Whether the connection is switched on: its {@link #getUrl() URL} is not empty.
		 */
		static boolean isSwitchedOn(BuiltInConnection connection) {
			String url = connection.getUrl();
			return url != null && !url.isBlank();
		}

	}

	/**
	 * Default of {@link Config#getProtectedFiles()}.
	 */
	public static final String DEFAULT_PROTECTED_FILES = "deploy/**";

	private final Map<String, GitProviderType> _types = new LinkedHashMap<>();

	private final Map<String, BuiltInConnection> _builtIn = new LinkedHashMap<>();

	private final Map<String, CredentialProvider> _builtInCredentials = new LinkedHashMap<>();

	private final Map<GitConnectionKey, GitHostingProvider> _providers = new ConcurrentHashMap<>();

	/**
	 * Creates a {@link GitHostingService} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public GitHostingService(InstantiationContext context, Config<?> config) {
		super(context, config);
		for (GitProviderType.Config<?> type : config.getProviderTypes().values()) {
			GitProviderType instance = context.getInstance(type);
			if (instance != null) {
				_types.put(type.getName(), instance);
			}
		}
		for (BuiltInConnection connection : config.getConnections().values()) {
			if (!BuiltInConnection.isSwitchedOn(connection)) {
				continue;
			}
			GitProviderType type = _types.get(connection.getProviderType());
			if (type == null) {
				throw new ConfigurationError(I18NConstants.ERROR_UNKNOWN_PROVIDER_TYPE__CONNECTION_TYPE
					.fill(connection.getName(), connection.getProviderType()));
			}
			CredentialProvider credentials;
			if (connection.getCredentials() != null) {
				credentials = context.getInstance(connection.getCredentials());
			} else if (connection.getTokenSecret() != null && !connection.getTokenSecret().isBlank()) {
				credentials = type.tokenCredentials(connection.getTokenSecret().strip());
			} else {
				throw new ConfigurationError(
					I18NConstants.ERROR_NO_CREDENTIALS__CONNECTION.fill(connection.getName()));
			}
			_builtIn.put(connection.getName(), connection);
			_builtInCredentials.put(connection.getName(), credentials);
		}
	}

	/**
	 * The names of the registered {@link GitProviderType}s, in the order of the configuration.
	 */
	public List<String> getProviderTypeNames() {
		return new ArrayList<>(_types.keySet());
	}

	/**
	 * The registered {@link GitProviderType} of the given name.
	 *
	 * @return The type, {@code null} if none is registered under the name.
	 */
	public GitProviderType getProviderType(String name) {
		return name == null ? null : _types.get(name);
	}

	/**
	 * The built-in connections that are switched on, in the order of the configuration.
	 */
	public List<BuiltInConnection> getBuiltInConnections() {
		return new ArrayList<>(_builtIn.values());
	}

	/**
	 * The switched-on built-in connection of the given name.
	 *
	 * @return The connection, {@code null} if there is none of the name or it is switched off.
	 */
	public BuiltInConnection getBuiltInConnection(String name) {
		return name == null ? null : _builtIn.get(name);
	}

	/**
	 * The glob patterns of the files no push to the default branch of a project repository may
	 * change.
	 *
	 * @see Config#getProtectedFiles()
	 */
	public List<String> getProtectedFiles() {
		return getConfig().getProtectedFiles();
	}

	/**
	 * The provider of the connection with the given key, created when the key is asked for the
	 * first time.
	 *
	 * <p>
	 * The credentials are those of the {@link GitConnectionKey#builtIn() built-in connection}, or
	 * the access token in the {@link GitConnectionKey#secretName() secret} of the connection.
	 * </p>
	 *
	 * @return The provider, {@code null} if the type of the key is not registered, or its built-in
	 *         connection does not exist or is switched off.
	 */
	public GitHostingProvider provider(GitConnectionKey key) {
		GitHostingProvider existing = _providers.get(key);
		if (existing != null) {
			return existing;
		}
		GitProviderType type = getProviderType(key.providerType());
		if (type == null) {
			return null;
		}
		CredentialProvider credentials;
		if (key.builtIn() != null) {
			credentials = _builtInCredentials.get(key.builtIn());
		} else if (key.secretName() != null) {
			credentials = type.tokenCredentials(key.secretName());
		} else {
			credentials = null;
		}
		if (credentials == null) {
			return null;
		}
		GitHostingProvider created = type.createProvider(
			new GitConnectionSettings(key.url(), key.owner(), credentials, key.createOwner()));
		GitHostingProvider concurrent = _providers.putIfAbsent(key, created);
		return concurrent == null ? created : concurrent;
	}

	/**
	 * Drops the providers of the connection with the given identifier, so that the next
	 * {@link #provider(GitConnectionKey)} creates a new one.
	 *
	 * @param connection
	 *        See {@link GitConnectionKey#connection()}.
	 */
	public void forget(String connection) {
		_providers.keySet().removeIf(key -> key.connection().equals(connection));
	}

	/**
	 * The {@link GitHostingService} of the running application.
	 */
	public static GitHostingService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * The running {@link GitHostingService}, {@code null} if it is not running.
	 */
	public static GitHostingService getInstanceOrNull() {
		return Module.INSTANCE.isActive() ? getInstance() : null;
	}

	/**
	 * Module of the {@link GitHostingService}.
	 */
	public static final class Module extends TypedRuntimeModule<GitHostingService> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<GitHostingService> getImplementation() {
			return GitHostingService.class;
		}
	}

}
