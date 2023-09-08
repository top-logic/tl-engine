/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.scripting.runtime.execution.DefaultScriptController;
import com.top_logic.layout.scripting.runtime.execution.LiveActionExecutor;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.TLContextManager;

/**
 * {@link CommandHandler} stating a script execution in a new session.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StartBackgroundReplayCommand extends AbstractCommandHandler {

	/**
	 * Creates a {@link StartBackgroundReplayCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public StartBackgroundReplayCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		ScriptRecorderTree scriptRecorder = (ScriptRecorderTree) component;
		TLTreeNode<?> selection = (TLTreeNode<?>) scriptRecorder.getTreeData().getTreeModel().getRoot();

		String sessionUser = TLContextManager.getCurrentUserName(context.getSubSessionContext());
		DefaultScriptController controller = new MultiUserScriptController(sessionUser, selection);

		if (controller.hasNext()) {
			ApplyApplicationAction.setCurrentExecution(scriptRecorder, controller);

			String userId = controller.currentUser();
			if (userId.equals(sessionUser)) {
				// Matching user IDs, no login required.
				Person user = context.getSubSessionContext().getPerson();
				LiveActionExecutor.startSession(context, controller, userId, user, true);
			} else {
				// Script user does not match current user. An explicit login action is
				// required.
				controller.stop();
				ApplyApplicationAction.stopExecution(scriptRecorder);
				throw ApplicationAssertions.fail(controller.currentOfAnyUser(),
					"Script user '" + userId + "' does not match current user '" + sessionUser
						+ "', an explicit login action is required.");
			}

			Runnable update = new Runnable() {
				@Override
				public void run() {
					TLTreeNode<?> current = controller.scriptPosition();
					Object lastNode =
						current == null ? scriptRecorder.getTreeData().getTreeModel().getRoot() : current;
					scriptRecorder.setSelected(lastNode);

					if (controller.isStopped()) {
						ApplyApplicationAction.stopExecution(scriptRecorder);
					} else {
						schedule(this);
					}
				}
			};
			schedule(update);
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	void schedule(Runnable update) {
		// Note: Do not take the display context as parameter, since it is not available from within
		// the scheduled runnable.
		DisplayContext context = DefaultDisplayContext.getDisplayContext();
		ScheduledExecutorService executor = context.getLayoutContext().getMainLayout().getWindowScope().getUIExecutor();
		executor.schedule(update, 2, TimeUnit.SECONDS);
	}

}
