/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.Collection;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.security.RoleToolAccess.Decision;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * The tool-authorization decision of one agent run, bound to that run's principal.
 *
 * <p>
 * Wraps the pure {@link RoleToolAccess} policy evaluation with everything that is specific to the run
 * rather than to the policy: which roles the principal holds, whether the principal is an
 * administrator, whether the run has a human who could answer an approval prompt, and how a tool
 * object maps to the name the policy knows it by.
 * </p>
 *
 * <p>
 * The roles are resolved <b>once</b>, when the run's context is built, and carried here - never
 * re-read from {@code TLContext} per check. A concurrent tool batch runs on worker threads that
 * re-establish the sub-session, and a design that re-reads thread state per check is one thread-local
 * bug away from checking the wrong user.
 * </p>
 *
 * @see com.top_logic.tl.ai.core.agent.TopLogicSecurityContext
 * @see AiToolAccessService
 */
public class ToolAuthorization {

	/**
	 * Grants every tool without consulting a policy - the behaviour of an application that has
	 * configured none.
	 *
	 * <p>
	 * Used for every run whose mode does not {@link AiSecurityMode#enforcesToolAccess() enforce} tool
	 * access, which is every run until an administrator writes a {@code <tool-access>} policy.
	 * </p>
	 */
	public static final ToolAuthorization UNCHECKED = new ToolAuthorization(null, null, Set.of(), false);

	/**
	 * Resolves the identity a {@link Tool} has for the access policy.
	 *
	 * <p>
	 * Exists because the run and the policy know a tool by different names: the agent's catalog keys
	 * it by {@link Tool#name()}, the registry and the policy by {@code {factory}.{tool}}. See
	 * {@link ToolIdentity}.
	 * </p>
	 */
	@FunctionalInterface
	public interface Identities {

		/** Uses only what the tool itself declares; no qualified name is resolved. */
		Identities DECLARED_ONLY = ToolIdentity::of;

		/**
		 * The identity of the given tool.
		 *
		 * @param tool
		 *        The tool to identify.
		 * @return Its identity, never <code>null</code>.
		 */
		ToolIdentity identify(Tool tool);
	}

	private final RoleToolAccess _access;

	private final Person _principal;

	private final Set<String> _heldRoleNames;

	private final boolean _background;

	private final Identities _identities;

	/**
	 * Creates a {@link ToolAuthorization} for one run.
	 *
	 * @param access
	 *        The policy to evaluate.
	 * @param principal
	 *        The user the run acts as, or <code>null</code> for a run with no user identity. A
	 *        <code>null</code> principal holds no roles and is therefore granted nothing beyond the
	 *        policy's baseline.
	 * @param heldRoleNames
	 *        The names of the roles {@code principal} holds on the security root, resolved once by
	 *        the caller.
	 * @param background
	 *        Whether this run has no human to answer approval prompts, in which case the policy's
	 *        background allowlist applies as an additional ceiling.
	 */
	public ToolAuthorization(RoleToolAccess access, Person principal, Collection<String> heldRoleNames,
			boolean background) {
		this(access, principal, heldRoleNames, background, Identities.DECLARED_ONLY);
	}

	/**
	 * Creates a {@link ToolAuthorization} for one run.
	 *
	 * @param access
	 *        The policy to evaluate.
	 * @param principal
	 *        The user the run acts as, or <code>null</code> for a run with no user identity. A
	 *        <code>null</code> principal holds no roles and is therefore granted nothing beyond the
	 *        policy's baseline.
	 * @param heldRoleNames
	 *        The names of the roles {@code principal} holds on the security root, resolved once by
	 *        the caller.
	 * @param background
	 *        Whether this run has no human to answer approval prompts, in which case the policy's
	 *        background allowlist applies as an additional ceiling.
	 * @param identities
	 *        Resolves the name the policy knows a {@link Tool} by; typically backed by the tool
	 *        registry.
	 */
	public ToolAuthorization(RoleToolAccess access, Person principal, Collection<String> heldRoleNames,
			boolean background, Identities identities) {
		_access = access;
		_principal = principal;
		_heldRoleNames = heldRoleNames == null ? Set.of() : Set.copyOf(heldRoleNames);
		_background = background;
		_identities = identities == null ? Identities.DECLARED_ONLY : identities;
	}

