/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

/**
 * How strictly security is enforced for an agent run.
 *
 * <p>
 * The effective mode of a run is resolved <b>once</b> from the deployment-wide policy, before the
 * first provider call (see {@link #resolve(ToolAccessPolicy)}). It is then carried on the run's
 * security context and never re-derived, so nothing can widen it mid-run.
 * </p>
 *
 * <p>
 * Informally {@link #ENFORCED} is the strictest and {@link #LEGACY} the laxest, but the modes are
 * not composed with each other anywhere: exactly one deployment-wide policy decides the mode, and
 * every question about it is asked through the predicates below rather than by comparing modes.
 * </p>
 *
 * @see ModelSecurity
 * @see RoleToolAccess
 */
public enum AiSecurityMode {

	/**
	 * Unenforced: no role-based tool authorization and no model access checks.
	 *
	 * <p>
	 * The <b>default</b> and the migration mode for applications that have not yet written a
	 * {@code <security-config>}; without one, the engine's deny-by-default model security would leave
	 * the agent unable to read anything. Removed once applications have migrated.
	 * </p>
	 *
	 * @see #resolve(ToolAccessPolicy)
	 */
	LEGACY,

	/**
	 * Audited bypass: the system, not a user, is the principal.
	 *
	 * <p>
	 * For trusted automation that deliberately has no user identity. Not "no security" - every use
	 * is audit-logged with the reason it was requested, so bypasses stay countable. This is the
	 * honest name for what {@code ToolContext.createUnrestricted()} does silently today.
	 * </p>
	 */
	SYSTEM,

	/**
	 * Full enforcement: an authenticated user is required, tool authorization and model access
	 * checks apply.
	 *
	 * <p>
	 * A run whose context has no {@code Person} is refused up front rather than at the first tool
	 * call: a run without a principal cannot be authorized, and failing later wastes a provider
	 * round trip and produces a confusing half-run.
	 * </p>
	 */
	ENFORCED;

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
		AiSecurityMode configured = deployment == null ? null : deployment.getSecurityMode();
		return configured == null ? LEGACY : configured;
	}
}
