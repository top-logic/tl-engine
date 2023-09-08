/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Aggregation of {@link CommandHandler}s that are executed in sequence.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CommandSequence extends AbstractCommandHandler {

	/**
	 * Configuration options for {@link CommandSequence}.
	 */
	@TagName(Config.DEFAULT_TAG_NAME)
	public interface Config extends AbstractCommandHandler.Config {

		/**
		 * Default tag name in e.g.
		 * {@link com.top_logic.mig.html.layout.LayoutComponent.Config#getCommands()}
		 */
		String DEFAULT_TAG_NAME = "sequence";

		@ClassDefault(CommandSequence.class)
		@Override
		public Class<? extends CommandHandler> getImplementationClass();

		/**
		 * The {@link CommandHandler}s that should be executed.
		 */
		@Subtypes(value = {
			@Subtype(tag = CallCommand.Config.DEFAULT_TAG_NAME, type = CallCommand.ConfigSystemGroup.class),
		}, adjust = true)
		List<CommandHandler.ConfigBase<? extends CommandHandler>> getCommands();

		/**
		 * One must explicitly decide about the command group of the sequence.
		 * 
		 * @see com.top_logic.tool.boundsec.CommandHandler.Config#getGroup()
		 */
		@Mandatory
		@Override
		CommandGroupReference getGroup();

		@Override
		@FormattedDefault(TARGET_NULL)
		ModelSpec getTarget();

	}

	List<CommandHandler> _commands;

	/**
	 * Creates a {@link CommandSequence} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CommandSequence(InstantiationContext context, Config config) {
		super(context, config);

		_commands = TypedConfiguration.getInstanceList(context, config.getCommands());
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
			Object model, Map<String, Object> someArguments) {

		for (CommandHandler command : _commands) {
			HandlerResult result = CommandHandlerUtil.handleCommand(command, aContext, aComponent, someArguments);
			if (!result.isSuccess()) {
				return result;
			}
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	public ExecutabilityRule createExecutabilityRule() {
		return new ExecutabilityRule() {
			@Override
			public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
				for (CommandHandler command : _commands) {
					ExecutableState state =
						command.isExecutable(aComponent, someValues);
					if (!state.isExecutable()) {
						return state;
					}
				}
				return ExecutableState.EXECUTABLE;
			}
		};
	}
}
