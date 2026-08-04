/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Set;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.basic.core.workspace.Environment;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.security.AiSecurityMode;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.ModelAccessRightsSecurity;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy;
import com.top_logic.tl.ai.core.security.ToolAuthorization;
import com.top_logic.tl.ai.tool.api.Tool;

/**
 * {@link SecurityContext} implementation backed by TopLogic's runtime.
 *
 * <p>Maps TopLogic's {@link MaintenanceWindowManager} and {@link Environment} to
 * {@link ExecutionMode}. User-level security ({@link #hasToolPermission}) is answered against the
 * roles the run's principal holds, but only where the run's {@link #securityMode() mode} enforces
 * tool access - which no application gets until it configures a {@code <tool-access>} policy.</p>
 *
 * <p>The execution mode is resolved dynamically on each call (not cached),
 * because maintenance mode can change at runtime. The {@link #securityMode() security mode}, the
 * {@link #modelSecurity() model access checks} and the tool authorization are the opposite: they are
 * bound once, when the context for a run is created, so nothing can widen them mid-run.</p>
 *
 * @see #forRun(Person, boolean)
 */
public class TopLogicSecurityContext implements SecurityContext {

	private final AiSecurityMode _securityMode;

	private final ModelSecurity _modelSecurity;

	private final ToolAuthorization _toolAuthorization;

	/**
	 * Creates a {@link TopLogicSecurityContext} with agent security unenforced.
	 *
	 * <p>
	 * The behaviour-preserving default: {@link AiSecurityMode#LEGACY} with
	 * {@link ModelSecurity#UNCHECKED} and {@link ToolAuthorization#UNCHECKED}. Use
	 * {@link #forRun(Person, boolean)} to honour an application's configured policy.
	 * </p>
	 */
	public TopLogicSecurityContext() {
		this(AiSecurityMode.LEGACY, ModelSecurity.UNCHECKED, ToolAuthorization.UNCHECKED);
	}

	private TopLogicSecurityContext(AiSecurityMode securityMode, ModelSecurity modelSecurity,
			ToolAuthorization toolAuthorization) {
		_securityMode = securityMode;
		_modelSecurity = modelSecurity;
		_toolAuthorization = toolAuthorization;
	}

	/**
	 * Creates the {@link SecurityContext} for one agent run under the deployment's configured policy.
	 *
	 * <p>
	 * The single entry point every caller that has an acting user should use. An application that has
	 * configured no {@link AiToolAccessService} policy gets {@link AiSecurityMode#LEGACY} and therefore
	 * exactly its previous behaviour: no tool authorization, no model checks.
	 * </p>
	 *
	 * @param principal
	 *        The authenticated user the run acts as, or <code>null</code> for a run with no user
	 *        identity.
	 * @param background
	 *        Whether the run has no human to answer approval prompts (a scheduler, an MCP client or
	 *        an {@code aiAgentChatAsync} run). Approval is the compensating control that makes
	 *        write-classified tools safe, so a run that has lost it is additionally bounded by the
	 *        policy's background allowlist.
	 * @throws IllegalStateException
	 *         If the resolved mode {@link AiSecurityMode#requiresUser() requires a user} and
	 *         {@code principal} is <code>null</code>.
	 */
	public static TopLogicSecurityContext forRun(Person principal, boolean background) {
		return build(AiToolAccessService.ceiling(), principal, background);
	}

	/**
	 * Creates the {@link SecurityContext} for one agent run, resolving its effective security mode
	 * against an explicitly supplied policy.
	 *
	 * <p>
	 * <b>A test seam.</b> It is the only way to exercise a policy without booting
	 * {@link AiToolAccessService}, which is why it survives; production code has no policy that is
	 * not the running service's and uses {@link #forRun(Person, boolean)}.
	 * </p>
	 *
	 * @param deployment
	 *        The deployment-wide policy, or <code>null</code> if the application configures none.
	 * @param principal
	 *        The authenticated user the run acts as, or <code>null</code> for a run with no user
	 *        identity.
	 * @throws IllegalStateException
	 *         If the resolved mode {@link AiSecurityMode#requiresUser() requires a user} and
	 *         {@code principal} is <code>null</code>. Refusing here rather than at the first tool
	 *         call is deliberate: a run without a principal cannot be authorized, and failing later
	 *         wastes a provider round trip and produces a confusing half-run.
	 */
	public static TopLogicSecurityContext forRun(ToolAccessPolicy deployment, Person principal) {
		return build(AiSecurityMode.resolve(deployment), principal, false);
	}

