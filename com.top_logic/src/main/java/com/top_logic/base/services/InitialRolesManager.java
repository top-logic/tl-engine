/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services;

import java.util.Collection;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.knowledge.service.KBBasedManagedClass;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.annotate.security.RoleConfig;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;

/**
 * Installs the roles required by the application.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ServiceDependencies({
	ModelService.Module.class,
})
@Label("Initial roles")
public class InitialRolesManager extends KBBasedManagedClass<InitialRolesManager.Config> {

	/**
	 * Configuration options for {@link InitialRolesManager}.
	 */
	public interface Config extends KBBasedManagedClass.Config<InitialRolesManager> {

		/** Property name of {@link #getRoles()}. */
		String ROLES = "roles";

		/**
		 * Roles that are initially loaded into the application.
		 */
		@Name(ROLES)
		@Key(RoleConfig.NAME_ATTRIBUTE)
		Collection<RoleConfig> getRoles();

	}

	/**
	 * Creates a {@link InitialRolesManager} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public InitialRolesManager(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();

		Transaction tx = kb().beginTransaction(I18NConstants.CREATING_INITIAL_ROLES);
		try {
			init();
			tx.commit();
		} finally {
			tx.rollback();
		}
	}

	/**
	 * Allocates initial roles.
	 */
	protected void init() {
		Config config = getConfig();
		mkRoles(config.getRoles());
	}

	private void mkRoles(Collection<RoleConfig> roles) {
		for (RoleConfig roleConfig : roles) {
			String roleName = roleConfig.getName();
			mkRole(roleName);
		}
	}

	private BoundedRole mkRole(String roleName) {
		BoundedRole existingRole = BoundedRole.getRoleByName(kb(), roleName);
		if (existingRole != null) {
			return existingRole;
		}

		BoundedRole newRole = BoundedRole.createBoundedRole(roleName, kb());
		newRole.setIsSystem(true);

		newRole.setValue(BoundedRole.ATTRIBUTE_DESCRIPTION,
			Resources.getInstance().getString(I18NConstants.ROLE_DESCRIPTION.key(roleName), ""));

		Logger.info("Created system wide role " + newRole, this);
		return newRole;
	}

	/**
	 * The singleton {@link InitialRolesManager} instance.
	 */
	public static InitialRolesManager getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton reference to {@link InitialRolesManager}.
	 */
	public static final class Module extends TypedRuntimeModule<InitialRolesManager> {

		/**
		 * The {@link Module} singleton.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<InitialRolesManager> getImplementation() {
			return InitialRolesManager.class;
		}

	}

}
