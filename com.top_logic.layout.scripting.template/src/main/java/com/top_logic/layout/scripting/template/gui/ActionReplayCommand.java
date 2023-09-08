/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.runtime.execution.LiveActionExecutor;
import com.top_logic.layout.scripting.runtime.execution.ScriptController;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * Common base class for {@link CommandHandler}s controlling script execution.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ActionReplayCommand extends AbstractCommandHandler {

	/**
	 * Component property holding the active {@link LiveActionExecutor}.
	 */
	private static final Property<ScriptController> CURRENT_EXECUTION =
		TypedAnnotatable.property(ScriptController.class, "currentScript");

	/**
	 * Creates a {@link ActionReplayCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	protected ActionReplayCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Removes the currently running {@link LiveActionExecutor} from the given component.
	 */
	protected static void stopExecution(LayoutComponent component) {
		component.reset(CURRENT_EXECUTION);
	}

	/**
	 * Whether a script is currently running in the context of the given component.
	 */
	protected static boolean isScriptRunning(LayoutComponent component) {
		return component.get(CURRENT_EXECUTION) != null;
	}

	/**
	 * The {@link ScriptController} of the running script, or <code>null</code>, if no script is
	 * currently running.
	 * 
	 * @see #isScriptRunning(LayoutComponent)
	 */
	protected static ScriptController getCurrentExecution(final LayoutComponent component) {
		return component.get(CURRENT_EXECUTION);
	}

	/**
	 * Starts a script execution by adding the given {@link LiveActionExecutor} to the given
	 * component.
	 */
	protected static void setCurrentExecution(LayoutComponent component, ScriptController controller) {
		component.set(CURRENT_EXECUTION, controller);
	}

}
