/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution.service;

import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Central point for customizing checks whether commands can be executed depending on their command
 * group and the target model state.
 * 
 * <p>
 * Per command checks can be configured through {@link ExecutabilityRule}s, see
 * {@link com.top_logic.tool.boundsec.CommandHandler.Config#getExecutability()}. Global checks based
 * on the current state of a command's target model is more convenient to realize throug a
 * {@link CommandApprovalService} configuration. This does not require adjusting all commands of all
 * view where a certain model can be displayed or edited.
 * </p>
 * 
 * @see ConfiguredCommandApprovalService
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ServiceDependencies({
	CommandGroupRegistry.Module.class
})
public abstract class CommandApprovalService extends ManagedClass {

	private static final CommandApprovalService INACTIVE = new CommandApprovalService() {
		@Override
		public ExecutableState isExecutable(LayoutComponent component, BoundCommandGroup commandGroup,
				String commandId,
				Object model, Map<String, Object> arguments) {
			return ExecutableState.EXECUTABLE;
		}
	};

	/**
	 * Creates a {@link CommandApprovalService}.
	 */
	protected CommandApprovalService() {
		super();
	}

	/**
	 * Creates a {@link CommandApprovalService}.
	 */
	protected CommandApprovalService(InstantiationContext context, ServiceConfiguration<?> config) {
		super(context, config);
	}

	/**
	 * Whether a given command is executable in a certain context.
	 * 
	 * @param component
	 *        The context component in which the command should be executed. A value of
	 *        <code>null</code> means to check whether a command with the other parameter value can
	 *        be executed in some component.
	 * @param commandGroup
	 *        The command group of the executed command.
	 * @param commandId
	 *        The concrete {@link CommandHandler#getID() ID} of the executed command. A value of
	 *        <code>null</code> means to check, if some command with the given command group can be
	 *        executed.
	 * @param model
	 *        The target model that is passed to the command execution.
	 * @param arguments
	 *        The command arguments.
	 * 
	 * @see CommandHandler#handleCommand(com.top_logic.layout.DisplayContext, LayoutComponent,
	 *      Object, Map)
	 */
	public abstract ExecutableState isExecutable(LayoutComponent component, BoundCommandGroup commandGroup,
			String commandId, Object model, Map<String, Object> arguments);

	/**
	 * Short-cut for {@link #isExecutable(LayoutComponent, BoundCommandGroup, String, Object, Map)}
	 * without command ID and arguments.
	 */
	public static boolean canExecute(LayoutComponent component, BoundCommandGroup commandGroup,
			TLObject model) {
		return getInstance().isExecutable(component, commandGroup, null, model, Collections.emptyMap()).isExecutable();
	}

	/**
	 * Short-cut for {@link #canExecute(LayoutComponent, BoundCommandGroup, TLObject)} without
	 * component.
	 */
	public static boolean canExecute(BoundCommandGroup commandGroup, BoundObject model) {
		return canExecute(null, commandGroup, model);
	}

	/**
	 * The {@link CommandApprovalService} singleton.
	 */
	public static CommandApprovalService getInstance() {
		Module module = Module.INSTANCE;
		if (!module.isActive()) {
			return INACTIVE;
		}
		return module.getImplementationInstance();
	}

	/**
	 * Singleton reference for {@link CommandApprovalService}.
	 */
	public static class Module extends TypedRuntimeModule<CommandApprovalService> {

		/**
		 * Singleton {@link CommandApprovalService.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<CommandApprovalService> getImplementation() {
			return CommandApprovalService.class;
		}

	}

}
