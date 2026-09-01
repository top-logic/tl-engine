/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Objects;
import java.util.Set;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.base.operation.OperationMode;
import com.top_logic.base.operation.OperationModeService;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.ModelAccessRightsSecurity;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.RoleToolAccess;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy;
import com.top_logic.tl.ai.core.security.ToolAuthorization;
import com.top_logic.tl.ai.tool.api.Tool;

/**
 * {@link SecurityContext} implementation backed by TopLogic's runtime.
 *
 * <p>Maps TopLogic's {@link MaintenanceWindowManager} and {@link OperationModeService} to
 * {@link ExecutionMode}. User-level security ({@link #hasToolPermission}) is answered against the
 * roles the run's principal holds, and per-object access ({@link #modelSecurity()}) against that
 * principal's model access rights. Both are unconditional: there is no configuration that switches
 * them off, and a run that cannot name a principal is refused where its context is built.</p>
 *
 * <p>The execution mode is resolved dynamically on each call (not cached),
 * because maintenance mode can change at runtime. The {@link #modelSecurity() model access checks}
 * and the tool authorization are the opposite: they are bound once, when the context for a run is
 * created, so nothing can widen them mid-run.</p>
 *
 * @see #forRun(Person, boolean)
 */
public class TopLogicSecurityContext implements SecurityContext {

	private final ModelSecurity _modelSecurity;

	private final ToolAuthorization _toolAuthorization;

	private TopLogicSecurityContext(ModelSecurity modelSecurity, ToolAuthorization toolAuthorization) {
		_modelSecurity = Objects.requireNonNull(modelSecurity, "Model security must not be null.");
		_toolAuthorization =
			Objects.requireNonNull(toolAuthorization, "Tool authorization must not be null.");
	}

	/**
	 * Creates the {@link SecurityContext} for one agent run.
	 *
	 * <p>
	 * The single production entry point. What the run may do is decided entirely by who it acts as:
	 * the principal's roles on the security root at the tool layer, the principal's model access
	 * rights per object at the data layer.
	 * </p>
	 *
	 * <p>
	 * An unattended entry point that has no interactive user - a scheduler, say - must supply a
	 * configured service account here. There is no mode that lets it run without one.
	 * </p>
	 *
	 * @param principal
	 *        The authenticated user the run acts as. Never <code>null</code>.
	 * @param background
	 *        Whether the run has no human to answer approval prompts (a scheduler, an MCP client or
	 *        an {@code aiAgentChatAsync} run). Approval is the compensating control that makes
	 *        write-classified tools safe, so a run that has lost it is additionally bounded by the
	 *        policy's background allowlist.
	 * @throws IllegalStateException
	 *         If {@code principal} is <code>null</code>. Refusing here rather than at the first tool
	 *         call is deliberate: a run without a principal cannot be authorized, and failing later
	 *         wastes a provider round trip and produces a confusing half-run.
	 */
	public static TopLogicSecurityContext forRun(Person principal, boolean background) {
		if (principal == null) {
			throw new IllegalStateException("An agent run acts as an authenticated user, and this run"
				+ " has none. Start it from an entry point that resolves the acting account (the chat"
				+ " UI, or a TL-Script call, both of which act as the logged-in account), or - for an"
				+ " unattended run - as a configured service account.");
		}
		return new TopLogicSecurityContext(new ModelAccessRightsSecurity(principal),
			AiToolAccessService.authorizationFor(principal, background));
	}

	/**
	 * Creates the {@link SecurityContext} of a run that authorizes tools against an explicitly
	 * supplied policy.
	 *
	 * <p>
	 * <b>A test seam.</b> It is the only way to exercise a policy without booting
	 * {@link AiToolAccessService}, and it pairs that policy with {@link ModelSecurity#UNCHECKED}
	 * because the engine's model rights need a booted {@code SecurityConfigurationService} too.
	 * Production code has no policy that is not the running service's and uses
	 * {@link #forRun(Person, boolean)}. Package-visible so it cannot quietly become a second
	 * production entry point.
	 * </p>
	 *
	 * @param deployment
	 *        The policy to authorize against. Never <code>null</code>.
	 * @param principal
	 *        The user the run acts as, or <code>null</code> for a test that has no {@code Person} -
	 *        who then holds no role and reaches only the policy's baseline.
	 * @param heldRoleNames
	 *        The roles {@code principal} is to be treated as holding on the security root.
	 */
	static TopLogicSecurityContext forRun(ToolAccessPolicy deployment, Person principal,
			Set<String> heldRoleNames) {
		RoleToolAccess access = new RoleToolAccess(deployment, name -> null);
		return new TopLogicSecurityContext(ModelSecurity.UNCHECKED,
			new ToolAuthorization(access, principal, heldRoleNames, false));
	}

