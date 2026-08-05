/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Set;

import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.security.AiSecurityMode;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Security context for tool execution.
 *
 * <p>Combines four concerns:</p>
 * <ul>
 *   <li><b>User-level access</b> ({@link #hasToolPermission}) — can this user invoke this tool at all?
 *       Answered against the roles the run's principal holds, but only where the run's
 *       {@link #securityMode() mode} enforces tool access; unenforced by default, so an application
 *       that has configured no {@code <tool-access>} policy sees {@code true} throughout.</li>
 *   <li><b>Model access</b> ({@link #modelSecurity}) - may the run's principal read or change a
 *       given model object? Consulted only when {@link #securityMode()} enforces it.</li>
 *   <li><b>System execution mode</b> ({@link #executionMode}) — is the system in development,
 *       production, or maintenance mode? The agent loop uses this to gate STRUCTURAL tools.</li>
 *   <li><b>Approval policy</b> ({@link #checkApproval}) — does this tool call need user
 *       confirmation before executing?</li>
 * </ul>
 *
 * @see ToolContext
 * @see ExecutionMode
 * @see PermissionDecision
 * @see AiSecurityMode
 */
public interface SecurityContext {

	/**
	 * Whether the current user is allowed to invoke the given tool.
	 *
	 * <p>This is a coarse check — it answers "can this user use this tool at all?"
	 * not "can this user use this tool with these specific arguments?" The latter
	 * is handled inside the tool itself.</p>
	 *
	 * @param tool the tool to check
	 * @return {@code true} if the user may invoke the tool
	 */
	boolean hasToolPermission(Tool tool);

	/**
	 * Whether the current user has permission to invoke a tool by name.
	 *
	 * <p>For a caller that holds a tool name but no {@link Tool}. The default returns {@code true} -
	 * permissive for unknown names. Override to integrate with a role-based permission system.</p>
	 *
	 * <p>
	 * <b>Not the agent loop's path.</b> A name the run's registry does not resolve cannot execute at
	 * all, and such a name has no group, so answering it here would report every hallucinated or
	 * misspelled name as a permission problem. The loop reports "no such tool" instead and reserves
	 * {@link #hasToolPermission(Tool)} for names that resolve.
	 * </p>
	 *
	 * @param toolName the tool name
	 * @return {@code true} if the user has permission
	 */
	default boolean hasToolPermission(String toolName) {
		return true;
	}

	/**
	 * Why {@link #hasToolPermission(Tool)} refused the given tool.
	 *
	 * <p>
	 * Only meaningful after {@code hasToolPermission} returned {@code false}; implementations must
	 * agree with it. The default returns <code>null</code>, which means "no specific reason" and lets
	 * the caller state a generic one.
	 * </p>
	 *
	 * <p>
	 * Exists so that a denial can name the <em>missing capability</em> rather than merely the fact of
	 * denial: "why can this user not use this tool?" must be answerable both by the model, which has
	 * to decide what to do instead, and by an operator reading the log. The reason names the
	 * capability, the rule and the role - never the data that was withheld.
	 * </p>
	 *
	 * @param tool
	 *        The tool that was refused.
	 * @return The reason, or <code>null</code> if none more specific than "denied" is available.
	 */
	default String toolDenialReason(Tool tool) {
		return null;
	}

	/**
	 * Why {@link #hasToolPermission(String)} refused the tool of the given name.
	 *
	 * @param toolName
	 *        The tool name that was refused.
	 * @return The reason, or <code>null</code> if none more specific than "denied" is available.
	 * @see #toolDenialReason(Tool)
	 */
	default String toolDenialReason(String toolName) {
		return null;
	}

	/**
	 * Why this run may not use the given capability, independently of any single tool.
	 *
	 * <p>
	 * The seam for an operation whose cost depends on an <em>argument</em>, which the invocation gate
	 * cannot see: {@code executeTLScript} is classified as the {@code script} capability, and calling
	 * it with {@code withCommit=true} persists arbitrary writes, so that call additionally requires
	 * {@code data.write}. The tool asks here, where the argument is known, and gets the answer the
	 * same policy would have given for a {@code data.write} tool - including the background allowlist
	 * and the administrator bypass.
	 * </p>
	 *
	 * <p>
	 * The default permits, matching {@link #hasToolPermission(String)}: a context from an application
	 * that configured no policy must keep behaving as it did before this layer existed.
	 * </p>
	 *
	 * @param capability
	 *        The name of the required capability (tool group), e.g. {@code data.write}.
	 * @param operation
	 *        What the run is about to do, for the denial message - e.g.
	 *        {@code "executing TL-Script with withCommit=true"}.
	 * @return The reason the capability is refused, safe to feed back to the model, or
	 *         <code>null</code> if it is permitted.
	 */
	default String capabilityDenialReason(String capability, String operation) {
		return null;
	}

	/**
	 * The capability (tool group) the given tool requires under this run's policy.
	 *
	 * <p>
	 * The same name a denial would put in front of the user, resolved for a tool that has not been
	 * denied yet, so that an agent can be told which capabilities its principal lacks without
	 * enumerating the tools behind them. The default answers with what the tool declares about
	 * itself, which is all a context without a policy knows.
	 * </p>
	 *
	 * @param tool
	 *        The tool to classify.
	 * @return The capability name, or <code>null</code> for an ungrouped tool.
	 */
	default String toolCapability(Tool tool) {
		String declared = tool.group();
		return declared == null || declared.isBlank() ? null : declared;
	}

	/**
	 * Whether this run has no human who could answer an approval prompt, and is therefore
	 * additionally bounded by the policy's {@code <background-allowlist>}.
	 *
	 * <p>
	 * Read by the parts that have to <em>explain</em> the ceiling rather than apply it - the
	 * capability notice in the system prompt - so that a run which lost a capability to the allowlist
	 * does not report it as a right the user lacks. The default is {@code false}: a context that says
	 * nothing about this is an attended one.
	 * </p>
	 */
	default boolean isBackgroundRun() {
		return false;
	}

	/**
	 * The capabilities this run's principal could otherwise use that this run cannot, because it is
	 * unattended.
	 *
	 * <p>
	 * Empty unless {@link #isBackgroundRun()}. Purely descriptive - nothing decides anything from it.
	 * </p>
	 *
	 * @return The withheld capability names, in a stable order; never <code>null</code>.
	 */
	default Set<String> capabilitiesWithheldInBackground() {
		return Set.of();
	}

	/**
	 * This context, additionally bounded by the policy's {@code <background-allowlist>}.
	 *
	 * <p>
	 * For a run that becomes unattended after its context was built. The one caller is
	 * {@link ToolContext#forDetachedSubagent(TransactionScope, com.top_logic.tl.ai.tool.api.CancellationToken)}:
	 * a fire-and-forget subagent keeps running on a pool thread after the parent turn ended, so from
	 * that moment there is no human to answer an approval prompt - which is precisely the condition
	 * the allowlist exists for.
	 * </p>
	 *
	 * <p>
	 * Narrows and never widens; the principal and its roles are unchanged. The default returns
	 * {@code this}, which is correct for every context that authorizes nothing anyway.
	 * </p>
	 */
	default SecurityContext withBackgroundCeiling() {
		return this;
	}

	/**
	 * The effective security mode of the run this context belongs to.
	 *
	 * <p>
	 * Resolved once, before the first provider call, and never re-derived afterwards, so neither a
	 * tool nor an agent definition can widen it mid-run. The default is
	 * {@link AiSecurityMode#LEGACY}: a context that says nothing about its mode is one from an
	 * application that has not configured agent security, and must keep behaving as it did before
	 * the modes existed.
	 * </p>
	 */
	default AiSecurityMode securityMode() {
		return AiSecurityMode.LEGACY;
	}

	/**
	 * This context, tightened to the security mode an agent definition declares for itself.
	 *
	 * <p>
	 * The composition seam for {@link AiSecurityMode#stricter(AiSecurityMode, AiSecurityMode)}: the
	 * agent's value is a <em>floor</em>, so this returns {@code this} whenever the floor is absent or
	 * not stricter than {@link #securityMode()}, and never a context laxer than this one. It is what
	 * makes a delegated child run inherit its parent's mode and still honour a stricter floor of its
	 * own.
	 * </p>
	 *
	 * <p>
	 * The default implementation can only answer the no-op case: a context that does not know the
	 * run's principal has nothing to build a stricter one from, and answering {@code this} anyway
	 * would silently drop the floor. It therefore refuses instead.
	 * </p>
	 *
	 * @param agentFloor
	 *        The mode the agent definition declares, or <code>null</code> if it declares none.
	 * @return A context whose {@link #securityMode()} is
	 *         {@code stricter(securityMode(), agentFloor)}, possibly {@code this}.
	 * @throws IllegalStateException
	 *         If the floor is stricter than this context and this context cannot produce a stricter
	 *         one.
	 */
	default SecurityContext restrictedTo(AiSecurityMode agentFloor) {
		AiSecurityMode effective = AiSecurityMode.stricter(securityMode(), agentFloor);
		if (effective == securityMode()) {
			return this;
		}
		throw new IllegalStateException("This agent declares security mode " + agentFloor
			+ ", which is stricter than the " + securityMode() + " mode of the run it was started"
			+ " from, but that run's security context cannot be tightened: it has no principal to"
			+ " authorize against. Start such an agent through an entry point that resolves its"
			+ " declared mode before building the context.");
	}

	/**
	 * Per-object model access checks for the run's principal.
	 *
	 * <p>
	 * Tools use this to filter what they return and to refuse changes the principal may not make.
	 * The default is {@link ModelSecurity#UNCHECKED}, matching the {@link #securityMode() default
	 * mode}: checks are only meaningful once a mode that
	 * {@link AiSecurityMode#enforcesModelAccess() enforces} them is configured.
	 * </p>
	 */
	default ModelSecurity modelSecurity() {
		return ModelSecurity.UNCHECKED;
	}

	/**
	 * The system's current execution mode.
	 *
	 * <p>The agent loop uses this to enforce the system mode gate: STRUCTURAL tools
	 * are denied in {@link ExecutionMode#PRODUCTION}.</p>
	 */
	ExecutionMode executionMode();

	/**
	 * Whether this tool call needs user approval before executing.
	 *
	 * <p>Only called after {@link #hasToolPermission} passes and the system mode gate allows
	 * the tool. The default implementation requires approval for all state-changing
	 * tools ({@link ToolMode#USER_SCOPED}, {@link ToolMode#DATA_WRITE}, and {@link ToolMode#STRUCTURAL}).</p>
	 *
	 * <p><b>Contract:</b> implementations must be side-effect-free and stable within a turn.
	 * The agent loop calls this method more than once for the same tool call — first to
	 * predict whether a call can join a concurrent batch, then again as the execution
	 * gate.</p>
	 *
	 * @param tool     the tool to check
	 * @param toolCall the specific invocation
	 * @return the approval decision
	 */
	default PermissionDecision checkApproval(Tool tool, ToolCall toolCall) {
		return switch (tool.mode()) {
			case READ -> new PermissionDecision.Allowed();
			case USER_SCOPED -> new PermissionDecision.NeedsApproval(
				"Modifies your personal settings.", toolCall);
			case DATA_WRITE -> new PermissionDecision.NeedsApproval(
				"Modifies shared application data.", toolCall);
			case STRUCTURAL -> new PermissionDecision.NeedsApproval(
				"Modifies application structure.", toolCall);
		};
	}

	/**
	 * Context with no restrictions - for tests and trusted internal use.
	 *
	 * <p>
	 * Reports {@link AiSecurityMode#SYSTEM}: this context has no user identity, which is what that
	 * mode names. Reporting {@link AiSecurityMode#LEGACY} instead would be a lie about the reason
	 * nothing is checked here.
	 * </p>
	 */
	SecurityContext UNRESTRICTED = new SecurityContext() {

		@Override
		public boolean hasToolPermission(Tool tool) {
			return true;
		}

		@Override
		public AiSecurityMode securityMode() {
			return AiSecurityMode.SYSTEM;
		}

		@Override
		public ExecutionMode executionMode() {
			return ExecutionMode.DEVELOPMENT;
		}

		@Override
		public PermissionDecision checkApproval(Tool tool, ToolCall toolCall) {
			return new PermissionDecision.Allowed();
		}
	};
}
