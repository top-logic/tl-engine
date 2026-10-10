/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import com.top_logic.tl.ai.core.agent.SecurityContext;

/**
 * Builds the {@link SecurityContext} of one agent run.
 *
 * <p>
 * The seam an application substitutes when its answer to "does this call need a human?" depends on
 * more than the tool's {@link com.top_logic.tl.ai.tool.api.ToolMode}. Three writes of the same
 * {@code DATA_WRITE} tool can deserve three different answers - overwrite an empty section, overwrite
 * what a person wrote, release a document to a customer - and the only thing that tells them apart is
 * the call's arguments, which {@link SecurityContext#checkApproval} receives. An application
 * therefore overrides {@code checkApproval} and returns its own context here; nothing about the
 * approval mechanism itself changes.
 * </p>
 *
 * <p>
 * Configured once, on {@code AgentRuntimeService}, and asked by every entry point that starts a run - the
 * chat, the TL-Script functions, the MCP endpoint, a scheduler. The shipped default is
 * {@link DefaultSecurityContextFactory}, which is what the entry points did by hand before this
 * existed, so an installation that configures nothing is unchanged.
 * </p>
 *
 * <p>
 * <b>A factory may narrow and it may not widen.</b> Everything else this product decides about a run
 * is decided against the account's roles - which tools it may call, which agents it may run, which
 * objects it may read - and a context that answered those questions more generously than
 * {@code TopLogicSecurityContext} would hand the run authority its principal does not have. The safe
 * way to write one is to extend {@code TopLogicSecurityContext}'s answer rather than to implement
 * {@link SecurityContext} from scratch: build the default with
 * {@link DefaultSecurityContextFactory#defaultContext(RunPrincipal)}, wrap it in a
 * {@link SecurityContextProxy}, and override the one answer that is to differ.
 * </p>
 *
 * @see RunPrincipal
 */
public interface SecurityContextFactory {

	/**
	 * The security context the given run is to be authorized against.
	 *
	 * <p>
	 * Called once per run, before its first provider request. The result is carried for the whole run
	 * and is passed on to every sub-agent it delegates to, so nothing here may depend on which turn or
	 * which tool call is being served.
	 * </p>
	 *
	 * @param run
	 *        Who the run acts as and how it was started. Never <code>null</code>.
	 * @return The context of the run; never <code>null</code>.
	 */
	SecurityContext createSecurityContext(RunPrincipal run);

}
