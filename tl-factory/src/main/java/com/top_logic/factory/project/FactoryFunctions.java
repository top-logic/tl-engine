/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.project;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.agent.AgentTasks;
import com.top_logic.factory.ci.CIBuild;
import com.top_logic.factory.ci.CIBuildStart;
import com.top_logic.factory.ci.ProjectCI;
import com.top_logic.factory.tenant.TenantMembers;
import com.top_logic.factory.tenant.TenantMembers.NewAccount;
import com.top_logic.factory.tenant.TenantRole;
import com.top_logic.factory.tenant.Tenants;
import com.top_logic.factory.workspace.WorkspaceService;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script functions managing the workspaces of the projects of the control application and the
 * members of its tenants, and telling the tenant and the projects of the current user.
 *
 * <p>
 * The functions changing a project, its workspace or its CI job require the right to write the
 * project (or the workspace, which is decided by its project).
 * </p>
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

	/** Key of the dictionary of {@link #lastBuild(TLObject)}: the number of the build. */
	public static final String BUILD_NUMBER = "number";

	/** Key of the dictionary of {@link #lastBuild(TLObject)}: the state as its name, e.g. SUCCESS. */
	public static final String BUILD_STATE = "state";

	/** Key of the dictionary of {@link #lastBuild(TLObject)}: the state for a user. */
	public static final String BUILD_STATE_LABEL = "stateLabel";

	/** Key of the dictionary of {@link #lastBuild(TLObject)}: the page of the build. */
	public static final String BUILD_URL = "url";

	/** Key of the dictionary of {@link #lastBuild(TLObject)}: when the build started. */
	public static final String BUILD_START_TIME = "startTime";

	/** Key of the dictionary of {@link #lastBuild(TLObject)}: the duration in seconds. */
	public static final String BUILD_DURATION = "durationSeconds";

	/** Key of the dictionary of {@link #lastBuild(TLObject)}: why the CI server cannot be asked. */
	public static final String BUILD_ERROR = "error";

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
	 * The tenant of the current user.
	 *
	 * @return The tenant the current user is a member of, nothing for a user of no tenant, e.g. an
	 *         operator.
	 */
	@Label("Tenant of the current user")
	@SideEffectFree
	public static TLObject currentTenant() {
		return Tenants.tenantOf(TLContext.currentUser());
	}

	/**
	 * The projects the current user works with: all projects for a user who may read every project
	 * (an operator), the projects of the tenant of the current user otherwise, none for a user of no
	 * tenant.
	 *
	 * @return The projects.
	 */
	@Label("Projects of the current user")
	@SideEffectFree
	public static List<TLObject> projects() {
		return Tenants.projects(TLContext.currentUser());
	}

	/**
	 * Checks the members of a tenant in one of its roles: a user is a member of one tenant only.
	 *
	 * @param tenant
	 *        The tenant.
	 * @param members
	 *        The users to become members of the tenant.
	 * @return The message naming the user who is a member of another tenant and that tenant,
	 *         nothing if there is none.
	 */
	@Label("Check the members of a tenant")
	@SideEffectFree
	public static ResKey checkTenantMembers(TLObject tenant, Object members) {
		return Tenants.checkMembers(tenant, members);
	}

	/**
	 * Creates a tenant with the account of its first administrator. Only a user who may create
	 * tenants (an operator) may do so.
	 *
	 * @param name
	 *        The name of the tenant, unique among the tenants.
	 * @param login
	 *        The login name of the administrator.
	 * @param firstName
	 *        The first name of the administrator.
	 * @param lastName
	 *        The last name of the administrator.
	 * @param email
	 *        The e-mail address of the administrator.
	 * @param password
	 *        The initial password of the administrator.
	 * @return The new tenant.
	 */
	@Label("Create a tenant")
	public static TLObject createTenant(@Mandatory String name, @Mandatory String login, String firstName,
			String lastName, String email, String password) {
		return TenantMembers.createTenant(name, new NewAccount(login, firstName, lastName, email, password));
	}

	/**
	 * Creates a member of a tenant with a new account. Only a user who may change the tenant (an
	 * administrator of the tenant or an operator) may do so.
	 *
	 * @param tenant
	 *        The tenant.
	 * @param role
	 *        The role of the member, a classifier of <code>tl.factory:TenantRole</code>.
	 * @param login
	 *        The login name of the new account, used by no other account.
	 * @param firstName
	 *        The first name of the member.
	 * @param lastName
	 *        The last name of the member.
	 * @param email
	 *        The e-mail address of the member.
	 * @param password
	 *        The initial password of the member.
	 * @return The account of the new member.
	 */
	@Label("Create a member of a tenant")
	public static Person createMember(@Mandatory TLObject tenant, @Mandatory TLClassifier role,
			@Mandatory String login, String firstName, String lastName, String email, String password) {
		return TenantMembers.createMember(tenant, TenantRole.fromClassifier(role),
			new NewAccount(login, firstName, lastName, email, password));
	}

	/**
	 * Changes the role of a member of a tenant. Only a user who may change the tenant may do so; the
	 * last administrator of a tenant keeps the role.
	 *
	 * @param tenant
	 *        The tenant.
	 * @param member
	 *        The account of a member of the tenant.
	 * @param role
	 *        The new role of the member, a classifier of <code>tl.factory:TenantRole</code>.
	 */
	@Label("Change the role of a member")
	public static void changeMemberRole(@Mandatory TLObject tenant, @Mandatory Person member,
			@Mandatory TLClassifier role) {
		TenantMembers.changeRole(tenant, member, TenantRole.fromClassifier(role));
	}

	/**
	 * Removes a member from a tenant and deletes the account of the member. Only a user who may
	 * change the tenant may do so; nobody removes himself or the last administrator.
	 *
	 * @param tenant
	 *        The tenant.
	 * @param member
	 *        The account of a member of the tenant.
	 */
	@Label("Remove a member")
	public static void removeMember(@Mandatory TLObject tenant, @Mandatory Person member) {
		TenantMembers.removeMember(tenant, member);
	}

	/**
	 * The members of a tenant in any of its roles.
	 *
	 * @param tenant
	 *        The tenant, empty for none.
	 * @return The accounts of the members, none without tenant.
	 */
	@Label("Members of a tenant")
	@SideEffectFree
	public static List<Person> members(TLObject tenant) {
		return tenant == null ? List.of() : TenantMembers.members(tenant);
	}

	/**
	 * The role of a member in a tenant.
	 *
	 * @param tenant
	 *        The tenant.
	 * @param member
	 *        The account of a member.
	 * @return The role, a classifier of <code>tl.factory:TenantRole</code>; nothing if the account
	 *         is no member of the tenant.
	 */
	@Label("Role of a member")
	@SideEffectFree
	public static TLClassifier memberRole(TLObject tenant, Person member) {
		if (tenant == null || member == null) {
			return null;
		}
		TenantRole role = TenantMembers.roleOf(tenant, member);
		return role == null ? null : role.classifier();
	}

	/**
	 * The e-mail address of an account.
	 *
	 * @param account
	 *        The account.
	 * @return The e-mail address, nothing if the account has none.
	 */
	@Label("E-mail of an account")
	@SideEffectFree
	public static String email(Person account) {
		return account == null ? null : TenantMembers.email(account);
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
	 * Whether the projects have repositories on a git hosting service.
	 *
	 * @return Whether git hosting is switched on.
	 */
	@Label("Git hosting enabled")
	@SideEffectFree
	public static boolean gitHosting() {
		return ProjectWorkspaces.getInstance().isGitHostingEnabled();
	}

	/**
	 * Checks the existing repository entered for a new project on the git hosting service.
	 *
	 * @param project
	 *        The project the repository is entered for.
	 * @param value
	 *        The full name of the repository (owner/name), its clone URL or the URL of one of its
	 *        pages; empty for a repository the control application creates.
	 * @return The message describing what is wrong with the value; nothing if the value is empty
	 *         or names a repository the control application can read, if the project has recorded
	 *         its repository already, or if git hosting is switched off or the workspace service is
	 *         not running.
	 */
	@Label("Check a repository")
	@SideEffectFree
	public static ResKey checkRepository(TLObject project, String value) {
		if (value == null || value.isBlank() || !ProjectWorkspaces.Module.INSTANCE.isActive()) {
			return null;
		}
		return ProjectWorkspaces.getInstance().checkRepository(project, value);
	}

	/**
	 * Creates the workspace of a project: a container in which the application is generated - or,
	 * with git hosting, cloned from the repository of the project -, built and started.
	 *
	 * @param project
	 *        A project without workspace.
	 * @return The workspace, in state "building" until the application answers.
	 */
	@Label("Create the workspace of a project")
	public static TLObject createWorkspace(@Mandatory TLObject project) {
		FactoryAccess.checkWrite(project);
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
		FactoryAccess.checkWrite(project);
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
		FactoryAccess.checkWrite(project);
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
		FactoryAccess.checkWrite(project);
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
		FactoryAccess.checkWrite(workspace);
		return AgentTasks.createConversation(workspace, task);
	}

	/**
	 * Why no CI build of a project can be started: CI or git hosting is switched off, or the
	 * project has no repository.
	 *
	 * @param project
	 *        The project, empty for none.
	 * @return The reason, nothing if a build can be started.
	 */
	@Label("CI build unavailable")
	@SideEffectFree
	public static ResKey ciUnavailable(TLObject project) {
		if (!ProjectWorkspaces.Module.INSTANCE.isActive()) {
			return com.top_logic.factory.ci.I18NConstants.ERROR_NO_CI;
		}
		return ProjectWorkspaces.getInstance().ciUnavailable(project);
	}

	/**
	 * Starts a build of the application of a project on the CI server. The CI job of the project is
	 * created first if it does not exist, and set to build the repository of the project.
	 *
	 * @param project
	 *        A project with repository.
	 * @return The message telling which build was started.
	 */
	@Label("Start a CI build")
	public static ResKey startBuild(@Mandatory TLObject project) {
		FactoryAccess.checkWrite(project);
		CIBuildStart build = ProjectWorkspaces.getInstance().startBuild(project);
		return ProjectCI.startedMessage(build, FactoryModel.getCIJob(project));
	}

	/**
	 * The last build of the CI job of a project, asked from the CI server.
	 *
	 * @param project
	 *        The project.
	 * @return A dictionary with the number (key "number"), the state ("state", e.g. "SUCCESS", and
	 *         "stateLabel" for display), the page ("url"), the start time ("startTime") and the
	 *         duration in seconds ("durationSeconds") of the build; if the CI server cannot be
	 *         asked, a dictionary with the reason only ("error"); nothing if CI is switched off,
	 *         the project has no CI job, or the job has no build.
	 */
	@Label("Last CI build")
	@SideEffectFree
	public static Map<String, Object> lastBuild(TLObject project) {
		if (project == null || !ProjectWorkspaces.Module.INSTANCE.isActive()) {
			return null;
		}
		CIBuild build;
		try {
			build = ProjectWorkspaces.getInstance().lastBuild(project);
		} catch (TopLogicException ex) {
			Logger.info("Cannot read the last CI build of " + project + ": " + ex.getMessage(), FactoryFunctions.class);
			return Map.of(BUILD_ERROR, ex.getErrorKey());
		}
		if (build == null) {
			return null;
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(BUILD_NUMBER, Integer.valueOf(build.number()));
		result.put(BUILD_STATE, build.state().name());
		result.put(BUILD_STATE_LABEL, build.state().label());
		result.put(BUILD_URL, build.webUrl());
		result.put(BUILD_START_TIME, Date.from(build.startTime()));
		result.put(BUILD_DURATION, Long.valueOf(build.duration().toSeconds()));
		return result;
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
		FactoryAccess.checkWrite(project);
		ProjectWorkspaces.getInstance().cleanup(project);
	}

}
