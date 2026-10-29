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
import com.top_logic.factory.git.GitCapabilities;
import com.top_logic.factory.git.GitConnections;
import com.top_logic.factory.git.GitConnections.OwnConnection;
import com.top_logic.factory.git.GitHostingService;
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
 * TL-Script functions managing the workspaces of the projects of the control application, the
 * members of its tenants and their git connections, and telling the tenant and the projects of the
 * current user.
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
	 * Key of the dictionary of {@link #gitCapabilities(TLObject, TLObject)}: whether the access
	 * token may create repositories.
	 */
	public static final String GIT_CREATE_REPOSITORIES = "createRepositories";

	/**
	 * Key of the dictionary of {@link #gitCapabilities(TLObject, TLObject)}: whether the access
	 * token may protect the branches of the repositories.
	 */
	public static final String GIT_PROTECT_BRANCHES = "protectBranches";

	/**
	 * Key of the dictionary of {@link #gitCapabilities(TLObject, TLObject)}: the organization or
	 * user the repositories of the tenant are created in.
	 */
	public static final String GIT_OWNER = "owner";

	/**
	 * Key of the dictionary of {@link #gitCapabilities(TLObject, TLObject)}: why the rights could
	 * not be determined.
	 */
	public static final String GIT_PROBLEM = "problem";

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
	 * Whether a new project of the current user has a git connection to choose, on which its
	 * repository lives.
	 *
	 * @return Whether the tenant of the current user (for a user of no tenant: the installation)
	 *         offers a git connection.
	 */
	@Label("Git hosting enabled")
	@SideEffectFree
	public static boolean gitHosting() {
		return !GitConnections.choices(currentTenant()).isEmpty();
	}

	/**
	 * The names of the types of git hosting services a git connection may be of, e.g. "gitea".
	 *
	 * @return The registered types.
	 */
	@Label("Git provider types")
	@SideEffectFree
	public static List<String> gitProviderTypes() {
		GitHostingService service = GitHostingService.getInstanceOrNull();
		return service == null ? List.of() : service.getProviderTypeNames();
	}

	/**
	 * The git connections a new project of a tenant may live on: the built-in connections of the
	 * installation, then the connections of the tenant.
	 *
	 * @param tenant
	 *        The tenant; empty for the tenant of the current user.
	 * @return The connections, objects of <code>tl.factory:GitConnection</code>.
	 */
	@Label("Git connections of a new project")
	@SideEffectFree
	public static List<TLObject> gitConnections(TLObject tenant) {
		return GitConnections.choices(tenant == null ? currentTenant() : tenant);
	}

	/**
	 * The git connection a new project of the current user lives on unless another one is chosen.
	 *
	 * @return The first of the git connections of a new project, nothing if there is none.
	 */
	@Label("Default git connection")
	@SideEffectFree
	public static TLObject defaultGitConnection() {
		return GitConnections.defaultChoice(currentTenant());
	}

	/**
	 * The git connections a tenant created itself, with access tokens of its own.
	 *
	 * @param tenant
	 *        The tenant.
	 * @return The connections, by name.
	 */
	@Label("Git connections of a tenant")
	@SideEffectFree
	public static List<TLObject> ownGitConnections(TLObject tenant) {
		return tenant == null ? List.of() : GitConnections.ownConnections(tenant);
	}

	/**
	 * The organization or user the repositories of a tenant are created in on a git connection:
	 * the owner of a connection of a tenant, the organization of the tenant on a built-in
	 * connection.
	 *
	 * @param connection
	 *        The connection.
	 * @param tenant
	 *        The tenant; empty for the tenant of the current user.
	 * @return The owner; nothing on a built-in connection for a user of no tenant.
	 */
	@Label("Owner on a git connection")
	@SideEffectFree
	public static String gitOwner(@Mandatory TLObject connection, TLObject tenant) {
		return GitConnections.ownerFor(connection, tenant == null ? currentTenant() : tenant);
	}

	/**
	 * Whether a git connection is a built-in connection of the installation, offered to every
	 * tenant and changed only by its configuration.
	 *
	 * @param connection
	 *        The connection.
	 * @return Whether it is built in.
	 */
	@Label("Built-in git connection")
	@SideEffectFree
	public static boolean gitBuiltIn(@Mandatory TLObject connection) {
		return GitConnections.isBuiltIn(connection);
	}

	/**
	 * Checks the git connection of a new project: one of the git connections of a new project of
	 * its tenant, if there are any.
	 *
	 * @param project
	 *        The project being created.
	 * @param value
	 *        The chosen connection.
	 * @return The message describing what is wrong, nothing if the connection is fine.
	 */
	@Label("Check a git connection")
	@SideEffectFree
	public static ResKey checkGitConnection(TLObject project, TLObject value) {
		return GitConnections.checkProjectConnection(project, value);
	}

	/**
	 * What the access token of a git connection may do, asked from the git hosting service.
	 *
	 * @param connection
	 *        The connection.
	 * @param tenant
	 *        The tenant whose repositories are created; empty for the tenant of the current user.
	 *        On a built-in connection, every tenant has an organization of its own.
	 * @return A dictionary: whether the token may create repositories ("createRepositories") and
	 *         protect their branches ("protectBranches"), the organization or user the repositories
	 *         are created in ("owner"), and why the rights could not be determined ("problem", only
	 *         then).
	 */
	@Label("Rights of a git connection")
	@SideEffectFree
	public static Map<String, Object> gitCapabilities(@Mandatory TLObject connection, TLObject tenant) {
		TLObject owner = tenant == null ? currentTenant() : tenant;
		GitCapabilities capabilities = GitConnections.capabilities(connection, owner);
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(GIT_CREATE_REPOSITORIES, Boolean.valueOf(capabilities.createRepositories()));
		result.put(GIT_PROTECT_BRANCHES, Boolean.valueOf(capabilities.protectBranches()));
		result.put(GIT_OWNER, GitConnections.ownerFor(connection, owner));
		if (capabilities.problem() != null) {
			result.put(GIT_PROBLEM, capabilities.problem());
		}
		return result;
	}

	/**
	 * Creates a git connection of a tenant with its access token. Only a user who may change the
	 * tenant (an administrator of the tenant or an operator) may do so. The token is kept in the
	 * secret store of the installation, never in the model, and is not shown again.
	 *
	 * @param tenant
	 *        The tenant.
	 * @param name
	 *        The display name of the connection.
	 * @param providerType
	 *        The type of the git hosting service, one of the git provider types.
	 * @param url
	 *        The URL of the server.
	 * @param owner
	 *        The organization or user the repositories live in.
	 * @param token
	 *        The access token.
	 * @return The new connection.
	 */
	@Label("Create a git connection")
	public static TLObject createGitConnection(@Mandatory TLObject tenant, @Mandatory String name,
			@Mandatory String providerType, @Mandatory String url, @Mandatory String owner, @Mandatory String token) {
		return GitConnections.create(tenant, new OwnConnection(name, providerType, url, owner), token);
	}

	/**
	 * Changes a git connection of a tenant. Only a user who may change the tenant may do so; type,
	 * URL and owner stay as they are while projects live on the connection.
	 *
	 * @param connection
	 *        The connection of a tenant.
	 * @param name
	 *        The display name.
	 * @param providerType
	 *        The type of the git hosting service.
	 * @param url
	 *        The URL of the server.
	 * @param owner
	 *        The organization or user the repositories live in.
	 */
	@Label("Change a git connection")
	public static void updateGitConnection(@Mandatory TLObject connection, @Mandatory String name,
			@Mandatory String providerType, @Mandatory String url, @Mandatory String owner) {
		GitConnections.update(connection, new OwnConnection(name, providerType, url, owner));
	}

	/**
	 * Replaces the access token of a git connection of a tenant. Only a user who may change the
	 * tenant may do so.
	 *
	 * @param connection
	 *        The connection of a tenant.
	 * @param token
	 *        The new access token.
	 */
	@Label("Replace the access token of a git connection")
	public static void replaceGitToken(@Mandatory TLObject connection, @Mandatory String token) {
		GitConnections.replaceToken(connection, token);
	}

	/**
	 * Deletes a git connection of a tenant and its access token. Only a user who may change the
	 * tenant may do so, and only while no project lives on the connection.
	 *
	 * @param connection
	 *        The connection of a tenant.
	 */
	@Label("Delete a git connection")
	public static void deleteGitConnection(@Mandatory TLObject connection) {
		GitConnections.delete(connection);
	}

	/**
	 * Checks the existing repository entered for a new project on the git hosting service of its
	 * git connection; without one, that the access token of the connection may create the
	 * repository of the new project.
	 *
	 * @param project
	 *        The project the repository is entered for.
	 * @param value
	 *        The full name of the repository (owner/name), its clone URL or the URL of one of its
	 *        pages; empty for a repository the control application creates.
	 * @return The message describing what is wrong with the value; nothing if the value names a
	 *         repository the access token of the connection can read, if it is empty and the
	 *         access token may create repositories, if the project has recorded its repository
	 *         already, or if the project has no available git connection or the workspace service
	 *         is not running.
	 */
	@Label("Check a repository")
	@SideEffectFree
	public static ResKey checkRepository(TLObject project, String value) {
		if (!ProjectWorkspaces.Module.INSTANCE.isActive()) {
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
	 * Whether the application of a workspace is reached through the control application only
	 * after the workspace is updated: its container serves the application under a context path
	 * outside the preview route of the control application, the update creates it with the
	 * context path of the configured pattern.
	 *
	 * @param workspace
	 *        The workspace of a project.
	 * @return Whether the context path of the workspace is outdated.
	 */
	@Label("Workspace context path outdated")
	@SideEffectFree
	public static boolean contextPathOutdated(@Mandatory TLObject workspace) {
		return ProjectWorkspaces.isContextPathOutdated(workspace);
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
	 * Whether a CI server is configured, so that the CI of a project can be set up.
	 *
	 * @return Whether CI is switched on.
	 */
	@Label("CI enabled")
	@SideEffectFree
	public static boolean ciEnabled() {
		return ProjectWorkspaces.Module.INSTANCE.isActive() && ProjectWorkspaces.getInstance().isCIEnabled();
	}

	/**
	 * Why the CI of a project cannot be set up: CI is switched off, the project is not on a
	 * built-in git connection, has no repository, its repository has no application yet (no
	 * workspace has been created), or it has a CI job already.
	 *
	 * @param project
	 *        The project, empty for none.
	 * @return The reason, nothing if the CI can be set up.
	 */
	@Label("CI set up unavailable")
	@SideEffectFree
	public static ResKey ciSetUpUnavailable(TLObject project) {
		if (!ProjectWorkspaces.Module.INSTANCE.isActive()) {
			return com.top_logic.factory.ci.I18NConstants.ERROR_NO_CI;
		}
		return ProjectWorkspaces.getInstance().ciSetUpUnavailable(project);
	}

	/**
	 * Sets up the CI of a project: commits the default pipeline definition to the default branch
	 * of its repository unless the repository has one, extends the protection of the branch by the
	 * pipeline file, creates the CI job of the project and starts its first build.
	 *
	 * @param project
	 *        A project on a built-in git connection whose repository has the application and which
	 *        has no CI job.
	 * @return The message telling what was set up.
	 */
	@Label("Set up CI")
	public static ResKey setUpCI(@Mandatory TLObject project) {
		FactoryAccess.checkWrite(project);
		return ProjectWorkspaces.getInstance().setUpCI(project).message();
	}

	/**
	 * Why no CI build of a project can be started: CI is switched off, the project is not on a
	 * built-in git connection, has no repository or no CI job.
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
	 * created again if it no longer exists on the CI server, and set to build the repository of the
	 * project.
	 *
	 * @param project
	 *        A project with repository and CI job.
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
