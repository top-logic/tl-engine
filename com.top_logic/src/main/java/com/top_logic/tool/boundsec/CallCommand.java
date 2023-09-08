/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link CommandHandler} that dynamically calls another command on the same or another component
 * using its own settings including {@link #getCommandGroup() access control}.
 * 
 * @see CommandHandlerReference Referencing a globally defined command implementation by name.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CallCommand extends AbstractCommandHandler {

	/**
	 * Configuration options for {@link CallCommand}.
	 */
	@TagName(Config.DEFAULT_TAG_NAME)
	public interface Config extends AbstractCommandHandler.Config, CommandReferenceConfig {
	
		/** Default tag name for a {@link CallCommand.Config} */
		String DEFAULT_TAG_NAME = "call";
	
		@ClassDefault(CallCommand.class)
		@Override
		public Class<? extends CallCommand> getImplementationClass();
	
		/**
		 * A {@link CallCommand} within a {@link CommandSequence} does not need its own ID.
		 * 
		 * <p>
		 * When directly registering a {@link CallCommand} on a component, a non-<code>null</code>
		 * ID is still required.
		 * </p>
		 * 
		 * @see com.top_logic.tool.boundsec.CommandHandler.Config#getId()
		 */
		@Nullable
		@NullDefault
		@Override
		public String getId();

		/**
		 * The command group must be explicitly specified, because one cannot refer to the command
		 * group of the target command, which is only given as ID.
		 * 
		 * @see com.top_logic.tool.boundsec.CommandHandler.Config#getGroup()
		 */
		@Mandatory
		@Override
		CommandGroupReference getGroup();

		/**
		 * Optional target component name.
		 */
		ComponentName getComponent();
	
	}

	/**
	 * Special {@link Config} that has a default for {@link #getGroup()}.
	 * 
	 * <p>
	 * Note: This configuration must only be used, if the command group of the called command is not
	 * relevant.
	 * </p>
	 * 
	 * @see CommandSequence.Config#getCommands()
	 */
	public interface ConfigSystemGroup extends Config {
		@FormattedDefault(SimpleBoundCommandGroup.SYSTEM_NAME)
		@Override
		public CommandGroupReference getGroup();
	}

	private String _commandId;

	private ComponentName _component;

	/**
	 * Creates a {@link CallCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CallCommand(InstantiationContext context, Config config) {
		super(context, config);
		
		_commandId = config.getCommandId();
		_component = config.getComponent();
	}
	
	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent component,
			Object model, Map<String, Object> someArguments) {
		LayoutComponent targetComponent = targetComponent(component);
		CommandHandler impl = impl(targetComponent);
		return impl.handleCommand(aContext, targetComponent, model, someArguments);
	}

	@SuppressWarnings("deprecation")
	@Override
	public Object getTargetModel(LayoutComponent component, Map<String, Object> arguments) {
		return impl(component).getTargetModel(targetComponent(component), arguments);
	}

	@Override
	public ExecutabilityRule createExecutabilityRule() {
		return new ExecutabilityRule() {
			@Override
			public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> someValues) {
				LayoutComponent targetComponent = targetComponent(component);
				CommandHandler impl = impl(targetComponent);
				return impl.isExecutable(targetComponent, model, someValues);
			}
		};
	}

	@Override
	public boolean checkSecurity(LayoutComponent component, Object model, Map<String, Object> someValues) {
		LayoutComponent targetComponent = targetComponent(component);
		CommandHandler impl = impl(targetComponent);
		return impl.checkSecurity(targetComponent, model, someValues);
	}

	@Override
	public CommandScriptWriter getCommandScriptWriter(LayoutComponent component) {
		LayoutComponent targetComponent = targetComponent(component);
		CommandHandler impl = impl(targetComponent);
		return impl.getCommandScriptWriter(targetComponent);
	}

	CommandHandler impl(LayoutComponent targetComponent) {
		return targetComponent.getCommandById(_commandId);
	}

	LayoutComponent targetComponent(LayoutComponent component) {
		if (_component != null) {
			return component.getMainLayout().getComponentByName(_component);
		}
		return component;
	}

}