	/**
	 * The context of an {@link AiSecurityMode#ENFORCED} run whose tool authorization is already
	 * resolved.
	 *
	 * <p>
	 * <b>A test seam.</b> Production code resolves the authorization from the acting {@link Person}
	 * through {@link #forRun(Person, boolean)}, which needs a {@code KnowledgeBase} to read that
	 * person's roles. A unit test about how an enforced context <em>behaves</em> has a policy but no
	 * {@code Person}, and {@link AiSecurityMode#ENFORCED} refuses a run without one. Package-visible
	 * so it cannot quietly become a second production entry point.
	 * </p>
	 *
	 * @param authorization
	 *        The resolved tool authorization of the run.
	 */
	static TopLogicSecurityContext enforcedRun(ToolAuthorization authorization) {
		return new TopLogicSecurityContext(AiSecurityMode.ENFORCED, ModelSecurity.UNCHECKED, authorization);
	}

	private static TopLogicSecurityContext build(AiSecurityMode mode, Person principal, boolean background) {
		if (mode.requiresUser() && principal == null) {
			throw new IllegalStateException("Agent security mode " + mode
				+ " requires an authenticated user, but this run has none.");
		}
		ModelSecurity modelSecurity = mode.enforcesModelAccess()
			? new ModelAccessRightsSecurity(principal)
			: ModelSecurity.UNCHECKED;
		ToolAuthorization toolAuthorization = mode.enforcesToolAccess()
			? AiToolAccessService.authorizationFor(principal, background)
			: ToolAuthorization.UNCHECKED;
		return new TopLogicSecurityContext(mode, modelSecurity, toolAuthorization);
	}

	@Override
	public boolean hasToolPermission(Tool tool) {
		return _toolAuthorization.allows(tool);
	}

	@Override
	public boolean hasToolPermission(String toolName) {
		return _toolAuthorization.allows(toolName);
	}

	@Override
	public String toolDenialReason(Tool tool) {
		return _toolAuthorization.denialReason(tool);
	}

	@Override
	public String toolDenialReason(String toolName) {
		return _toolAuthorization.denialReason(toolName);
	}

	@Override
	public String capabilityDenialReason(String capability, String operation) {
		return _toolAuthorization.capabilityDenialReason(capability, operation);
	}

	@Override
	public String toolCapability(Tool tool) {
		return _toolAuthorization.capabilityOf(tool);
	}

	@Override
	public boolean isBackgroundRun() {
		return _toolAuthorization.isBackground();
	}

	@Override
	public Set<String> capabilitiesWithheldInBackground() {
		return _toolAuthorization.capabilitiesWithheldInBackground();
	}

	/**
	 * The same context with the tool authorization additionally bounded by the background allowlist.
	 *
	 * <p>
	 * The mode and the model-access checks are carried over untouched: they are about <em>who</em> the
	 * run acts as, which losing the approval prompt does not change. Only the tool ceiling does.
	 * </p>
	 */
	@Override
	public SecurityContext withBackgroundCeiling() {
		ToolAuthorization bounded = _toolAuthorization.asBackground();
		if (bounded == _toolAuthorization) {
			return this;
		}
		return new TopLogicSecurityContext(_securityMode, _modelSecurity, bounded);
	}

	@Override
	public AiSecurityMode securityMode() {
		return _securityMode;
	}

	@Override
	public ModelSecurity modelSecurity() {
		return _modelSecurity;
	}

	/** */
	@Override
	public ExecutionMode executionMode() {
		MaintenanceWindowManager mgr = MaintenanceWindowManager.getInstance();
		if (mgr != null
				&& mgr.getMaintenanceModeState() == MaintenanceWindowManager.IN_MAINTENANCE_MODE) {
			return ExecutionMode.MAINTENANCE;
		}
		if (!Environment.isDeployed()) {
			return ExecutionMode.DEVELOPMENT;
		}
		return ExecutionMode.PRODUCTION;
	}
}
