/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.agent;

import com.top_logic.factory.ci.CIRoute;
import com.top_logic.factory.ci.CIService;
import com.top_logic.factory.git.RepositoryName;
import com.top_logic.factory.git.RepositorySource;
import com.top_logic.factory.project.FactoryAccess;
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.factory.project.ProjectWorkspaces;
import com.top_logic.factory.proxy.WorkspaceProxyService;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;

/**
 * The {@link ConversationTarget} of a stored {@link FactoryModel#AGENT_CONVERSATION}.
 *
 * <p>
 * The agent works in the directory of the application of the workspace's project; it is told how
 * its work reaches the application if the project has a repository.
 * </p>
 */
public class StoredConversation implements ConversationTarget {

	private final TLObject _conversation;

	/**
	 * Creates a {@link StoredConversation}.
	 *
	 * @param conversation
	 *        The stored {@link FactoryModel#AGENT_CONVERSATION}.
	 */
	public StoredConversation(TLObject conversation) {
		_conversation = conversation;
	}

	/**
	 * The stored {@link FactoryModel#AGENT_CONVERSATION}.
	 */
	public TLObject getConversation() {
		return _conversation;
	}

	private TLObject workspace() {
		return FactoryModel.getConversationWorkspace(_conversation);
	}

	/**
	 * A user continues a conversation if the user may write it, that is the project of its
	 * workspace.
	 */
	@Override
	public void checkContinue(Person user) {
		FactoryAccess.checkWrite(user, _conversation);
	}

	@Override
	public String workspaceId() {
		return FactoryModel.getWorkspaceId(workspace());
	}

	@Override
	public Object displayName() {
		return FactoryModel.getProject(workspace());
	}

	@Override
	public String workingDir() {
		TLObject project = FactoryModel.getProject(workspace());
		return ApplicationWorkspaces.appDir(FactoryModel.getString(project, FactoryModel.PROJECT_ARTIFACT_ID));
	}

	/**
	 * For a project with a {@link FactoryModel#PROJECT_REPOSITORY repository}, that the work of the
	 * agent reaches the application only by commit and push, see
	 * {@link RepositorySource#agentInstructions(String, java.util.List)}; with {@link CIService CI}
	 * and the {@link CIRoute} in the workspace proxy, how it builds the application on the CI
	 * server, see {@link CIRoute#agentInstructions()}.
	 */
	@Override
	public String instructions() {
		RepositoryName repository = FactoryModel.getRepository(FactoryModel.getProject(workspace()));
		if (repository == null) {
			return null;
		}
		String result =
			RepositorySource.agentInstructions(repository.getFullName(), ProjectWorkspaces.getProtectedFiles());
		if (hasCIRoute()) {
			result = result + " " + CIRoute.agentInstructions();
		}
		return result;
	}

	/**
	 * Whether a CI server is configured and the running workspace proxy has a {@link CIRoute}.
	 */
	private static boolean hasCIRoute() {
		WorkspaceProxyService proxy = WorkspaceProxyService.getInstance();
		return CIService.configuredProvider() != null && proxy != null && proxy.isEnabled()
			&& proxy.getRoute(CIRoute.class) != null;
	}

	@Override
	public String sessionId() {
		return FactoryModel.getSessionId(_conversation);
	}

	@Override
	public void storeSessionId(String sessionId) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(I18NConstants.COMMIT_SESSION_ID)) {
			_conversation.tUpdateByName(FactoryModel.CONVERSATION_SESSION_ID, sessionId);
			tx.commit();
		}
	}

}
