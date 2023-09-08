/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ActivateCommand;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ComponentCommandModel;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.scripting.action.CommandActionBase;
import com.top_logic.layout.scripting.recorder.ref.misc.AttributeValue;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.util.ActiveModels;
import com.top_logic.mig.html.layout.CommandDispatcher;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponentScope;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Base class for {@link ApplicationActionOp}s executing a {@link CommandHandler}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class CommandActionOpBase<S extends CommandActionBase> extends ComponentActionOp<S> {

	/**
	 * Creates a {@link CommandActionOpBase} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CommandActionOpBase(InstantiationContext context, S config) {
		super(context, config);
	}

	@Override
	protected Object process(ActionContext context, LayoutComponent component, Object argument) {
		HandlerResult result = executeCommand(context, component);
		checkResult(result);
		return argument;
	}

	/**
	 * Executes the referenced command.
	 * 
	 * @param context
	 *        The current {@link ActionContext}.
	 * @param component
	 *        The context component.
	 * @return The {@link HandlerResult} produced by the command execution.
	 */
	protected HandlerResult executeCommand(ActionContext context, LayoutComponent component) {
		CommandHandler command = getCommandHandler(context, component);
		Map<String, Object> arguments = getArguments(context, command, component);
		
		LayoutComponentScope searchedScope = null;

		LayoutComponent scopeComponent = component;
		while (scopeComponent != null) {
			LayoutComponentScope scope = scopeComponent.getEnclosingFrameScope();
			if (scope != searchedScope) {
				searchedScope = scope;

				for (Control control : ActiveModels.visibleControls(searchedScope)) {
					if (control instanceof ButtonControl) {
						ButtonControl button = (ButtonControl) control;
						CommandModel model = button.getModel();
						if (model instanceof ComponentCommandModel) {
							ComponentCommandModel componentCommand = (ComponentCommandModel) model;

							if (componentCommand.getCommandHandler() != command) {
								continue;
							}
							if (componentCommand.getComponent() != component) {
								continue;
							}
							if (!Utils.equals(componentCommand.getArguments(), arguments)) {
								continue;
							}

							return button.executeCommand(CommandDispatcher.approved(context.getDisplayContext()),
								ActivateCommand.COMMAND_ID, arguments);
						}
					}
				}
			}

			scopeComponent = scopeComponent.getParent();
		}

		// No visible button was found, try the legacy way - the command may be hidden in some menu.
		
		DisplayContext executionContext = context.getDisplayContext();
		HandlerResult result =
			CommandDispatcher.getInstance().dispatchCommand(command, CommandDispatcher.approved(executionContext),
				component, arguments);
		return result;
	}

	/**
	 * The {@link CommandHandler} to execute.
	 */
	protected abstract CommandHandler getCommandHandler(ActionContext context, LayoutComponent component);

	/**
	 * Subclasses should overwrite this method if they need to do any further analysis on the
	 * result. (Instead of overwriting 'process' itself.)
	 */
	protected void checkResult(HandlerResult result) {
		ApplicationAssertions.assertSuccess(getConfig(), result);
	}

	/**
	 * The arguments to pass to the handler.
	 * 
	 * @param command
	 *        The {@link CommandHandler} to pass arguments to.
	 * @param component
	 *        The {@link LayoutComponent} to pass as argument for the handler.
	 * 
	 * @see #getCommandHandler(ActionContext, LayoutComponent)
	 */
	protected Map<String, Object> getArguments(ActionContext context, CommandHandler command,
			LayoutComponent component) {
		Map<String, AttributeValue> argumentRefs = config.getArguments();
		Map<String, Object> arguments = resolveArgumentValues(context, argumentRefs);
		return arguments;
	}

	private static Map<String, Object> resolveArgumentValues(ActionContext context,
			Map<String, AttributeValue> argumentRefs) {
		HashMap<String, Object> arguments = MapUtil.newMap(argumentRefs.size() + 1);
		for (Map.Entry<String, AttributeValue> entry : argumentRefs.entrySet()) {
			String key = entry.getKey();
			AttributeValue attributeValue = entry.getValue();
			Object resolvedValue = context.resolve(attributeValue.getValue());
			arguments.put(key, resolvedValue);
		}
		return arguments;
	}

}
