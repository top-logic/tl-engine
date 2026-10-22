/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.project;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.element.meta.MetaElementUtil;
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

	/** Attribute of a {@link #WORKSPACE}: the output of its last operation. */
	public static final String WORKSPACE_LOG = "log";

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
