/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution.service.filter;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandGroupReference;

/**
 * {@link ExecutionContextFilter} that selects all commands of a certain {@link BoundCommandGroup}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CommandWithGroup extends AbstractConfiguredInstance<CommandWithGroup.Config>
		implements ExecutionContextFilter {

	/**
	 * Configuration options for {@link CommandWithGroup}.
	 */
	@TagName("command-with-group")
	public interface Config extends PolymorphicConfiguration<CommandWithGroup> {

		/**
		 * The {@link BoundCommandGroup} to match.
		 */
		@Mandatory
		CommandGroupReference getCommandGroup();

	}

	private BoundCommandGroup _group;

	/**
	 * Creates a {@link CommandWithGroup} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CommandWithGroup(InstantiationContext context, Config config) {
		super(context, config);
		_group = config.getCommandGroup().resolve();
	}

	@Override
	public MatchResult matchesExecutionContext(LayoutComponent component, BoundCommandGroup commandGroup,
			String commandId) {
		return MatchResult.fromBoolean(_group.equals(commandGroup));
	}

}
