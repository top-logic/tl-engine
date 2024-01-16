/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services;

import java.util.Collection;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.Utils;
import com.top_logic.knowledge.service.KBBasedManagedClass;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.Messages;

/**
 * The {@link InitialGroupManager} installs groups and roles for the application:
 * 
 * <p>
 * It ensures that for each existing user there is a representative group.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
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
		 * Name of the group all persons have.
		 * 
		 * <p>
		 * If the "default group" is empty, then there is no group which all persons have.
		 * </p>
		 */
		@Name(DEFAULT_GROUP)
		String getDefaultGroup();

		/**
		 * Groups to bring to existence during startup.
		 */
		@Name(GROUPS)
		@Key(GroupConfig.NAME)
		Collection<GroupConfig> getGroups();

		/**
		 * Definition of a group.
		 */
		public interface GroupConfig extends ConfigurationItem {
			/** Property name of {@link #getName()}. */
			String NAME = "name";

			/**
			 * Name of the group to create.
			 */
			@Name(NAME)
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
	public InitialGroupManager(InstantiationContext context, Config config) throws ModuleException {
		super(context, config);

		Transaction tx = kb().beginTransaction(Messages.CREATING_INITIAL_GROUPS_AND_ROLES.fill());
		try {
			init(config);

			tx.commit();
		} catch (KnowledgeBaseException ex) {
			throw new ModuleException("Unable to create initial groups.", ex, InitialGroupManager.class);
		} finally {
			tx.rollback();
		}

	}

	private void init(Config config) {
		mkGroups(config.getGroups(), config.getDefaultGroup());
	}

	private void mkGroups(Collection<Config.GroupConfig> groups, String defaultGroup) {
		for (Config.GroupConfig groupConfig : groups) {
			String groupName = groupConfig.getName();
			Group group = mkGroup(groupName, defaultGroup);
			if (defaultGroup.equals(groupName)) {
				_defaultGroup = group;
			}
		}
		if (!defaultGroup.isEmpty() && _defaultGroup == null) {
			throw new ConfigurationError("No group with name " + defaultGroup + " found to use as default group.");
		}
	}

	private Group mkGroup(String groupName, String defaultGroup) {
		{
			Group existingGroup = Group.getGroupByName(kb(), groupName);
			if (existingGroup != null) {
				return existingGroup;
			}

			Group newGroup = Group.createGroup(groupName, kb());
			if (Utils.equals(defaultGroup, groupName)) {
				newGroup.setDefaultGroup(true);
			}
			newGroup.setIsSystem(true);
			Logger.info("Created system wide group " + newGroup, this);
			return newGroup;
		}
	}

	/**
	 * Returns the {@link Group} which each person has.
	 * 
	 * @return May be <code>null</code>.
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
