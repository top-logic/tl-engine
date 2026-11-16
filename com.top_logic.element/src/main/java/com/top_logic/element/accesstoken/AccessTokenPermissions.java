/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.accesstoken;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.category.RuntimeCategory;
import com.top_logic.basic.util.ResKey;

/**
 * The permissions an {@link AccessToken} can grant.
 *
 * <p>
 * A token carries the names of its permissions, and an endpoint that accepts tokens accepts only
 * those with its permission. This service knows no endpoint: the module that offers an endpoint
 * contributes the permission it requires through its configuration, as
 * </p>
 *
 * <pre>
 * &lt;config service-class="com.top_logic.element.accesstoken.AccessTokenPermissions"&gt;
 *   &lt;instance&gt;
 *     &lt;permissions&gt;
 *       &lt;permission name="ui-act" label="..." implies="ui-observe"/&gt;
 *     &lt;/permissions&gt;
 *   &lt;/instance&gt;
 * &lt;/config&gt;
 * </pre>
 *
 * <p>
 * A token can only be created with permissions listed here.
 * </p>
 *
 * @see AccessTokens#create(com.top_logic.knowledge.wrap.person.Person, String, Integer, Collection,
 *      String)
 */
@Label("Access token permissions")
@ServiceCategory.Of(RuntimeCategory.class)
public class AccessTokenPermissions extends ConfiguredManagedClass<AccessTokenPermissions.Config> {

	/**
	 * Configuration of {@link AccessTokenPermissions}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<AccessTokenPermissions> {

		/** @see #getPermissions() */
		String PERMISSIONS = "permissions";

		/**
		 * The permissions an access token can grant, by their names.
		 */
		@Name(PERMISSIONS)
		@Key(PermissionConfig.NAME_ATTRIBUTE)
		@EntryTag(PermissionConfig.TAG_NAME)
		Map<String, PermissionConfig> getPermissions();
	}

	/**
	 * One permission an access token can grant.
	 *
	 * <p>
	 * The {@link #getName() name} is what a token stores; it contains no white space, because a
	 * token stores the names of its permissions separated by
	 * {@link AccessTokens#PERMISSION_SEPARATOR}.
	 * </p>
	 */
	public interface PermissionConfig extends NamedConfigMandatory {

		/** Tag of a permission in {@link Config#getPermissions()}. */
		String TAG_NAME = "permission";

		/** @see #getLabel() */
		String LABEL = "label";

		/** @see #getImplies() */
		String IMPLIES = "implies";

		/** @see #getSessionBinding() */
		String SESSION_BINDING = "session-binding";

		/**
		 * How the permission is shown to the person creating a token.
		 */
		@Name(LABEL)
		@Mandatory
		ResKey getLabel();

		/**
		 * Names of further permissions a token with this permission has as well.
		 */
		@Name(IMPLIES)
		@Format(CommaSeparatedStrings.class)
		List<String> getImplies();

		/**
		 * How the endpoint of the permission finds the session the token was issued from.
		 *
		 * <p>
		 * A permission with a session binding acts only in the session the token was issued from:
		 * a token with such a permission stores the key of that session, under which the binding
		 * registers it. Empty for a permission that does not bind to the session.
		 * </p>
		 *
		 * @see AccessToken#getSessionKey()
		 */
		@Name(SESSION_BINDING)
		PolymorphicConfiguration<? extends SessionBinding> getSessionBinding();
	}

	private final Map<String, SessionBinding> _sessionBindings = new HashMap<>();

	/**
	 * Creates a {@link AccessTokenPermissions} from configuration.
	 */
	public AccessTokenPermissions(InstantiationContext context, Config config) {
		super(context, config);

		Map<String, PermissionConfig> permissions = config.getPermissions();
		for (PermissionConfig permission : permissions.values()) {
			String name = permission.getName();
			if (permission.getSessionBinding() != null) {
				_sessionBindings.put(name, context.getInstance(permission.getSessionBinding()));
			}
			if (name.isEmpty() || !name.equals(name.strip()) || name.chars().anyMatch(Character::isWhitespace)) {
				context.error("An access token permission name must not contain white space: '" + name + "' at "
					+ permission.location());
			}
			for (String implied : permission.getImplies()) {
				if (!permissions.containsKey(implied)) {
					context.error("The access token permission '" + name + "' implies the unknown permission '"
						+ implied + "' at " + permission.location());
				}
			}
		}
	}

	/**
	 * All permissions an access token can grant.
	 */
	public Collection<PermissionConfig> getPermissions() {
		return getConfig().getPermissions().values();
	}

	/**
	 * The permission with the given name, or <code>null</code> if no module offers it.
	 */
	public PermissionConfig getPermission(String name) {
		return getConfig().getPermissions().get(name);
	}

	/**
	 * The {@link PermissionConfig#getSessionBinding() session binding} of the permission with the
	 * given name, or <code>null</code> if it has none.
	 */
	public SessionBinding getSessionBinding(String name) {
		return _sessionBindings.get(name);
	}

	/**
	 * Whether any of the given permissions binds to the session, that is, has a
	 * {@link PermissionConfig#getSessionBinding() session binding}.
	 */
	public boolean isBindingSession(Collection<String> names) {
		for (String name : names) {
			if (_sessionBindings.containsKey(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The given permissions together with all they {@link PermissionConfig#getImplies() imply},
	 * transitively.
	 *
	 * <p>
	 * A name no module offers stands for itself and implies nothing.
	 * </p>
	 */
	public Set<String> withImplied(Collection<String> names) {
		Set<String> result = new LinkedHashSet<>();
		Deque<String> open = new ArrayDeque<>(names);
		while (!open.isEmpty()) {
			String name = open.pop();
			if (result.add(name)) {
				PermissionConfig permission = getPermission(name);
				if (permission != null) {
					open.addAll(permission.getImplies());
				}
			}
		}
		return result;
	}

	/**
	 * The {@link AccessTokenPermissions} of the application.
	 */
	public static AccessTokenPermissions getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module of the {@link AccessTokenPermissions}.
	 */
	public static final class Module extends TypedRuntimeModule<AccessTokenPermissions> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<AccessTokenPermissions> getImplementation() {
			return AccessTokenPermissions.class;
		}
	}
}
