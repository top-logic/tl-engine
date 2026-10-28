/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.agent;

import java.util.List;

import com.top_logic.factory.ci.CIProvider;
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
	 * The {@link #projectInstructions(TLObject, CIProvider, boolean) instructions} of the project of
	 * the workspace, with the {@link CIService CI server} of the installation.
	 */
	@Override
	public String instructions() {
		return projectInstructions(FactoryModel.getProject(workspace()), CIService.configuredProvider(),
			hasCIRoute());
	}

	/**
	 * What the agent working on the given project is told in addition to its instructions.
	 *
	 * <p>
	 * For a project with a {@link FactoryModel#PROJECT_REPOSITORY repository}, that the work of the
	 * agent reaches the application only by commit and push, see
	 * {@link RepositorySource#agentInstructions(String, java.util.List)}, naming the
	 * {@link ProjectWorkspaces#protectedFiles(TLObject, CIProvider) protected files of the project}
	 * unless the default branch is {@link FactoryModel#PROJECT_BRANCH_PROTECTED known to be
	 * unprotected}; for a project with a {@link FactoryModel#PROJECT_CI_JOB CI job}, with a CI
	 * server and the {@link CIRoute} in the workspace proxy, how it builds the application on the
	 * CI server, see {@link CIRoute#agentInstructions()}.
	 * </p>
	 *
	 * @param project
	 *        The project.
	 * @param ci
	 *        The CI server, {@code null} if CI is switched off.
	 * @param ciRoute
	 *        Whether the workspace proxy has a {@link CIRoute}.
	 * @return The instructions, {@code null} for a project without repository.
	 */
	public static String projectInstructions(TLObject project, CIProvider ci, boolean ciRoute) {
		RepositoryName repository = FactoryModel.getRepository(project);
		if (repository == null) {
			return null;
		}
		boolean unprotected = Boolean.FALSE.equals(project.tValueByName(FactoryModel.PROJECT_BRANCH_PROTECTED));
		String result = RepositorySource.agentInstructions(repository.getFullName(),
			unprotected ? List.of() : ProjectWorkspaces.protectedFiles(project, ci));
		if (ci != null && ciRoute && FactoryModel.getCIJob(project) != null) {
			result = result + " " + CIRoute.agentInstructions();
		}
		return result;
	}

	/**
	 * Whether the running workspace proxy has a {@link CIRoute}.
	 */
	private static boolean hasCIRoute() {
		WorkspaceProxyService proxy = WorkspaceProxyService.getInstance();
		return proxy != null && proxy.isEnabled() && proxy.getRoute(CIRoute.class) != null;
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
