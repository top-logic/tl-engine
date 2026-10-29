/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.project;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.agent.AgentTasks;
import com.top_logic.factory.workspace.WorkspaceService;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;

/**
 * TL-Script functions managing the workspaces of the projects of the control application.
 *
 * <p>
 * The functions starting an operation on a workspace return as soon as the operation is started;
 * the operation runs in the background and stores its progress in the workspace (see
 * {@link ProjectWorkspaces}). They commit the state of the workspace themselves and are therefore
 * called outside of a transaction.
 * </p>
 *
 * @implNote Every public static method is a TL-Script function named by the {@link ScriptPrefix}
 *           of this class followed by the capitalized method name, e.g. {@link #start(TLObject)} is
 *           called <code>factoryStart</code>.
 */
@ScriptPrefix(FactoryFunctions.PREFIX)
public class FactoryFunctions extends TLScriptFunctions {

	/** Prefix of the names of the functions of this class. */
	public static final String PREFIX = "factory";

	/**
	 * The TopLogic version a new project builds its application on, unless it chooses another one.
	 *
	 * @return The configured engine version.
	 */
	@Label("Default engine version")
	@SideEffectFree
	public static String engineVersion() {
		return WorkspaceService.getInstance().getApplications().getConfig().getEngineVersion();
	}

	/**
	 * Checks a Maven group ID for an application.
	 *
	 * @param value
	 *        The group ID.
	 * @return The message describing what is wrong with the value, nothing if it is valid.
	 */
	@Label("Check a group ID")
	@SideEffectFree
	public static ResKey checkGroupId(String value) {
		return ProjectWorkspaces.checkGroupId(value);
	}

	/**
	 * Checks a Maven artifact ID for an application.
	 *
	 * <p>
	 * The artifact ID is also the context path the application is served under.
	 * </p>
	 *
	 * @param value
	 *        The artifact ID.
	 * @return The message describing what is wrong with the value, nothing if it is valid.
	 */
	@Label("Check an artifact ID")
	@SideEffectFree
	public static ResKey checkArtifactId(String value) {
		return ProjectWorkspaces.checkArtifactId(value);
	}

	/**
	 * Creates the workspace of a project: a container in which an empty application is generated,
	 * built and started.
	 *
	 * @param project
	 *        A project without workspace.
	 * @return The workspace, in state "building" until the application answers.
	 */
	@Label("Create the workspace of a project")
	public static TLObject createWorkspace(@Mandatory TLObject project) {
		return ProjectWorkspaces.getInstance().create(project);
	}

	/**
	 * Starts the workspace of a project and with it its application.
	 *
	 * @param project
	 *        A project whose workspace is stopped.
	 */
	@Label("Start the workspace of a project")
	public static void start(@Mandatory TLObject project) {
		ProjectWorkspaces.getInstance().start(project);
	}

	/**
	 * Stops the workspace of a project and with it its application. The sources, the Maven
	 * repository and the database of the workspace are kept.
	 *
	 * @param project
	 *        A project whose workspace is running.
	 */
	@Label("Stop the workspace of a project")
	public static void stop(@Mandatory TLObject project) {
		ProjectWorkspaces.getInstance().stop(project);
	}

	/**
	 * Creates the container of the workspace of a project anew on the current workspace image. The
	 * sources, the Maven repository, the database and the initial password of the workspace are
	 * kept; a running workspace is started again.
	 *
	 * @param project
	 *        A project whose workspace is not busy.
	 */
	@Label("Update the workspace of a project")
	public static void update(@Mandatory TLObject project) {
		ProjectWorkspaces.getInstance().update(project);
	}

	/**
	 * Whether the container of a workspace runs another image than the current workspace image,
	 * so that it misses changes of the tools in the image until it is updated.
	 *
	 * @param workspace
	 *        The workspace of a project.
	 * @return Whether the workspace is outdated.
	 */
	@Label("Workspace image outdated")
	@SideEffectFree
	public static boolean imageOutdated(@Mandatory TLObject workspace) {
		return ProjectWorkspaces.isImageOutdated(workspace);
	}

	/**
	 * Why the coding agent cannot work in a workspace now: the workspace is not running, runs an
	 * outdated image without the agent tools, or the control application gives workspaces no access
	 * to a language model.
	 *
	 * @param workspace
	 *        The workspace of a project, empty for a project without one.
	 * @return The reason, nothing if the agent can work in the workspace.
	 */
	@Label("Agent unavailable")
	@SideEffectFree
	public static ResKey agentUnavailable(TLObject workspace) {
		return AgentTasks.unavailable(workspace);
	}

	/**
	 * Creates a conversation with the coding agent of a workspace for a task. The conversation is
	 * the subject of the chat that sends the task to the agent as its first message, see
	 * <code>chatStart</code>.
	 *
	 * @param workspace
	 *        The workspace whose agent is to work on the task; it must be running on the current
	 *        image.
	 * @param task
	 *        What the agent is to do.
	 * @return The new conversation, a <code>tl.factory:AgentConversation</code>.
	 */
	@Label("Create an agent conversation")
	public static TLObject createConversation(@Mandatory TLObject workspace, @Mandatory String task) {
		return AgentTasks.createConversation(workspace, task);
	}

	/**
	 * Removes the workspace of a project with its container and all its data. The project is
	 * kept and may create a new workspace afterwards.
	 *
	 * @param project
	 *        A project whose workspace is not busy.
	 */
	@Label("Remove the workspace of a project")
	public static void cleanup(@Mandatory TLObject project) {
		ProjectWorkspaces.getInstance().cleanup(project);
	}

}
