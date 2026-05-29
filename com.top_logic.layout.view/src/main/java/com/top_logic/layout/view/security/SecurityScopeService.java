/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KBBasedManagedClass;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;

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
 * <p>
 * At startup the service materializes a persistent {@link PersBoundComp} for every configured scope
 * (keyed by its id), so the existing security administration tooling can assign roles to view
 * security scopes just like to legacy components.
 * </p>
 *
 * @see AccessControl
 * @see SecurityScope
 */
@ServiceDependencies({
	KnowledgeBaseFactory.Module.class,
	PersistencyLayer.Module.class,
	SecurityComponentCache.Module.class,
	BoundHelper.Module.class,
})
public class SecurityScopeService extends KBBasedManagedClass<SecurityScopeService.Config> {

	/**
	 * Configuration of the {@link SecurityScopeService}.
	 */
	public interface Config extends KBBasedManagedClass.Config<SecurityScopeService> {

		/** Configuration name for {@link #getScopes()}. */
		String SCOPES = "scopes";

		/**
		 * The top-level nodes of the security scope catalog.
		 */
		@Name(SCOPES)
		@EntryTag("scope")
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
		@EntryTag("scope")
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

	@Override
	protected void startUp() {
		super.startUp();
		materializePersBoundComps();
	}

	/**
	 * Ensures a persistent {@link PersBoundComp} exists for every configured scope, so roles can be
	 * assigned to view security scopes via the existing security administration.
	 *
	 * <p>
	 * Runs as part of service startup, which may already be within an enclosing transaction. A
	 * transaction is therefore opened only when there is actually something to create (and is then
	 * committed, never rolled back), so the common idempotent re-boot does not touch the enclosing
	 * transaction.
	 * </p>
	 */
	private void materializePersBoundComps() {
		List<ComponentName> missing = new ArrayList<>();
		for (SecurityScope scope : _scopesById.values()) {
			ComponentName name = scope.getSecurityId();
			if (PersBoundComp.getInstance(kb(), name) == null) {
				missing.add(name);
			}
		}
		if (missing.isEmpty()) {
			return;
		}
		try (Transaction tx = kb().beginTransaction(I18NConstants.CREATING_SECURITY_SCOPES)) {
			for (ComponentName name : missing) {
				PersBoundComp.createInstance(kb(), name);
			}
			tx.commit();
		}
		SecurityComponentCache.setupCache();
		Logger.info("Created " + missing.size() + " security scope object(s).", SecurityScopeService.class);
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
