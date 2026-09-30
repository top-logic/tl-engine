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
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.knowledge.service.KBBasedManagedClass;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.model.ModelService;

/**
 * Installs the groups required by the application.
 *
 * <p>
 * It ensures that for each existing user there is a representative group.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ServiceDependencies({
	ModelService.Module.class,
})
@Label("Initial groups")
public class InitialGroupManager extends KBBasedManagedClass<InitialGroupManager.Config> {

	/**
	 * Configuration options for {@link InitialGroupManager}.
	 */
	public interface Config extends KBBasedManagedClass.Config<InitialGroupManager> {

		/** Property name of {@link #getGroups()}. */
		String GROUPS = "groups";

		/** Property name of {@link #getDefaultGroup()}. */
		String DEFAULT_GROUP = "default-group";

		/**
		 * Name of the group every newly created account is added to.
		 * 
		 * <p>
		 * The anonymous account is excluded, it is never added to the default group.
		 * </p>
		 * 
		 * <p>
		 * Changing this setting affects only accounts created afterwards. Accounts that already
		 * exist are neither added to nor removed from a group.
		 * </p>
		 * 
		 * <p>
		 * If empty, there is no default group and a new account is not added to any group.
		 * </p>
		 */
		@Name(DEFAULT_GROUP)
		String getDefaultGroup();

		/**
		 * Groups to bring to existence during startup.
		 */
		@Name(GROUPS)
		@Key(GroupConfig.NAME_ATTRIBUTE)
		Collection<GroupConfig> getGroups();

		/**
		 * Definition of a group.
		 */
		public interface GroupConfig extends NamedConfigMandatory {

			/**
			 * Name of the group to create.
			 */
			@Override
			String getName();
		}
	}

	/** @see #getDefaultGroup() */
	private Group _defaultGroup;

	/**
	 * Creates a {@link InitialGroupManager} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public InitialGroupManager(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();

		Transaction tx = kb().beginTransaction(I18NConstants.CREATING_INITIAL_GROUPS);
		try {
			init();
			tx.commit();
		} finally {
			tx.rollback();
		}
	}

	/**
	 * Allocates initial groups.
	 */
	protected void init() {
		Config config = getConfig();
		mkGroups(config.getGroups(), config.getDefaultGroup());
	}

	private void mkGroups(Collection<Config.GroupConfig> groups, String defaultGroup) {
		for (Config.GroupConfig groupConfig : groups) {
			String groupName = groupConfig.getName();
			Group group = mkGroup(groupName);
			if (defaultGroup.equals(groupName)) {
				_defaultGroup = group;
			}
		}
		if (!defaultGroup.isEmpty() && _defaultGroup == null) {
			Logger.error("No group with name " + defaultGroup + " found to use as default group.",
				InitialGroupManager.class);
		}
	}

	private Group mkGroup(String groupName) {
		Group existingGroup = Group.getGroupByName(kb(), groupName);
		if (existingGroup != null) {
			return existingGroup;
		}

		Group newGroup = Group.createGroup(groupName);
		newGroup.setIsSystem(true);
		Logger.info("Created system wide group " + newGroup, this);
		return newGroup;
	}

	/**
	 * The {@link Group} every newly created account is added to.
	 * 
	 * <p>
	 * The anonymous account is excluded, it is never added to the default group. Changing
	 * {@link Config#getDefaultGroup()} affects only accounts created afterwards: accounts that
	 * already exist are neither added to nor removed from a group.
	 * </p>
	 * 
	 * @return May be <code>null</code>, if no default group is configured.
	 * 
	 * @see Config#getDefaultGroup()
	 */
	public Group getDefaultGroup() {
		return _defaultGroup;
	}

	/**
	 * The singleton {@link InitialGroupManager} instance.
	 */
	public static InitialGroupManager getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton reference to {@link InitialGroupManager}.
	 */
	public static final class Module extends TypedRuntimeModule<InitialGroupManager> {

		/**
		 * The {@link Module} singleton.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<InitialGroupManager> getImplementation() {
			return InitialGroupManager.class;
		}

	}

}
