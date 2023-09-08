/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.scripting.runtime.execution.ScriptController;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link CommandHandler} stopping a currently running script.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StopActionReplay extends ActionReplayCommand {

	/**
	 * Creates a {@link StopActionReplay} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public StopActionReplay(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext displayContext, LayoutComponent component,
			Object model, Map<String, Object> someArguments) {
		ScriptRecorderTree scriptRecorderTree = (ScriptRecorderTree) component;

		ScriptController oldController = getCurrentExecution(scriptRecorderTree);
		if (oldController != null) {
			oldController.stop();

			// Note: The component selection is updated from within the currently running script in
			// its on-stop handler.
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	private void selectNext(ScriptRecorderTree scriptRecorderTree, TLTreeNode<?> reference) {
		while (true) {
			TLTreeNode<?> parent = reference.getParent();
			if (parent != null) {
				int index = parent.getIndex(reference);
				if (index + 1 < parent.getChildCount()) {
					TLTreeNode<?> next = parent.getChildAt(index + 1);
					scriptRecorderTree.setSelected(next);
					break;
				} else {
					reference = parent;
				}
			} else {
				scriptRecorderTree.setSelected(reference);
				break;
			}
		}
	}

	@Override
	public ExecutabilityRule createExecutabilityRule() {
		return new ExecutabilityRule() {
			@Override
			public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
				if (!isScriptRunning(aComponent)) {
					return ExecutableState.createDisabledState(I18NConstants.ERROR_NO_REPLAY_RUNNING);
				}
				return ExecutableState.EXECUTABLE;
			}
		};
	}

}
