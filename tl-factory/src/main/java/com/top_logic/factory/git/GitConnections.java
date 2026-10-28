/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.git;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.basic.credential.WritableSecretStore;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.factory.git.GitHostingService.BuiltInConnection;
import com.top_logic.factory.project.FactoryAccess;
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * The git connections the projects of the tenants choose from, and the
 * {@link GitHostingProvider} of each.
 *
 * <p>
 * A {@link FactoryModel#GIT_CONNECTION git connection} names the {@link GitProviderType type} of a
 * server, its URL and the owner the repositories live in. There are two kinds:
 * </p>
 * <ul>
 * <li><b>Built-in</b> connections are configured in the installation
 * ({@link GitHostingService.Config#getConnections()}) and offered to every tenant; they have no
 * {@link FactoryModel#GIT_CONNECTION_TENANT tenant}. {@link #synchronizeBuiltIn()} keeps one object
 * per switched-on configuration, by its name, so that a project references a built-in connection
 * like any other. A built-in connection authenticates with the credentials of the installation,
 * and every tenant gets an organization of its own on the server ({@link #tenantOwner(String,
 * TLObject)}), created when the first repository of the tenant is created.</li>
 * <li><b>Own</b> connections are created by the administrators of a tenant, with an access token
 * of their own. The token is kept in the {@link SecretStoreService#writableSecretStore() writable
 * secret store} under a generated name (the hidden {@link FactoryModel#GIT_CONNECTION_SECRET_NAME
 * secret name}); it is never stored in the model and never shown again, only replaced. Deleting the
 * connection removes the secret.</li>
 * </ul>
 *
 * <p>
 * Every member of a tenant reads the connections offered to the tenant, its
 * {@link FactoryModel#GIT_CONNECTION_TENANTS tenants}: the own connections of the tenant and all
 * built-in ones. Nobody but an operator writes a connection through the model; the administrators
 * of a tenant (and the operators) change its own connections through the checked operations of this
 * class, which commit their changes in a transaction of their own, like {@link
 * com.top_logic.factory.tenant.TenantMembers}.
 * </p>
 */
public class GitConnections {

	/**
	 * Start of the names of the secrets holding the access tokens of the own connections.
	 */
	public static final String SECRET_PREFIX = "tl_factory_git_";

	/**
	 * Separator of the start configured for a built-in connection and the key of a tenant in the
	 * name of the organization of the tenant.
	 */
	public static final String OWNER_SEPARATOR = "-";

	/**
	 * The longest name of an organization of a tenant, short enough for the usual hosting
	 * services.
	 */
	public static final int MAX_OWNER_LENGTH = 39;

	private GitConnections() {
		// Static operations only.
	}

	/**
	 * What a user enters for an own connection, without its access token.
	 *
	 * @param name
	 *        The display name.
	 * @param providerType
	 *        The {@link GitProviderType#getName() name of the type} of the server.
	 * @param url
	 *        The URL of the server.
	 * @param owner
	 *        The organization or user the repositories live in.
	 */
	public record OwnConnection(String name, String providerType, String url, String owner) {

		/**
		 * The same data with surrounding white space removed.
		 */
		OwnConnection strip() {
			return new OwnConnection(trim(name), trim(providerType), trim(url), trim(owner));
		}

		private static String trim(String value) {
			return value == null ? null : value.strip();
		}
	}

	/**
	 * Whether the given connection is a built-in connection of the installation, offered to every
	 * tenant.
	 */
	public static boolean isBuiltIn(TLObject connection) {
		return tenantOf(connection) == null;
	}

	/**
	 * The tenant owning the given connection, {@code null} for a built-in connection.
	 */
	public static TLObject tenantOf(TLObject connection) {
		return (TLObject) connection.tValueByName(FactoryModel.GIT_CONNECTION_TENANT);
	}

	/**
	 * The display name of the given connection.
	 */
	public static String nameOf(TLObject connection) {
		return FactoryModel.getString(connection, FactoryModel.GIT_CONNECTION_NAME);
	}

	/**
	 * The organization or user the repositories of the given tenant are created in on the given
	 * connection: the {@link FactoryModel#GIT_CONNECTION_OWNER owner} of an own connection, the
	 * {@link #tenantOwner(String, TLObject) organization of the tenant} on a built-in one.
	 *
	 * @param connection
	 *        The connection.
	 * @param tenant
	 *        The tenant, {@code null} for none.
	 * @return The owner, {@code null} on a built-in connection without tenant.
	 */
	public static String ownerFor(TLObject connection, TLObject tenant) {
		String owner = FactoryModel.getString(connection, FactoryModel.GIT_CONNECTION_OWNER);
		if (!isBuiltIn(connection)) {
			return owner;
		}
		BuiltInConnection config = builtInConfig(connection);
		if (config != null) {
			owner = config.getOwner();
		}
		return tenant == null ? null : tenantOwner(owner, tenant);
	}

	/**
	 * The name of the organization of a tenant on a built-in connection: the configured start, a
	 * {@link #OWNER_SEPARATOR} and the identifier of the tenant.
	 *
	 * <p>
	 * The identifier does not change when the tenant is renamed and is never reused by another
	 * tenant, so the organization stays the tenant's. The name consists of lower case letters,
	 * digits and {@link #OWNER_SEPARATOR}s, at most {@link #MAX_OWNER_LENGTH} characters; a long
	 * start is shortened.
	 * </p>
	 *
	 * @param start
	 *        The configured start, see {@link BuiltInConnection#getOwner()}.
	 * @param tenant
	 *        The tenant.
	 */
	public static String tenantOwner(String start, TLObject tenant) {
		String key = OWNER_SEPARATOR + nameSafe(IdentifierUtil.toExternalForm(tenant.tId().getObjectName()));
		String prefix = nameSafe(start);
		int room = Math.max(0, MAX_OWNER_LENGTH - key.length());
		if (prefix.length() > room) {
			prefix = prefix.substring(0, room);
		}
		String result = prefix + key;
		return result.length() > MAX_OWNER_LENGTH ? result.substring(result.length() - MAX_OWNER_LENGTH) : result;
	}

	private static String nameSafe(String value) {
		String lower = value.strip().toLowerCase(Locale.ROOT);
		StringBuilder result = new StringBuilder(lower.length());
		for (int n = 0; n < lower.length(); n++) {
			char c = lower.charAt(n);
			boolean valid = (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');
			result.append(valid ? c : OWNER_SEPARATOR.charAt(0));
		}
		return result.toString();
	}

	/**
	 * The provider of the repository of the given project, the provider of its connection for its
	 * tenant.
	 *
	 * @return The provider, {@code null} if the project has no connection or its connection is
	 *         {@link #provider(TLObject, TLObject) not available}.
	 */
	public static GitHostingProvider projectProvider(TLObject project) {
		TLObject connection = FactoryModel.getGitConnection(project);
		return connection == null ? null : provider(connection, FactoryModel.getTenant(project));
	}

	/**
	 * The provider of the given connection, for the repositories of the given tenant.
	 *
	 * @param connection
	 *        The connection.
	 * @param tenant
	 *        The tenant whose repositories are accessed: the organization of the tenant on a
	 *        built-in connection; ignored on an own connection.
	 * @return The provider, {@code null} if the {@link GitHostingService} is not running, the type
	 *         of the connection is not registered, a built-in connection is no longer configured or
	 *         switched off, or a built-in connection is asked for no tenant.
	 */
	public static GitHostingProvider provider(TLObject connection, TLObject tenant) {
		GitHostingService service = GitHostingService.getInstanceOrNull();
		GitConnectionKey key = key(connection, tenant);
		return service == null || key == null ? null : service.provider(key);
	}

	/**
	 * The key of the provider of the given connection for the given tenant.
	 *
	 * @return The key, {@code null} if the connection is not available for the tenant.
	 */
	private static GitConnectionKey key(TLObject connection, TLObject tenant) {
		String id = idOf(connection);
		if (isBuiltIn(connection)) {
			BuiltInConnection config = builtInConfig(connection);
			if (config == null || tenant == null) {
				return null;
			}
			return new GitConnectionKey(id, config.getProviderType(), config.getUrl().strip(),
				tenantOwner(config.getOwner(), tenant), config.getName(), null, true);
		}
		String secretName = FactoryModel.getString(connection, FactoryModel.GIT_CONNECTION_SECRET_NAME);
		String type = FactoryModel.getString(connection, FactoryModel.GIT_CONNECTION_PROVIDER_TYPE);
		String url = FactoryModel.getString(connection, FactoryModel.GIT_CONNECTION_URL);
		String owner = FactoryModel.getString(connection, FactoryModel.GIT_CONNECTION_OWNER);
		if (secretName == null || type == null || url == null || owner == null) {
			return null;
		}
		return new GitConnectionKey(id, type, url, owner, null, secretName, false);
	}

	/**
	 * The configuration of the given built-in connection.
	 *
	 * @return The configuration, {@code null} if the connection is not configured, switched off,
	 *         or the {@link GitHostingService} is not running.
	 */
	private static BuiltInConnection builtInConfig(TLObject connection) {
		GitHostingService service = GitHostingService.getInstanceOrNull();
		return service == null ? null : service.getBuiltInConnection(nameOf(connection));
	}

	/**
	 * Whether the given connection can be reached: its type is registered and, for a built-in
	 * connection, it is configured and switched on.
	 */
	public static boolean isAvailable(TLObject connection) {
		GitHostingService service = GitHostingService.getInstanceOrNull();
		if (service == null) {
			return false;
		}
		if (isBuiltIn(connection)) {
			BuiltInConnection config = service.getBuiltInConnection(nameOf(connection));
			return config != null && service.getProviderType(config.getProviderType()) != null;
		}
		return service.getProviderType(
			FactoryModel.getString(connection, FactoryModel.GIT_CONNECTION_PROVIDER_TYPE)) != null;
	}

	/**
	 * The connections a new project of the given tenant may live on: the available built-in
	 * connections in the order of their configuration, then the available own connections of the
	 * tenant by name.
	 *
	 * @param tenant
	 *        The tenant, {@code null} for none: the built-in connections only.
	 */
	public static List<TLObject> choices(TLObject tenant) {
		List<TLObject> result = new ArrayList<>();
		GitHostingService service = GitHostingService.getInstanceOrNull();
		if (service == null) {
			return result;
		}
		List<TLObject> builtIn = builtInObjects();
		for (BuiltInConnection config : service.getBuiltInConnections()) {
			for (TLObject connection : builtIn) {
				if (config.getName().equals(nameOf(connection)) && isAvailable(connection)) {
					result.add(connection);
				}
			}
		}
		if (tenant != null) {
			List<TLObject> own = ownConnections(tenant);
			own.removeIf(connection -> !isAvailable(connection));
			result.addAll(own);
		}
		return result;
	}

	/**
	 * The connection a new project of the given tenant lives on unless the user chooses another:
	 * the first of the {@link #choices(TLObject) choices}.
	 *
	 * @return The connection, {@code null} if there is no choice.
	 */
	public static TLObject defaultChoice(TLObject tenant) {
		List<TLObject> choices = choices(tenant);
		return choices.isEmpty() ? null : choices.get(0);
	}

	/**
	 * The own connections of the given tenant, by name.
	 */
	public static List<TLObject> ownConnections(TLObject tenant) {
		TLReference reference =
			(TLReference) FactoryModel.gitConnectionType().getPartOrFail(FactoryModel.GIT_CONNECTION_TENANT);
		List<TLObject> result = new ArrayList<>(tenant.tReferers(reference));
		result.sort(Comparator.comparing(connection -> Objects.toString(nameOf(connection), "")));
		return result;
	}

	/**
	 * The projects whose repository lives on the given connection.
	 */
	public static List<TLObject> projectsOn(TLObject connection) {
		TLReference reference =
			(TLReference) FactoryModel.projectType().getPartOrFail(FactoryModel.PROJECT_GIT_CONNECTION);
		return new ArrayList<>(connection.tReferers(reference));
	}

	/**
	 * Checks the connection of a project.
	 *
	 * <p>
	 * A project that is created needs one of the {@link #choices(TLObject) choices} of its tenant,
	 * if there are any; without choices, it has no connection and no repository. The connection of
	 * a stored project is not checked again.
	 * </p>
	 *
	 * @param project
	 *        The project, or the form object creating one.
	 * @param value
	 *        The connection, {@code null} for none.
	 * @return The problem, {@code null} if there is none.
	 */
	public static ResKey checkProjectConnection(TLObject project, Object value) {
		if (project == null || !isCreating(project)) {
			return null;
		}
		List<TLObject> choices = choices(FactoryModel.getTenant(project));
		if (value == null) {
			return choices.isEmpty() ? null : I18NConstants.ERROR_NO_GIT_CONNECTION;
		}
		if (!(value instanceof TLObject connection) || !choices.contains(connection)) {
			return I18NConstants.ERROR_GIT_CONNECTION_NOT_AVAILABLE__CONNECTION.fill(value);
		}
		return null;
	}

	/**
	 * Whether the given object is a project being created: the transient draft of a new project,
	 * a form object editing such a draft, or a form object creating a project.
	 */
	public static boolean isCreating(TLObject project) {
		TLObject edited = TLFormObjectBase.editedObject(project);
		if (edited instanceof TLFormObjectBase form) {
			return form.isCreate();
		}
		return edited.tTransient();
	}

	/**
	 * What the credentials of the given connection may do for the repositories of the given
	 * tenant.
	 *
	 * @param connection
	 *        The connection.
	 * @param tenant
	 *        The tenant, see {@link #provider(TLObject, TLObject)}.
	 * @return The capabilities, with the {@link GitCapabilities#problem() problem} if the
	 *         connection is not available or the hosting service cannot be asked.
	 */
	public static GitCapabilities capabilities(TLObject connection, TLObject tenant) {
		GitHostingProvider provider = provider(connection, tenant);
		if (provider == null) {
			return GitCapabilities.unknown(I18NConstants.ERROR_CONNECTION_UNAVAILABLE__CONNECTION.fill(connection));
		}
		try {
			return provider.capabilities();
		} catch (TopLogicException ex) {
			return GitCapabilities.unknown(ex.getErrorKey());
		}
	}

	/**
	 * Creates an own connection of a tenant with its access token.
	 *
	 * @param tenant
	 *        The tenant owning the connection.
	 * @param data
	 *        The server and owner of the connection.
	 * @param token
	 *        The access token, which is kept in the writable secret store only.
	 * @return The new connection.
	 * @throws TopLogicException
	 *         If the current user may not manage the tenant, the data is incomplete or invalid,
	 *         the installation has no writable secret store, or the token cannot be stored.
	 */
	public static TLObject create(TLObject tenant, OwnConnection data, String token) {
		FactoryAccess.checkWrite(tenant);
		OwnConnection own = checkData(data);
		checkToken(token);
		String secretName = storeToken(token);
		try {
			checkSettings(own, secretName);
			return FactoryAccess.commitUnchecked(I18NConstants.CREATED_CONNECTION__CONNECTION_TENANT.fill(own.name(), tenant),
				() -> {
					TLObject connection =
						ModelService.getInstance().getFactory().createObject(FactoryModel.gitConnectionType());
					connection.tUpdateByName(FactoryModel.GIT_CONNECTION_TENANT, tenant);
					connection.tUpdateByName(FactoryModel.GIT_CONNECTION_TENANTS, Set.of(tenant));
					connection.tUpdateByName(FactoryModel.GIT_CONNECTION_SECRET_NAME, secretName);
					setData(connection, own);
					return connection;
				});
		} catch (RuntimeException ex) {
			removeToken(secretName);
			throw ex;
		}
	}

	/**
	 * Changes an own connection of a tenant.
	 *
	 * <p>
	 * The name can always be changed; type, URL and owner only while no project lives on the
	 * connection, since its repository is on the server and in the owner of the connection.
	 * </p>
	 *
	 * @param connection
	 *        The own connection.
	 * @param data
	 *        The new server and owner of the connection.
	 * @throws TopLogicException
	 *         If the current user may not manage the tenant of the connection, it is a built-in
	 *         connection, the data is incomplete or invalid, or the server changes while projects
	 *         live on the connection.
	 */
	public static void update(TLObject connection, OwnConnection data) {
		checkOwn(connection);
		OwnConnection own = checkData(data);
		boolean serverChanged = !Objects.equals(own.providerType(),
			FactoryModel.getString(connection, FactoryModel.GIT_CONNECTION_PROVIDER_TYPE))
			|| !Objects.equals(own.url(), FactoryModel.getString(connection, FactoryModel.GIT_CONNECTION_URL))
			|| !Objects.equals(own.owner(), FactoryModel.getString(connection, FactoryModel.GIT_CONNECTION_OWNER));
		if (serverChanged) {
			List<TLObject> projects = projectsOn(connection);
			if (!projects.isEmpty()) {
				throw new TopLogicException(I18NConstants.ERROR_CONNECTION_SERVER_IN_USE__CONNECTION_PROJECTS
					.fill(nameOf(connection), projectNames(projects)));
			}
			checkSettings(own, FactoryModel.getString(connection, FactoryModel.GIT_CONNECTION_SECRET_NAME));
		}
		FactoryAccess.commitUnchecked(I18NConstants.CHANGED_CONNECTION__CONNECTION.fill(own.name()), () -> {
			setData(connection, own);
			return null;
		});
		forget(connection);
	}

	/**
	 * Replaces the access token of an own connection of a tenant.
	 *
	 * <p>
	 * The token is stored under a new name, the old secret is removed.
	 * </p>
	 *
	 * @param connection
	 *        The own connection.
	 * @param token
	 *        The new access token.
	 * @throws TopLogicException
	 *         If the current user may not manage the tenant of the connection, it is a built-in
	 *         connection, the token is empty, or the token cannot be stored.
	 */
	public static void replaceToken(TLObject connection, String token) {
		checkOwn(connection);
		checkToken(token);
		String before = FactoryModel.getString(connection, FactoryModel.GIT_CONNECTION_SECRET_NAME);
		String secretName = storeToken(token);
		try {
			FactoryAccess.commitUnchecked(I18NConstants.REPLACED_TOKEN__CONNECTION.fill(nameOf(connection)), () -> {
				connection.tUpdateByName(FactoryModel.GIT_CONNECTION_SECRET_NAME, secretName);
				return null;
			});
		} catch (RuntimeException ex) {
			removeToken(secretName);
			throw ex;
		}
		forget(connection);
		if (before != null) {
			removeToken(before);
		}
	}

	/**
	 * Deletes an own connection of a tenant and removes its access token.
	 *
	 * @param connection
	 *        The own connection.
	 * @throws TopLogicException
	 *         If the current user may not manage the tenant of the connection, it is a built-in
	 *         connection, or projects live on it.
	 */
	public static void delete(TLObject connection) {
		checkOwn(connection);
		List<TLObject> projects = projectsOn(connection);
		if (!projects.isEmpty()) {
			throw new TopLogicException(I18NConstants.ERROR_CONNECTION_IN_USE__CONNECTION_PROJECTS
				.fill(nameOf(connection), projectNames(projects)));
		}
		String secretName = FactoryModel.getString(connection, FactoryModel.GIT_CONNECTION_SECRET_NAME);
		String name = nameOf(connection);
		forget(connection);
		FactoryAccess.commitUnchecked(I18NConstants.DELETED_CONNECTION__CONNECTION.fill(name), () -> {
			connection.tDelete();
			return null;
		});
		if (secretName != null) {
			removeToken(secretName);
		}
	}

	/**
	 * Makes the built-in connections available to the given tenant, in the current transaction.
	 *
	 * <p>
	 * Called when a tenant is created; {@link #synchronizeBuiltIn()} does it for all tenants.
	 * </p>
	 */
	public static void offerBuiltIn(TLObject tenant) {
		for (TLObject connection : builtInObjects()) {
			Set<Object> tenants = new LinkedHashSet<>(tenantsOf(connection));
			if (tenants.add(tenant)) {
				connection.tUpdateByName(FactoryModel.GIT_CONNECTION_TENANTS, tenants);
			}
		}
	}

	/**
	 * Brings the built-in connection objects in line with the configuration of the
	 * {@link GitHostingService}, in a system context and a transaction of its own.
	 *
	 * <ul>
	 * <li>Every switched-on built-in connection has an object of its name, with the type, URL and
	 * owner of the configuration, offered to every tenant.</li>
	 * <li>The object of a connection that is no longer configured or switched off is deleted if no
	 * project lives on it; otherwise it is kept and is not {@link #isAvailable(TLObject)
	 * available}.</li>
	 * </ul>
	 */
	public static void synchronizeBuiltIn() {
		GitHostingService service = GitHostingService.getInstanceOrNull();
		if (service == null) {
			return;
		}
		ThreadContext.inSystemContext(GitConnections.class, () -> {
			FactoryAccess.commitUnchecked(I18NConstants.SYNCHRONIZED_BUILT_IN_CONNECTIONS, () -> {
				synchronize(service);
				return null;
			});
		});
	}

	private static void synchronize(GitHostingService service) {
		List<TLObject> existing = builtInObjects();
		Set<TLObject> allTenants = new HashSet<>(
			MetaElementUtil.getAllInstancesOf(FactoryModel.tenantType(), TLObject.class));
		Set<TLObject> kept = new HashSet<>();
		for (BuiltInConnection config : service.getBuiltInConnections()) {
			TLObject connection = null;
			for (TLObject candidate : existing) {
				if (config.getName().equals(nameOf(candidate))) {
					connection = candidate;
					break;
				}
			}
			if (connection == null) {
				connection = ModelService.getInstance().getFactory().createObject(FactoryModel.gitConnectionType());
				connection.tUpdateByName(FactoryModel.GIT_CONNECTION_NAME, config.getName());
				Logger.info("Created the built-in git connection " + config.getName() + ".", GitConnections.class);
			}
			updateIfChanged(connection, FactoryModel.GIT_CONNECTION_PROVIDER_TYPE, config.getProviderType());
			updateIfChanged(connection, FactoryModel.GIT_CONNECTION_URL, config.getUrl().strip());
			updateIfChanged(connection, FactoryModel.GIT_CONNECTION_OWNER, config.getOwner());
			if (!new HashSet<>(tenantsOf(connection)).equals(allTenants)) {
				connection.tUpdateByName(FactoryModel.GIT_CONNECTION_TENANTS, allTenants);
			}
			kept.add(connection);
		}
		for (TLObject connection : existing) {
			if (kept.contains(connection)) {
				continue;
			}
			if (projectsOn(connection).isEmpty()) {
				Logger.info("Deleted the built-in git connection " + nameOf(connection)
					+ ", which is no longer configured.", GitConnections.class);
				connection.tDelete();
			} else {
				Logger.warn("The built-in git connection " + nameOf(connection)
					+ " is no longer configured, but projects live on it.", GitConnections.class);
			}
		}
	}

	private static void updateIfChanged(TLObject object, String attribute, String value) {
		if (!Objects.equals(value, object.tValueByName(attribute))) {
			object.tUpdateByName(attribute, value);
		}
	}

	/**
	 * The stored built-in connection objects.
	 */
	private static List<TLObject> builtInObjects() {
		List<TLObject> result = new ArrayList<>();
		for (TLObject connection : MetaElementUtil.getAllInstancesOf(FactoryModel.gitConnectionType(),
			TLObject.class)) {
			if (isBuiltIn(connection)) {
				result.add(connection);
			}
		}
		return result;
	}

	private static Collection<?> tenantsOf(TLObject connection) {
		Object value = connection.tValueByName(FactoryModel.GIT_CONNECTION_TENANTS);
		return value == null ? List.of() : (Collection<?>) value;
	}

	private static void checkOwn(TLObject connection) {
		TLObject tenant = tenantOf(connection);
		if (tenant == null) {
			throw new TopLogicException(I18NConstants.ERROR_BUILT_IN_CONNECTION__CONNECTION.fill(nameOf(connection)));
		}
		FactoryAccess.checkWrite(tenant);
	}

	private static OwnConnection checkData(OwnConnection data) {
		OwnConnection own = data.strip();
		if (isEmpty(own.name())) {
			throw new TopLogicException(I18NConstants.ERROR_MISSING_CONNECTION_NAME);
		}
		if (isEmpty(own.url())) {
			throw new TopLogicException(I18NConstants.ERROR_MISSING_CONNECTION_URL);
		}
		if (isEmpty(own.owner())) {
			throw new TopLogicException(I18NConstants.ERROR_MISSING_CONNECTION_OWNER);
		}
		GitHostingService service = GitHostingService.getInstanceOrNull();
		if (service == null || service.getProviderType(own.providerType()) == null) {
			throw new TopLogicException(I18NConstants.ERROR_UNKNOWN_PROVIDER_TYPE__TYPE.fill(own.providerType()));
		}
		return own;
	}

	/**
	 * Lets the type of the connection check its settings by creating a provider, which does not
	 * contact the server.
	 */
	private static void checkSettings(OwnConnection own, String secretName) {
		GitProviderType type = GitHostingService.getInstance().getProviderType(own.providerType());
		type.createProvider(
			new GitConnectionSettings(own.url(), own.owner(), type.tokenCredentials(secretName), false));
	}

	private static void checkToken(String token) {
		if (isEmpty(token)) {
			throw new TopLogicException(I18NConstants.ERROR_MISSING_TOKEN);
		}
	}

	private static void setData(TLObject connection, OwnConnection own) {
		updateIfChanged(connection, FactoryModel.GIT_CONNECTION_NAME, own.name());
		updateIfChanged(connection, FactoryModel.GIT_CONNECTION_PROVIDER_TYPE, own.providerType());
		updateIfChanged(connection, FactoryModel.GIT_CONNECTION_URL, own.url());
		updateIfChanged(connection, FactoryModel.GIT_CONNECTION_OWNER, own.owner());
	}

	/**
	 * Stores the given token in the writable secret store under a new name.
	 *
	 * @return The name of the secret.
	 */
	private static String storeToken(String token) {
		WritableSecretStore store = writableStore();
		String name = SECRET_PREFIX + UUID.randomUUID().toString().replace("-", "");
		try {
			store.store(name, token.strip());
		} catch (I18NRuntimeException ex) {
			throw new TopLogicException(I18NConstants.ERROR_TOKEN_NOT_STORED, ex);
		}
		return name;
	}

	/**
	 * Removes the secret of the given name; a failure is logged, the secret is left behind.
	 */
	private static void removeToken(String secretName) {
		try {
			WritableSecretStore store = SecretStoreService.writableSecretStore();
			if (store != null) {
				store.remove(secretName);
			}
		} catch (RuntimeException ex) {
			Logger.warn("Failed to remove the secret " + secretName + " of a git connection: " + ex.getMessage(),
				GitConnections.class);
		}
	}

	private static WritableSecretStore writableStore() {
		WritableSecretStore store;
		try {
			store = SecretStoreService.writableSecretStore();
		} catch (I18NRuntimeException ex) {
			throw new TopLogicException(I18NConstants.ERROR_NO_WRITABLE_SECRET_STORE, ex);
		}
		if (store == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_WRITABLE_SECRET_STORE);
		}
		return store;
	}

	private static void forget(TLObject connection) {
		GitHostingService service = GitHostingService.getInstanceOrNull();
		if (service != null) {
			service.forget(idOf(connection));
		}
	}

	private static String idOf(TLObject connection) {
		return IdentifierUtil.toExternalForm(connection.tId().getObjectName());
	}

	private static String projectNames(List<TLObject> projects) {
		return projects.stream()
			.map(project -> FactoryModel.getString(project, FactoryModel.PROJECT_NAME))
			.sorted()
			.collect(Collectors.joining(", "));
	}

	private static boolean isEmpty(String value) {
		return value == null || value.isBlank();
	}

}
