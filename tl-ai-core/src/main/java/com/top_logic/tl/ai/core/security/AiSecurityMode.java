/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

/*
 * The JavaDoc of this enum and of its constants is exported to META-INF/messages_en.properties by the
 * build's doclet and shown in the service editor, where the security mode is chosen. It is therefore
 * written for an administrator; the design behind the modes is documented on resolve(ToolAccessPolicy)
 * and in section 3.1 of doc/security/agent-security-architecture.md, neither of which the doclet
 * exports.
 *
 * The effective mode of a run is resolved once, before the first provider call, as the stricter of
 * the deployment-wide ceiling and the agent definition's floor, then carried on the run's security
 * context and never re-derived - so nothing can widen it mid-run. The ordering is
 * ENFORCED > SYSTEM > LEGACY and stricter(AiSecurityMode, AiSecurityMode) is the only place it is
 * used; every other question about a mode is asked through the predicates below rather than by
 * comparing modes.
 */

/**
 * How strictly security is enforced when an agent runs.
 *
 * <p>
 * A single agent may name a <i>stricter</i> mode for itself than the application uses; it can never
 * name a laxer one.
 * </p>
 *
 * @see ModelSecurity
 * @see RoleToolAccess
 */
public enum AiSecurityMode {

	/**
	 * Nothing is checked: neither which tools the acting user may use nor which data they may see.
	 *
	 * <p>
	 * The state of an application that has configured no policy of its own.
	 * </p>
	 *
	 * @see #resolve(ToolAccessPolicy)
	 */
	/*
	 * The default and the migration mode: without a <security-config> the engine's deny-by-default
	 * model security would leave the agent unable to read anything. Removed once applications have
	 * migrated.
	 */
	LEGACY(0),

	/**
	 * The run acts as the application itself rather than as a user, and every such run is recorded in
	 * the security audit log.
	 *
	 * <p>
	 * For automation that deliberately has no user identity.
	 * </p>
	 */
	/*
	 * Not "no security": the audit entry names the reason the bypass was requested, so bypasses stay
	 * countable. This is the honest name for what ToolContext.createUnrestricted() does silently today.
	 */
	SYSTEM(1),

	/**
	 * Everything is checked: the run needs a logged-in user, and both the tools it may call and the
	 * data it may touch are checked against that user's roles.
	 */
	/*
	 * A run whose context has no Person is refused up front rather than at the first tool call: a run
	 * without a principal cannot be authorized, and failing later wastes a provider round trip and
	 * produces a confusing half-run.
	 */
	ENFORCED(2);

	/*
	 * Written out rather than derived from ordinal(): the declaration order is what an administrator
	 * sees in the mode selector, and a future reordering there must not silently redefine which mode is
	 * stricter than which.
	 */
	private final int _strictness;

	private AiSecurityMode(int strictness) {
		_strictness = strictness;
	}

	/**
	 * Whether this mode requires an authenticated user for the run to start.
	 */
	public boolean requiresUser() {
		return this == ENFORCED;
	}

	/**
	 * Whether role-based tool authorization ({@link RoleToolAccess}) applies in this mode.
	 */
	public boolean enforcesToolAccess() {
		return this == ENFORCED;
	}

	/**
	 * Whether per-object model access checks ({@link ModelSecurity}) apply in this mode.
	 */
	public boolean enforcesModelAccess() {
		return this == ENFORCED;
	}

	/**
	 * Whether a run in this mode must be recorded in the security audit log purely for existing.
	 *
	 * @see #SYSTEM
	 */
	public boolean requiresBypassAudit() {
		return this == SYSTEM;
	}

	/**
	 * The effective mode of a run under the given deployment policy.
	 *
	 * <p>
	 * <b>An absent policy means {@link #LEGACY}</b>, not {@link #ENFORCED}. That is the one place
	 * this design does not fail closed, and deliberately so: the engine's model security is
	 * deny-by-default, so enforcing it in an application that has written no
	 * {@code <security-config>} would leave the agent unable to read anything at all. Enforcement is
	 * therefore opt-in - an administrator turns it on by configuring a policy - and an application
	 * that has not migrated keeps behaving exactly as it did before these modes existed. The absence
	 * of a policy is not an unknown that has to fail closed; it is a positive statement that the
	 * application has not been migrated.
	 * </p>
	 *
	 * <p>
	 * Inside an enforced deployment the individual decisions do fail closed: an ungrouped tool is
	 * denied to every role, and an unattended run without an explicit
	 * {@code <background-allowlist>} is a configuration error rather than an unrestricted run.
	 * </p>
	 *
	 * @param deployment
	 *        The deployment-wide policy, or <code>null</code> if the application configures none.
	 * @return The mode to carry on the run's {@code SecurityContext}, never <code>null</code>.
	 */
	public static AiSecurityMode resolve(ToolAccessPolicy deployment) {
		return resolve(deployment, null);
	}

	/**
	 * The effective mode of a run of an agent that declares the given floor, under the given
	 * deployment policy.
	 *
	 * @param deployment
	 *        The deployment-wide policy, or <code>null</code> if the application configures none.
	 *        See {@link #resolve(ToolAccessPolicy)} for why its absence means {@link #LEGACY}.
	 * @param agentFloor
	 *        The mode the agent definition names for itself, or <code>null</code> if it names none.
	 *        Can only make the run stricter.
	 * @return The mode to carry on the run's {@code SecurityContext}, never <code>null</code>.
	 */
	public static AiSecurityMode resolve(ToolAccessPolicy deployment, AiSecurityMode agentFloor) {
		AiSecurityMode configured = deployment == null ? null : deployment.getSecurityMode();
		return stricter(configured == null ? LEGACY : configured, agentFloor);
	}

	/**
	 * The stricter of the deployment-wide ceiling and an agent definition's floor.
	 *
	 * <p>
	 * The whole composition rule: the effective mode of a run is
	 * {@code stricter(deploymentCeiling, agentFloor)}, resolved once before the first provider call.
	 * Reading the per-agent value as a floor rather than as an override removes the escalation
	 * direction - editing an agent definition is itself a privileged action, and it must not be a way
	 * to loosen what the deployment decided. So an agent can demand {@link #ENFORCED} in a
	 * {@link #LEGACY} deployment, and an agent asking for {@link #LEGACY} in an {@link #ENFORCED}
	 * deployment gets nothing.
	 * </p>
	 *
	 * @param ceiling
	 *        The deployment-wide mode. Never <code>null</code> - both
	 *        {@link #resolve(ToolAccessPolicy)} and {@code AiToolAccessService.ceiling()} answer
	 *        {@link #LEGACY} rather than nothing for an application that configured no policy.
	 * @param floor
	 *        The agent definition's mode, or <code>null</code> if it names none.
	 * @return The stricter of the two, never <code>null</code>.
	 */
	public static AiSecurityMode stricter(AiSecurityMode ceiling, AiSecurityMode floor) {
		if (floor == null) {
			return ceiling;
		}
		return floor._strictness > ceiling._strictness ? floor : ceiling;
	}
}
