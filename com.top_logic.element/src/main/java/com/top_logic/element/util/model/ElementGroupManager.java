/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.util.model;

import com.top_logic.base.services.InitialGroupManager;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.config.RoleAssignment;
import com.top_logic.element.config.SingletonConfig;
import com.top_logic.element.config.annotation.TLSingletons;
import com.top_logic.model.TLModule;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.model.ModelService;

/**
 * {@link InitialGroupManager} that does also role assignments on singletons.
 */
public class ElementGroupManager extends InitialGroupManager {

	/**
	 * Creates a {@link ElementGroupManager} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ElementGroupManager(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void init() {
		super.init();
		setupRoles();

	}

	private void setupRoles() {
		for (TLModule module : ModelService.getApplicationModel().getModules()) {
			TLSingletons singletons = module.getAnnotation(TLSingletons.class);
			if (singletons == null) {
				continue;
			}

			for (SingletonConfig singleton : singletons.getSingletons()) {
				setupRoles(module, singleton);
			}
		}
	}

	private void setupRoles(TLModule module, SingletonConfig singleton) {
		for (RoleAssignment assignment : singleton.getRoleAssignments()) {
			for (String roleName : assignment.getRoles()) {
				BoundedRole role = BoundedRole.getDefinedRole(module, roleName);
				if (role == null) {
					Logger.error("Role '" + roleName + "' used in assignment at '" + assignment.location()
						+ "' is not defined.", ElementGroupManager.class);
				}

				Group group = Group.getGroupByName(assignment.getGroup());
				if (group == null) {
					Logger.error("Reference to undefined group '" + assignment.getGroup() + "' in assignment at '"
						+ assignment.location() + "'.", ElementGroupManager.class);
				}

				BoundedRole.assignRole(module.getSingleton(singleton.getName()), group, role);
			}
		}
	}

}