	/**
	 * Why the run's principal may not use the given tool, recording the denial in the audit log.
	 *
	 * @param tool
	 *        The tool being checked.
	 * @return A message naming the missing capability, safe to feed back to the model, or
	 *         <code>null</code> if the tool is allowed.
	 */
	public String denialReason(Tool tool) {
		return denialReason(_identities.identify(tool));
	}

	/**
	 * Why the run's principal may not use the tool of the given name, recording the denial in the
	 * audit log.
	 *
	 * <p>
	 * For tools whose metadata is not available - a name the model produced that the catalog does not
	 * resolve. Such a tool is ungrouped by construction, so only an explicit per-tool grant reaches
	 * it.
	 * </p>
	 *
	 * @param toolName
	 *        The tool name.
	 * @return A message naming the missing capability, or <code>null</code> if the tool is allowed.
	 */
	public String denialReason(String toolName) {
		return denialReason(ToolIdentity.of(toolName));
	}

	/**
	 * Why the run's principal may not use the given tool, recording the denial in the audit log.
	 *
	 * <p>
	 * Call this at the invocation gate, where a denial is a real event. Filtering the tool schemas
	 * sent to the provider is a different matter - the model simply never sees a capability it could
	 * not use - and uses the quiet {@link #allows(Tool)} so that one denial produces one log line
	 * rather than one per turn.
	 * </p>
	 *
	 * @param tool
	 *        The tool being checked.
	 * @return A message naming the missing capability, or <code>null</code> if the tool is allowed.
	 */
	public String denialReason(ToolIdentity tool) {
		String reason = decide(tool);
		return reason == null ? null : audit(tool, reason);
	}

	/**
	 * Why the run's principal may not use the given capability, recording the denial in the audit log.
	 *
	 * <p>
	 * The capability-level counterpart of {@link #denialReason(Tool)}, for an operation that costs a
	 * capability the tool it belongs to is not classified as. The gate at the invocation point decides
	 * per tool and without arguments; {@code executeTLScript} is classified {@link ToolGroups#SCRIPT}
	 * and yet persists arbitrary writes when called with {@code withCommit=true}, so that argument
	 * additionally costs {@link ToolGroups#DATA_WRITE} and only the tool itself can ask.
	 * </p>
	 *
	 * <p>
	 * Decided in the same order as {@link #decide(ToolIdentity)} - background allowlist first, which
	 * binds administrators too, then the administrator bypass, then the role grants - so that a
	 * capability cannot be reached through an argument where the same capability's tools are out of
	 * reach.
	 * </p>
	 *
	 * @param capability
	 *        The name of the required capability (tool group).
	 * @param operation
	 *        What the run is about to do, for the denial message - e.g.
	 *        {@code "executing TL-Script with withCommit=true"}.
	 * @return A message naming the missing capability, safe to feed back to the model, or
	 *         <code>null</code> if the capability is allowed.
	 */
	public String capabilityDenialReason(String capability, String operation) {
		String reason = decideCapability(capability, operation);
		return reason == null ? null : auditCapability(capability, reason);
	}

	/**
	 * The denial reason for the given capability, or <code>null</code> if it is allowed. No side
	 * effects.
	 */
	private String decideCapability(String capability, String operation) {
		if (_access == null) {
			// No policy configured: the application has not opted in, so nothing is checked.
			return null;
		}
		if (_background && !_access.capabilityAllowedInBackground(capability)) {
			return "Permission denied: " + operation + " requires the '" + capability
				+ "' capability, which is not on the background allowlist. This run has no user who"
				+ " could approve it, so only the allowlisted capabilities are reachable.";
		}
		if (_principal != null && _principal.isAdmin()) {
			return null;
		}
		Decision decision = _access.checkCapability(_heldRoleNames, capability, operation);
		return decision instanceof Decision.Denied denied ? denied.reason() : null;
	}