	/**
	 * The context of a run whose tool authorization is already resolved.
	 *
	 * <p>
	 * <b>A test seam.</b> Production code resolves the authorization from the acting {@link Person}
	 * through {@link #forRun(Person, boolean)}, which needs a {@code KnowledgeBase} to read that
	 * person's roles and a booted {@code SecurityConfigurationService} for the model rights. A unit
	 * test about how a context <em>behaves</em> has an authorization but neither of those.
	 * Package-visible so it cannot quietly become a second production entry point.
	 * </p>
	 *
	 * @param authorization
	 *        The resolved tool authorization of the run.
	 */
	static TopLogicSecurityContext enforcedRun(ToolAuthorization authorization) {
		return new TopLogicSecurityContext(ModelSecurity.UNCHECKED, authorization);
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

	@Override
	public boolean withheldByBackgroundCeiling(Tool tool) {
		return _toolAuthorization.withheldByBackgroundCeiling(tool);
	}

	/**
	 * The same context with the tool authorization additionally bounded by the background allowlist.
	 *
	 * <p>
	 * The model-access checks are carried over untouched: they are about <em>who</em> the run acts as,
	 * which losing the approval prompt does not change. Only the tool ceiling does.
	 * </p>
	 */
	@Override
	public SecurityContext withBackgroundCeiling() {
		ToolAuthorization bounded = _toolAuthorization.asBackground();
		if (bounded == _toolAuthorization) {
			return this;
		}
		return new TopLogicSecurityContext(_modelSecurity, bounded);
	}

	@Override
	public ModelSecurity modelSecurity() {
		return _modelSecurity;
	}

	/** */
	@Override
	public ExecutionMode executionMode() {
		return executionMode(MaintenanceWindowManager.isMaintenanceActive(), configuredOperationMode());
	}

	/**
	 * Folds the engine's two operational axes into the single {@link ExecutionMode} tl-ai gates tools
	 * with.
	 *
	 * <p>
	 * The engine keeps runtime maintenance and the boot-time environment apart - a production install
	 * can be in a maintenance window - and tl-ai does not, because the only question it asks of both
	 * is whether a {@code STRUCTURAL} tool may run. Maintenance therefore wins over the environment:
	 * an operator who opened a maintenance window did so to let exactly those tools run.
	 * </p>
	 *
	 * <p>
	 * {@link OperationMode#TEST} has no counterpart in tl-ai's vocabulary and is deliberately folded
	 * onto {@link ExecutionMode#DEVELOPMENT} rather than overlooked: a test install is not a live one,
	 * so the restriction that exists to protect live data does not apply to it.
	 * </p>
	 *
	 * <p>
	 * A {@code null} mode - the service is not started, see {@link #configuredOperationMode()} - and
	 * any mode the engine may add in the future both yield {@link ExecutionMode#PRODUCTION}. This path
	 * gates tools, so an unknown environment has to be the most restricted one; guessing the
	 * permissive mode would silently open {@code STRUCTURAL} tools on an install nobody classified.
	 * </p>
	 *
	 * @param maintenanceActive
	 *        Whether a maintenance window is currently open, from
	 *        {@link MaintenanceWindowManager#isMaintenanceActive()}.
	 * @param mode
	 *        The installation's environment, or <code>null</code> if it cannot be determined.
	 */
	static ExecutionMode executionMode(boolean maintenanceActive, OperationMode mode) {
		if (maintenanceActive) {
			return ExecutionMode.MAINTENANCE;
		}
		if (mode == null) {
			return ExecutionMode.PRODUCTION;
		}
		switch (mode) {
			case DEVELOPMENT:
			case TEST:
				return ExecutionMode.DEVELOPMENT;
			case PRODUCTION:
			default:
				return ExecutionMode.PRODUCTION;
		}
	}

	/**
	 * The environment {@link OperationModeService} reports, or <code>null</code> if that service is
	 * not started.
	 *
	 * <p>
	 * {@link OperationModeService#getInstance()} throws on an unstarted module, and this method sits
	 * on the tool-gating path of every run - including in unit tests and other minimal setups that
	 * boot no services at all. Answering <code>null</code> there lets
	 * {@link #executionMode(boolean, OperationMode)} fall back to the most restricted mode instead of
	 * failing the run.
	 * </p>
	 */
	private static OperationMode configuredOperationMode() {
		if (!OperationModeService.Module.INSTANCE.isActive()) {
			return null;
		}
		return OperationModeService.getInstance().getMode();
	}
}
