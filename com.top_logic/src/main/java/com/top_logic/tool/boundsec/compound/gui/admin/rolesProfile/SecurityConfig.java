/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.compound.gui.admin.rolesProfile;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.ComponentReference;
import com.top_logic.mig.html.layout.LayoutComponent.Config;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.gui.profile.EditSecurityProfileComponent;

/**
 * {@link ConfigurationItem} for the textual persistence of the {@link RolesProfileHandler} profiles
 * as configured in the {@link EditSecurityProfileComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface SecurityConfig extends ConfigurationItem {

	/**
	 * Configuration for a {@link BoundRole} identified by its {@link BoundRole#getName() name}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface RoleProfileConfig extends NamedConfigMandatory {

		/** Configuration name for the value of {@link #getViews()}. */
		String VIEWS = "views";

		/**
		 * The {@link ViewConfig}s on which a user the represented {@link BoundRole} must have, to
		 * execute commands.
		 */
		@Key(ViewConfig.NAME)
		@DefaultContainer
		@Name(VIEWS)
		Map<ComponentName, ViewConfig> getViews();

		/**
		 * The name of the role for which is this the configuration.
		 */
		@Override
		String getName();

		/**
		 * Creates a new {@link RoleProfileConfig} for the given {@link BoundRole}.
		 * 
		 * @param role
		 *        The {@link BoundRole} to get a {@link RoleProfileConfig} for.
		 */
		static RoleProfileConfig newInstance(BoundRole role) {
			RoleProfileConfig result = TypedConfiguration.newConfigItem(RoleProfileConfig.class);
			result.setName(role.getName());
			return result;
		}

	}

	/**
	 * Configuration for a represented {@link Config}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface ViewConfig extends ComponentReference {

		/** Configuration name for the value of {@link #getCommandGroups()}. */
		String COMMAND_GROUPS = "commandGroups";

		/**
		 * The configured {@link BoundCommandGroup} for this view.
		 */
		@Key(CommandGroupConfig.COMMAND_GROUP)
		@DefaultContainer
		@Name(COMMAND_GROUPS)
		List<CommandGroupConfig> getCommandGroups();

		/**
		 * The name of the represented {@link Config}.
		 */
		@Override
		ComponentName getName();

		/**
		 * Creates a new {@link ViewConfig} for the given {@link Config}.
		 * 
		 * @param view
		 *        The {@link Config} to create {@link ViewConfig} for.
		 */
		static ViewConfig newInstance(Config view) {
			ViewConfig result = TypedConfiguration.newConfigItem(ViewConfig.class);
			result.setName(view.getName());
			return result;
		}

	}

	/**
	 * Configuration of a {@link BoundCommandGroup}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface CommandGroupConfig extends ConfigurationItem {

		/** Configuration name for the value of {@link #getCommandGroup()} */
		String COMMAND_GROUP = "name";

		/**
		 * The represented {@link BoundCommandGroup}.
		 */
		@Name(COMMAND_GROUP)
		@Mandatory
		BoundCommandGroup getCommandGroup();

		/**
		 * Setter for {@link #getCommandGroup()}
		 */
		void setCommandGroup(BoundCommandGroup group);

		/**
		 * Creates a new {@link CommandGroupConfig} for the given {@link BoundCommandGroup}.
		 * 
		 * @param group
		 *        The {@link BoundCommandGroup} to get {@link CommandGroupConfig} for.
		 */
		static CommandGroupConfig newInstance(BoundCommandGroup group) {
			CommandGroupConfig result = TypedConfiguration.newConfigItem(CommandGroupConfig.class);
			result.setCommandGroup(group);
			return result;
		}

	}

	/** Configuration name for the value of {@link #getProfiles()}. */
	String PROFILES = "profiles";

	/**
	 * All known {@link RoleProfileConfig}s.
	 */
	@Key(RoleProfileConfig.NAME_ATTRIBUTE)
	@Name(PROFILES)
	Map<String, RoleProfileConfig> getProfiles();

}