	/**
	 * The capability (tool group) the given tool requires, as this run's policy classifies it.
	 *
	 * <p>
	 * The name a denial would put in front of the user, resolved for a tool that has not been denied
	 * yet. Used to tell an agent which capabilities its principal lacks without listing the tools
	 * behind them: {@code data.write} is one line an administrator can act on, where the twenty-nine
	 * tool names in it are not.
	 * </p>
	 *
	 * @param tool
	 *        The tool to classify.
	 * @return The capability name, or <code>null</code> for an ungrouped tool - one no capability
	 *         grant can reach.
	 */
	public String capabilityOf(Tool tool) {
		if (_access == null) {
			return ToolIdentity.of(tool).declaredGroup();
		}
		return _access.groupOf(_identities.identify(tool));
	}

	/**
	 * Whether the run's principal may use the given tool, without logging a denial.
	 */
	public boolean allows(Tool tool) {
		return _access == null || decide(_identities.identify(tool)) == null;
	}

	/**
	 * Whether the run's principal may use the tool of the given name, without logging a denial.
	 */
	public boolean allows(String toolName) {
		return _access == null || decide(ToolIdentity.of(toolName)) == null;
	}

	/**
	 * The denial reason for the given tool, or <code>null</code> if it is allowed. No side effects.
	 */
	private String decide(ToolIdentity tool) {
		if (_access == null) {
			// No policy configured: the application has not opted in, so nothing is checked.
			return null;
		}
		// The background ceiling applies to administrators too: its justification is the missing
		// approval prompt, not the principal's authority. An administrator who needs a write tool in
		// an unattended run says so by putting its group in the background allowlist.
		if (_background && !_access.allowedInBackground(tool)) {
			return "Permission denied: tool '" + tool.displayName()
				+ "' is not on the background allowlist. This run has no user who could approve it,"
				+ " so only the allowlisted capabilities are reachable.";
		}
		if (_principal != null && _principal.isAdmin()) {
			// Mirrors the engine: an administrator already bypasses ModelAccessRights
			// (AccessManager.hasRole), and a tool layer that disagreed would produce an
			// unexplainable half-denial.
			return null;
		}
		Decision decision = _access.check(_heldRoleNames, tool);
		return decision instanceof Decision.Denied denied ? denied.reason() : null;
	}

	/**
	 * The roles the run's principal holds on the security root.
	 */
	public Set<String> heldRoleNames() {
		return _heldRoleNames;
	}

	/**
	 * Records the denial where an operator can find it, then returns the reason unchanged.
	 *
	 * <p>
	 * Answering "why can this user not use this tool?" is what makes the layer operable, so the log
	 * line names the tool, the account, the roles it holds and the rule that decided - and nothing
	 * about the data that was withheld.
	 * </p>
	 */
	private String audit(ToolIdentity tool, String reason) {
		Logger.warn("Tool access denied: tool='" + tool.displayName()
			+ "' group=" + _access.groupOf(tool)
			+ principalSuffix()
			+ " reason=" + reason, ToolAuthorization.class);
		return reason;
	}

	/**
	 * Records a capability-level denial in the same form as a tool denial, so that one log format
	 * answers "why was this refused?" for both.
	 */
	private String auditCapability(String capability, String reason) {
		Logger.warn("Tool access denied: capability='" + capability + "'"
			+ principalSuffix()
			+ " reason=" + reason, ToolAuthorization.class);
		return reason;
	}

	private String principalSuffix() {
		return " user=" + (_principal == null ? "(none)" : _principal.getName())
			+ " roles=" + _heldRoleNames
			+ (_background ? " background=true" : "");
	}
}
