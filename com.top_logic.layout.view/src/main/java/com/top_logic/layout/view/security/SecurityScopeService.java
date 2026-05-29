/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.layout.ComponentName;

/**
 * Central catalog of {@link SecurityScope}s available to the declarative view system.
 *
 * <p>
 * Scopes are defined once here as a tree of {@code id}/{@code label} entries (nesting provides
 * grouping for the security administration UI) and referenced from removable UI units via
 * {@link AccessControl}. Defining scopes centrally — rather than inline in the view — lets several
 * UI units share one scope by referencing the same id without duplicating its label.
 * </p>
 *
 * @see AccessControl
 * @see SecurityScope
 */
public class SecurityScopeService extends ConfiguredManagedClass<SecurityScopeService.Config> {

	/**
	 * Configuration of the {@link SecurityScopeService}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<SecurityScopeService> {

		/** Configuration name for {@link #getScopes()}. */
		String SCOPES = "scopes";

		/**
		 * The top-level nodes of the security scope catalog.
		 */
		@Name(SCOPES)
		@DefaultContainer
		List<ScopeConfig> getScopes();
	}

	/**
	 * A node in the security scope catalog tree.
	 *
	 * <p>
	 * A node with an {@link #getId() id} is a referenceable {@link SecurityScope}. Nesting via
	 * {@link #getScopes()} provides grouping; a node's {@link #getLabel() label} doubles as the
	 * grouping label for its children in the security administration UI.
	 * </p>
	 */
	public interface ScopeConfig extends ConfigurationItem {

		/** Configuration name for {@link #getId()}. */
		String ID = "id";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getScopes()}. */
		String SCOPES = "scopes";

		/**
		 * The unique scope id.
		 *
		 * <p>
		 * When set, this node is a {@link SecurityScope} that can be referenced from a view's
		 * {@code <access-control>}. When unset, the node is a pure grouping node.
		 * </p>
		 */
		@Name(ID)
		@Nullable
		String getId();

		/**
		 * Human-readable label for the security administration UI.
		 */
		@Name(LABEL)
		@Nullable
		ResKey getLabel();

		/**
		 * Nested scope nodes (sub-groups or sub-scopes).
		 */
		@Name(SCOPES)
		@DefaultContainer
		List<ScopeConfig> getScopes();
	}

	private final Map<String, SecurityScope> _scopesById;

	/**
	 * Creates a {@link SecurityScopeService} from configuration.
	 */
	public SecurityScopeService(InstantiationContext context, Config config) {
		super(context, config);
		Map<String, SecurityScope> index = new LinkedHashMap<>();
		indexScopes(context, config.getScopes(), index);
		_scopesById = Collections.unmodifiableMap(index);
	}

	private static void indexScopes(InstantiationContext context, List<ScopeConfig> nodes,
			Map<String, SecurityScope> index) {
		for (ScopeConfig node : nodes) {
			String id = node.getId();
			if (id != null && !id.isEmpty()) {
				if (index.containsKey(id)) {
					context.error("Duplicate security scope id '" + id + "'.");
				} else {
					index.put(id, new SecurityScope(ComponentName.newName(id), node.getLabel()));
				}
			}
			indexScopes(context, node.getScopes(), index);
		}
	}

	/**
	 * The {@link SecurityScope} with the given id.
	 *
	 * @param id
	 *        The scope id to look up.
	 * @return The scope, or {@code null} when no scope with the given id is defined.
	 */
	public SecurityScope getScope(String id) {
		return _scopesById.get(id);
	}

	/**
	 * All defined {@link SecurityScope}s.
	 */
	public Collection<SecurityScope> getAllScopes() {
		return _scopesById.values();
	}

	/**
	 * The singleton {@link SecurityScopeService} instance.
	 */
	public static SecurityScopeService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton reference to the {@link SecurityScopeService}.
	 */
	public static final class Module extends TypedRuntimeModule<SecurityScopeService> {

		/** Singleton {@link SecurityScopeService.Module} instance. */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<SecurityScopeService> getImplementation() {
			return SecurityScopeService.class;
		}

	}

}
