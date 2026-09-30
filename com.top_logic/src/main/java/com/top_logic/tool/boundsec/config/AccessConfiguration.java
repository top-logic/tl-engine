/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.config;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;

/**
 * Configuration that specifies the roles a user must have to execute commands of certain command
 * groups on various views of the application.
 */
public interface AccessConfiguration extends ConfigurationItem {

	/**
	 * @see #getProfiles()
	 */
	String PROFILES = "profiles";

	/**
	 * List access configurations for certain roles.
	 */
	@Name(PROFILES)
	@Key(ProfileConfig.ROLE)
	@DefaultContainer
	List<ProfileConfig> getProfiles();

	/**
	 * Configuration for a single role.
	 */
	interface ProfileConfig extends ConfigurationItem {
		/**
		 * @see #getRole()
		 */
		String ROLE = "name";

		/**
		 * @see #getViews()
		 */
		String VIEWS = "views";

		/**
		 * The name of the role that should get access.
		 */
		@Name(ROLE)
		String getRole();

		/**
		 * The application's views the given role should get access to.
		 */
		@Name(VIEWS)
		@DefaultContainer
		List<ViewConfig> getViews();

		/**
		 * Configuration for a component subtree.
		 */
		interface ViewConfig extends ConfigurationItem {
			/**
			 * @see #getComponent()
			 */
			String COMPONENT = "name";

			/**
			 * @see #getCommandGroups()
			 */
			String COMMAND_GROUPS = "commandGroups";

			/**
			 * The component this configuration applies to.
			 * 
			 * <p>
			 * The component must be a {@link CompoundSecurityLayout} grouping a substructure of the
			 * application's layout.
			 * </p>
			 */
			@Name(COMPONENT)
			ComponentName getComponent();

			/**
			 * The command groups of commands that can be executed, if the user has the
			 * {@link AccessConfiguration.ProfileConfig#getRole() profile's role}.
			 */
			@Name(COMMAND_GROUPS)
			@DefaultContainer
			List<CommandGroupRef> getCommandGroups();

			/**
			 * Reference to a {@link BoundCommandGroup} name.
			 * 
			 * @implNote This wrapper interface is used to allow parsing the legacy format.
			 */
			interface CommandGroupRef extends ConfigurationItem {
				/**
				 * @see #getName()
				 */
				String NAME = "name";

				/**
				 * Name of the {@link BoundCommandGroup} to which access is granted.
				 */
				@Name(NAME)
				String getName();
			}
		}
	}
}
