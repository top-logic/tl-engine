/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Set;

import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.RunAuthorization;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Security context for tool execution.
 *
 * <p>Combines four concerns:</p>
 * <ul>
 *   <li><b>User-level access</b> ({@link #hasToolPermission}) — can this user invoke this tool at all?
 *       Answered against the roles the run's principal holds.</li>
 *   <li><b>Model access</b> ({@link #modelSecurity}) - may the run's principal read or change a
 *       given model object?</li>
 *   <li><b>System execution mode</b> ({@link #executionMode}) — is the system in development,
 *       production, or maintenance mode? The agent loop uses this to gate STRUCTURAL tools.</li>
 *   <li><b>Approval policy</b> ({@link #checkApproval}) — does this tool call need user
 *       confirmation before executing?</li>
 * </ul>
 *
 * <p>
 * Both security concerns are unconditional. There is no configuration that turns them off, and no
 * run-level mode to consult: what a run may do follows from <em>who it acts as</em>, and a run that
 * cannot name a principal is refused where its context is built
 * ({@link TopLogicSecurityContext#forRun(com.top_logic.knowledge.wrap.person.Person, boolean)}). The
 * permissive defaults below therefore describe only {@link #UNRESTRICTED}, which is a test seam.
 * </p>
 *
 * @see ToolContext
 * @see ExecutionMode
 * @see PermissionDecision
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
	 * The default permits, matching {@link #hasToolPermission(String)}: it is what
	 * {@link #UNRESTRICTED} answers, and that context is a test seam.
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
	 * Whether the given tool is denied to this run <em>only</em> because the run is unattended.
	 *
	 * <p>
	 * Tells the two reasons a tool can be missing from an unattended run apart: the principal's roles
	 * do not grant it, or they do and the background allowlist is the ceiling that removed it. Both
	 * are {@link #hasToolPermission(Tool) denials}, and a message that blamed the user's roles for the
	 * second would send them to an administrator who has nothing to grant - the fix is a
	 * {@code <background-allowlist>} entry.
	 * </p>
	 *
	 * <p>
	 * <b>Not a gate and never asked instead of one.</b> {@link #hasToolPermission(Tool)} decides;
	 * this only attributes a decision it already made. The default answers <code>false</code>, which
	 * reads as "no reason to believe the ceiling did it" - correct for every context that has no
	 * background ceiling.
	 * </p>
	 *
	 * @param tool
	 *        The tool to attribute.
	 * @return Whether an attended run with the same principal would have permitted it.
	 */
	default boolean withheldByBackgroundCeiling(Tool tool) {
		return false;
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
	 * What this run may <em>be</em>: which agent it runs, which model it runs on, which MCP servers it
	 * reaches.
	 *
	 * <p>
	 * The sibling of {@link #hasToolPermission(Tool)}, which says what the run may <em>do</em>. Asked
	 * where a run is assembled - {@code AgentFactory} - rather than per tool call, because these three
	 * are settled before the first provider request.
	 * </p>
	 *
	 * <p>
	 * The default is {@link RunAuthorization#UNCHECKED}, which is what {@link #UNRESTRICTED} answers -
	 * a test seam. Every production context is a {@link TopLogicSecurityContext} and carries a real
	 * one.
	 * </p>
	 */
	default RunAuthorization runAuthorization() {
		return RunAuthorization.UNCHECKED;
	}

	/**
	 * Per-object model access checks for the run's principal.
	 *
	 * <p>
	 * Tools use this to filter what they return and to refuse changes the principal may not make.
	 * The default is {@link ModelSecurity#UNCHECKED}, which is what {@link #UNRESTRICTED} answers -
	 * a test seam. Every production context is a {@link TopLogicSecurityContext} and carries a real
	 * {@code ModelAccessRightsSecurity}.
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
	 * tools ({@link ToolMode#USER_SCOPED}, {@link ToolMode#DATA_WRITE}, and {@link ToolMode#STRUCTURAL}),
	 * and for {@link ToolMode#EXTERNAL} tools, whose state change lands in another system.</p>
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
			// The only gate an external call answers to. Nothing else here reaches it: the remote
			// system commits by itself, so rolling the turn back cannot undo it, and no retry or
			// compensation exists to fall back on. Asking before it goes out is the whole control,
			// which is why it is a prompt and not an allow.
			case EXTERNAL -> new PermissionDecision.NeedsApproval(
				"Sends data to a system outside this application.", toolCall);
		};
	}

	/**
	 * Context with no restrictions.
	 *
	 * <p>
	 * <b>A test seam.</b> It has no principal, so it cannot answer any question this interface asks
	 * about one; it permits instead. Its only production-shaped use is {@link ToolContext#EMPTY}, which
	 * is a test seam for the same reason. Nothing in production builds a context this way - every run
	 * goes through
	 * {@link TopLogicSecurityContext#forRun(com.top_logic.knowledge.wrap.person.Person, boolean)},
	 * which requires a principal.
	 * </p>
	 */
	SecurityContext UNRESTRICTED = new SecurityContext() {

		@Override
		public boolean hasToolPermission(Tool tool) {
			return true;
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
