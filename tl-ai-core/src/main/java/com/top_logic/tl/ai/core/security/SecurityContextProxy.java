/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.Objects;
import java.util.Set;

import com.top_logic.tl.ai.core.agent.ExecutionMode;
import com.top_logic.tl.ai.core.agent.PermissionDecision;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.tool.api.Tool;

/**
 * A {@link SecurityContext} that answers everything the way another one does.
 *
 * <p>
 * What an application's {@link SecurityContextFactory} extends: override the one answer that is to
 * differ - normally {@link #checkApproval(Tool, ToolCall)} - and every other question keeps the
 * answer this product gives it. Writing a context from scratch instead would drop the role check, the
 * model access rights and the background ceiling, none of which the approval question is about, and
 * each of which permits by default on a bare {@link SecurityContext}.
 * </p>
 *
 * <pre>
 * SecurityContext base = DefaultSecurityContextFactory.defaultContext(run);
 * return new SecurityContextProxy(base) {
 *     &#64;Override
 *     public PermissionDecision checkApproval(Tool tool, ToolCall call) {
 *         ...
 *     }
 * };
 * </pre>
 *
 * <p>
 * The two methods that answer with a context - {@link #withBackgroundCeiling()} and
 * {@link #withoutAgentChoice()} - narrow the delegate and wrap the result in a proxy of the same kind,
 * so an override survives a run that becomes unattended. A subclass that carries state of its own
 * overrides {@link #copy(SecurityContext)} to carry it too.
 * </p>
 */
public class SecurityContextProxy implements SecurityContext {

	private final SecurityContext _impl;

	/**
	 * Creates a {@link SecurityContextProxy}.
	 *
	 * @param impl
	 *        The context every unanswered question goes to. Never <code>null</code>.
	 */
	public SecurityContextProxy(SecurityContext impl) {
		_impl = Objects.requireNonNull(impl, "A proxy needs a context to delegate to.");
	}

	/**
	 * The context this one delegates to.
	 */
	protected final SecurityContext impl() {
		return _impl;
	}

	/**
	 * This proxy over a narrowed delegate.
	 *
	 * <p>
	 * Override when the subclass carries state that has to be carried over. The default builds a plain
	 * {@link SecurityContextProxy}, which loses a subclass's overrides - correct only for a subclass
	 * that has none.
	 * </p>
	 *
	 * @param narrowed
	 *        The delegate to wrap.
	 */
	protected SecurityContext copy(SecurityContext narrowed) {
		return new SecurityContextProxy(narrowed);
	}

	@Override
	public boolean hasToolPermission(Tool tool) {
		return _impl.hasToolPermission(tool);
	}

	@Override
	public boolean hasToolPermission(String toolName) {
		return _impl.hasToolPermission(toolName);
	}

	@Override
	public String toolDenialReason(Tool tool) {
		return _impl.toolDenialReason(tool);
	}

	@Override
	public String toolDenialReason(String toolName) {
		return _impl.toolDenialReason(toolName);
	}

	@Override
	public String capabilityDenialReason(String capability, String operation) {
		return _impl.capabilityDenialReason(capability, operation);
	}

	@Override
	public boolean withheldByBackgroundCeiling(Tool tool) {
		return _impl.withheldByBackgroundCeiling(tool);
	}

	@Override
	public String toolCapability(Tool tool) {
		return _impl.toolCapability(tool);
	}

	@Override
	public boolean isBackgroundRun() {
		return _impl.isBackgroundRun();
	}

	@Override
	public Set<String> capabilitiesWithheldInBackground() {
		return _impl.capabilitiesWithheldInBackground();
	}

	@Override
	public SecurityContext withBackgroundCeiling() {
		SecurityContext narrowed = _impl.withBackgroundCeiling();
		return narrowed == _impl ? this : copy(narrowed);
	}

	@Override
	public RunAuthorization runAuthorization() {
		return _impl.runAuthorization();
	}

	@Override
	public SecurityContext withoutAgentChoice() {
		SecurityContext widened = _impl.withoutAgentChoice();
		return widened == _impl ? this : copy(widened);
	}

	@Override
	public RunPrincipal principal() {
		return _impl.principal();
	}

	@Override
	public ModelSecurity modelSecurity() {
		return _impl.modelSecurity();
	}

	@Override
	public ExecutionMode executionMode() {
		return _impl.executionMode();
	}

	@Override
	public PermissionDecision checkApproval(Tool tool, ToolCall toolCall) {
		return _impl.checkApproval(tool, toolCall);
	}

}
