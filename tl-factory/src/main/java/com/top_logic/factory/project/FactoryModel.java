/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.project;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.factory.ci.CIJob;
import com.top_logic.factory.git.GitRepository;
import com.top_logic.factory.git.RepositoryName;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Names and accessors of the model module {@link #MODULE} holding the projects of the control
 * application and their workspaces.
 */
public class FactoryModel {

	/**
	 * Name of the model module.
	 */
	public static final String MODULE = "tl.factory";

	/**
	 * Name of the type of a project.
	 */
	public static final String PROJECT = "Project";

	/**
	 * Name of the type of the workspace of a project.
	 */
	public static final String WORKSPACE = "Workspace";

	/**
	 * Name of the enumeration of the states of a workspace, see {@link WorkspaceStatus}.
	 */
	public static final String WORKSPACE_STATUS = "WorkspaceStatus";

	/** Attribute of a {@link #PROJECT}: its display name. */
	public static final String PROJECT_NAME = "name";

	/** Attribute of a {@link #PROJECT}: the Maven group of its application. */
	public static final String PROJECT_GROUP_ID = "groupId";

	/** Attribute of a {@link #PROJECT}: the Maven artifact of its application. */
	public static final String PROJECT_ARTIFACT_ID = "artifactId";

	/** Attribute of a {@link #PROJECT}: the TopLogic version its application is built on. */
	public static final String PROJECT_ENGINE_VERSION = "engineVersion";

	/**
	 * Attribute of a {@link #PROJECT}: the {@link RepositoryName#getFullName() full name} of its
	 * repository on the git hosting service.
	 */
	public static final String PROJECT_REPOSITORY = "repository";

	/**
	 * Attribute of a {@link #PROJECT}: the {@link GitRepository#getWebUrl() page} of its repository
	 * on the git hosting service.
	 */
	public static final String PROJECT_REPOSITORY_URL = "repositoryUrl";

	/**
	 * Attribute of a {@link #PROJECT}: the {@link CIJob#name() name} of its job on the CI server.
	 */
	public static final String PROJECT_CI_JOB = "ciJob";

	/**
	 * Attribute of a {@link #PROJECT}: the {@link CIJob#webUrl() page} of its job on the CI server.
	 */
	public static final String PROJECT_CI_JOB_URL = "ciJobUrl";

	/** Attribute of a {@link #PROJECT}: its {@link #WORKSPACE}, a composition. */
	public static final String PROJECT_WORKSPACE = "workspace";

	/** Attribute of a {@link #WORKSPACE}: the identifier of its container. */
	public static final String WORKSPACE_ID = "workspaceId";

	/** Attribute of a {@link #WORKSPACE}: its {@link #WORKSPACE_STATUS}. */
	public static final String WORKSPACE_STATE = "status";

	/** Attribute of a {@link #WORKSPACE}: the host port the application is published on. */
	public static final String WORKSPACE_HOST_PORT = "hostPort";

	/** Attribute of a {@link #WORKSPACE}: the URL a browser reaches the application with. */
	public static final String WORKSPACE_PREVIEW_URL = "previewUrl";

	/** Attribute of a {@link #WORKSPACE}: the initial password of the account root of its application. */
	public static final String WORKSPACE_INITIAL_PASSWORD = "initialPassword";

	/**
	 * Attribute of a {@link #WORKSPACE}: the image its container was created from, see
	 * {@link com.top_logic.factory.workspace.WorkspaceInfo#getImage()}.
	 */
	public static final String WORKSPACE_IMAGE = "image";

	/** Attribute of a {@link #WORKSPACE}: the output of its last operation. */
	public static final String WORKSPACE_LOG = "log";

	/**
	 * Attribute of a {@link #WORKSPACE}: its {@link #AGENT_CONVERSATION}s, a composition.
	 */
	public static final String WORKSPACE_CONVERSATIONS = "conversations";

	/**
	 * Name of the type of a conversation with the coding agent of a workspace.
	 */
	public static final String AGENT_CONVERSATION = "AgentConversation";

	/** Attribute of an {@link #AGENT_CONVERSATION}: the {@link #WORKSPACE} it belongs to. */
	public static final String CONVERSATION_WORKSPACE = "workspace";

	/** Attribute of an {@link #AGENT_CONVERSATION}: when it was started. */
	public static final String CONVERSATION_CREATED_AT = "createdAt";

	/**
	 * Name of the property of an {@link #AGENT_CONVERSATION} holding the task it was started with.
	 */
	public static final String CONVERSATION_TASK = "task";

	/**
	 * Attribute of an {@link #AGENT_CONVERSATION}: the ID under which the agent keeps the
	 * conversation.
	 */
	public static final String CONVERSATION_SESSION_ID = "sessionId";

	private FactoryModel() {
		// Constants and static accessors only.
	}

	/**
	 * The type of a project.
	 */
	public static TLClass projectType() {
		return (TLClass) TLModelUtil.findType(MODULE, PROJECT);
	}

	/**
	 * The type of the workspace of a project.
	 */
	public static TLClass workspaceType() {
		return (TLClass) TLModelUtil.findType(MODULE, WORKSPACE);
	}

	/**
	 * The type of a conversation with the coding agent of a workspace.
	 */
	public static TLClass conversationType() {
		return (TLClass) TLModelUtil.findType(MODULE, AGENT_CONVERSATION);
	}

	/**
	 * Whether the given object is an {@link #AGENT_CONVERSATION}.
	 */
	public static boolean isConversation(Object object) {
		return object instanceof TLObject conversation && conversation.tValid()
			&& conversation.tType() == conversationType();
	}

	/**
	 * Creates a conversation with the coding agent of the given workspace, started with the given
	 * task, in the current transaction.
	 */
	public static TLObject createConversation(TLObject workspace, String task) {
		TLObject conversation = ModelService.getInstance().getFactory().createObject(conversationType(), workspace);
		conversation.tUpdateByName(CONVERSATION_CREATED_AT, new Date());
		conversation.tUpdateByName(CONVERSATION_TASK, task);
		workspace.tAddByName(WORKSPACE_CONVERSATIONS, conversation);
		return conversation;
	}

	/**
	 * The workspace the given conversation belongs to.
	 */
	public static TLObject getConversationWorkspace(TLObject conversation) {
		return conversation.tContainer();
	}

	/**
	 * The ID under which the agent keeps the given conversation, {@code null} before its first
	 * answer.
	 */
	public static String getSessionId(TLObject conversation) {
		return getString(conversation, CONVERSATION_SESSION_ID);
	}

	/**
	 * All workspaces stored in the application.
	 */
	public static List<TLObject> allWorkspaces() {
		return new ArrayList<>(MetaElementUtil.getAllInstancesOf(workspaceType(), TLObject.class));
	}

	/**
	 * Creates a workspace for the given project in the current transaction.
	 *
	 * @param project
	 *        The project without workspace.
	 * @param workspaceId
	 *        The identifier of the container of the workspace.
	 * @param initialPassword
	 *        The initial password of the account root of the application in the workspace.
	 * @param status
	 *        The initial state.
	 * @return The new workspace.
	 */
	public static TLObject createWorkspace(TLObject project, String workspaceId, String initialPassword,
			WorkspaceStatus status) {
		TLObject workspace = ModelService.getInstance().getFactory().createObject(workspaceType(), project);
		workspace.tUpdateByName(WORKSPACE_ID, workspaceId);
		workspace.tUpdateByName(WORKSPACE_INITIAL_PASSWORD, initialPassword);
		setStatus(workspace, status);
		project.tUpdateByName(PROJECT_WORKSPACE, workspace);
		return workspace;
	}

	/**
	 * The stored workspace with the given identifier.
	 *
	 * @return The workspace, {@code null} if there is none.
	 */
	public static TLObject findWorkspace(String workspaceId) {
		for (TLObject workspace : allWorkspaces()) {
			if (workspaceId.equals(getWorkspaceId(workspace))) {
				return workspace;
			}
		}
		return null;
	}

	/**
	 * The workspace of the given project, {@code null} if it has none.
	 */
	public static TLObject getWorkspace(TLObject project) {
		return (TLObject) project.tValueByName(PROJECT_WORKSPACE);
	}

	/**
	 * The project the given workspace belongs to.
	 */
	public static TLObject getProject(TLObject workspace) {
		return workspace.tContainer();
	}

	/**
	 * The repository of the given project on the git hosting service.
	 *
	 * @return Its full name, {@code null} if the project has none.
	 */
	public static RepositoryName getRepository(TLObject project) {
		return RepositoryName.fromFullName(getString(project, PROJECT_REPOSITORY));
	}

	/**
	 * Sets the repository of the given project in the current transaction.
	 *
	 * @param repository
	 *        The repository on the git hosting service, {@code null} for none.
	 */
	public static void setRepository(TLObject project, GitRepository repository) {
		project.tUpdateByName(PROJECT_REPOSITORY, repository == null ? null : repository.getFullName());
		project.tUpdateByName(PROJECT_REPOSITORY_URL, repository == null ? null : repository.getWebUrl());
	}

	/**
	 * The {@link CIJob#name() name} of the job of the given project on the CI server.
	 *
	 * @return The name, {@code null} if the project has no job.
	 */
	public static String getCIJob(TLObject project) {
		return getString(project, PROJECT_CI_JOB);
	}

	/**
	 * Sets the CI job of the given project in the current transaction.
	 *
	 * @param job
	 *        The job on the CI server, {@code null} for none.
	 */
	public static void setCIJob(TLObject project, CIJob job) {
		project.tUpdateByName(PROJECT_CI_JOB, job == null ? null : job.name());
		project.tUpdateByName(PROJECT_CI_JOB_URL, job == null ? null : job.webUrl());
	}

	/**
	 * Reads a text attribute of a project or workspace.
	 */
	public static String getString(TLObject object, String attribute) {
		return (String) object.tValueByName(attribute);
	}

	/**
	 * The identifier of the container of the given workspace.
	 */
	public static String getWorkspaceId(TLObject workspace) {
		return getString(workspace, WORKSPACE_ID);
	}

	/**
	 * The stored state of the given workspace.
	 */
	public static WorkspaceStatus getStatus(TLObject workspace) {
		TLClassifier classifier = (TLClassifier) workspace.tValueByName(WORKSPACE_STATE);
		return classifier == null ? WorkspaceStatus.UNKNOWN : WorkspaceStatus.valueOf(classifier.getName());
	}

	/**
	 * Sets the stored state of the given workspace in the current transaction.
	 */
	public static void setStatus(TLObject workspace, WorkspaceStatus status) {
		workspace.tUpdateByName(WORKSPACE_STATE, classifier(status));
	}

	/**
	 * Sets where the application of the given workspace is reached in the current transaction.
	 *
	 * @param hostPort
	 *        The host port, {@code null} when the application is not published.
	 * @param previewUrl
	 *        The URL of the application, {@code null} when the application is not published.
	 */
	public static void setEndpoint(TLObject workspace, Integer hostPort, String previewUrl) {
		workspace.tUpdateByName(WORKSPACE_HOST_PORT, hostPort);
		workspace.tUpdateByName(WORKSPACE_PREVIEW_URL, previewUrl);
	}

	/**
	 * The classifier standing for the given state.
	 */
	public static TLClassifier classifier(WorkspaceStatus status) {
		TLEnumeration enumeration = (TLEnumeration) TLModelUtil.findType(MODULE, WORKSPACE_STATUS);
		TLClassifier result = enumeration.getClassifier(status.name());
		if (result == null) {
			throw new IllegalStateException("Missing classifier " + status + " in " + enumeration + ".");
		}
		return result;
	}

}
