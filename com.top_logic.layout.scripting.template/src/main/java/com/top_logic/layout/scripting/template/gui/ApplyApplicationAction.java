/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.scripting.runtime.LiveActionContext;
import com.top_logic.layout.scripting.runtime.execution.DefaultScriptController;
import com.top_logic.layout.scripting.runtime.execution.LiveActionExecutor;
import com.top_logic.layout.scripting.runtime.execution.ScriptController;
import com.top_logic.layout.scripting.runtime.execution.ScriptDriver;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.NoSelectionDisabled;
import com.top_logic.util.TLContextManager;

/**
 * {@link CommandHandler} that executes the selected action.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ApplyApplicationAction extends ActionReplayCommand {

	/**
	 * Configuration options for {@link ApplyApplicationAction}
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/**
		 * The execution {@link Mode}.
		 */
		Mode getMode();

		@FormattedDefault(SimpleBoundCommandGroup.SYSTEM_NAME)
		@Override
		CommandGroupReference getGroup();

	}

	/**
	 * The script execution mode.
	 */
	public enum Mode {
		/**
		 * Execute only a single step.
		 */
		STEP_INTO,

		/**
		 * Execute until the end of the current group.
		 */
		STEP_OUT,

		/**
		 * Execute the whole selected group including all descending actions.
		 */
		STEP_OVER,

		/**
		 * Start executing the script and execute until the end of the script.
		 */
		PLAY
	}

	private static final String I18N_NO_ACTION = "tl.scripting.applyAction.noAction";

	private Mode _mode;

	/**
	 * Creates a {@link ApplyApplicationAction} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ApplyApplicationAction(InstantiationContext context, Config config) {
		super(context, config);

		_mode = config.getMode();
	}

	@Override
	public HandlerResult handleCommand(DisplayContext displayContext, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		ScriptRecorderTree scriptRecorder = (ScriptRecorderTree) component;

		LiveActionContext actionContext = new LiveActionContext(displayContext, component);

		DefaultMutableTLTreeNode selection = scriptRecorder.getSelectedNode();
		if (selection == null) {
			return HandlerResult.error(I18N_NO_ACTION, ApplyApplicationAction.class);
		}

		String currentUserName = TLContextManager.getCurrentUserName(displayContext.getSubSessionContext());

		ScriptController controller = new DefaultScriptController(currentUserName, selection);
		switch (_mode) {
			case STEP_INTO: {
				controller.setBreakPoint(DefaultScriptController.followingNode(selection));
				controller.setBreakPoint(DefaultScriptController.followingSibling(selection));
				break;
			}
			case STEP_OUT: {
				if (selection.getParent() != null) {
					controller.setBreakPoint(DefaultScriptController.followingSibling(selection.getParent()));
				}
				break;
			}
			case STEP_OVER: {
				controller.setBreakPoint(DefaultScriptController.followingSibling(selection));
				break;
			}
			case PLAY: {
				// Execute until the end of the script is reached.
				break;
			}
		}

		LiveActionExecutor executor =
			new LiveActionExecutor(actionContext, controller, currentUserName, true, (exec) -> {
			ApplyApplicationAction.stopExecution(scriptRecorder);

			TLTreeNode<?> current = exec.scriptPosition();
			Object failedNode = current == null ? scriptRecorder.getTreeData().getTreeModel().getRoot() : current;
			scriptRecorder.setSelected(failedNode);
		});

		ApplyApplicationAction.setCurrentExecution(scriptRecorder, controller);

		ScriptDriver scriptDriver = new ScriptDriver(scriptRecorder.getEnclosingFrameScope(), executor);
		scriptRecorder.set(ScriptRecorderTree.SCRIPT_DRIVER, scriptDriver);
		return scriptDriver.next(displayContext);
	}

	@Override
	public ExecutabilityRule createExecutabilityRule() {
		return CombinedExecutabilityRule.combine(NoSelectionDisabled.INSTANCE, DisabledWhenBuzzy.INSTANCE);
	}

}