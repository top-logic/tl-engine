/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.agent;

import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.llm.LLMProxyService;
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.factory.project.ProjectWorkspaces;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;

/**
 * Giving a task to the coding agent of a workspace: whether the agent can work there, and the
 * conversation a task starts.
 *
 * <p>
 * The conversation is the subject of the chat that talks to the agent (see
 * {@link AgentConversationResolver}); the task is its first message, sent by the chat.
 * </p>
 */
public final class AgentTasks {

	private AgentTasks() {
		// Static utility.
	}

	/**
	 * Why the coding agent cannot work in the given workspace now, or {@code null} if it can.
	 *
	 * <p>
	 * The agent works in a workspace whose container runs and has the agent tools, i.e. runs the
	 * current workspace image, and needs the LLM proxy of the control application. The application
	 * of the workspace need not run: the agent is needed most when it does not start.
	 * </p>
	 *
	 * @param workspace
	 *        The workspace of a project, {@code null} for a project without one.
	 */
	public static ResKey unavailable(TLObject workspace) {
		if (workspace == null) {
			return I18NConstants.ERROR_NO_WORKSPACE;
		}
		if (WorkspaceAgentService.getInstance() == null) {
			return I18NConstants.ERROR_NO_AGENT_SERVICE;
		}
		LLMProxyService proxy = LLMProxyService.getInstance();
		if (proxy == null || !proxy.isEnabled()) {
			return I18NConstants.ERROR_NO_LLM_ACCESS;
		}
		TLObject project = FactoryModel.getProject(workspace);
		Object name = project == null ? FactoryModel.getWorkspaceId(workspace)
			: project.tValueByName(FactoryModel.PROJECT_NAME);
		if (!FactoryModel.getStatus(workspace).isContainerRunning()) {
			return I18NConstants.ERROR_WORKSPACE_NOT_RUNNING__WORKSPACE.fill(name);
		}
		if (ProjectWorkspaces.isImageOutdated(workspace)) {
			return I18NConstants.ERROR_AGENT_TOOLS_OUTDATED__WORKSPACE.fill(name);
		}
		return null;
	}

	/**
	 * Creates a conversation with the coding agent of the given workspace for the given task, in a
	 * transaction of its own.
	 *
	 * @param workspace
	 *        The workspace whose agent is to work on the task.
	 * @param task
	 *        What the agent is to do, the first message of the conversation.
	 * @return The new {@link FactoryModel#AGENT_CONVERSATION}.
	 * @throws TopLogicException
	 *         If the agent cannot work in the workspace now, see {@link #unavailable(TLObject)}, or
	 *         the task is empty.
	 */
	public static TLObject createConversation(TLObject workspace, String task) {
		ResKey unavailable = unavailable(workspace);
		if (unavailable != null) {
			throw new TopLogicException(unavailable);
		}
		String text = task == null ? "" : task.trim();
		if (text.isEmpty()) {
			throw new TopLogicException(I18NConstants.ERROR_EMPTY_TASK);
		}
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(I18NConstants.COMMIT_CONVERSATION)) {
			TLObject conversation = FactoryModel.createConversation(workspace, text);
			tx.commit();
			return conversation;
		}
	}

}
