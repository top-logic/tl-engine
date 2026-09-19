/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.TopLogicSecurityContext;

/**
 * The {@link SecurityContextFactory} this product ships: every run is authorized against the roles
 * and the model access rights of the account it acts as.
 *
 * <p>
 * What the entry points built by hand before the seam existed, so an installation that configures no
 * factory behaves exactly as before. Approval follows the tool's
 * {@link com.top_logic.tl.ai.tool.api.ToolMode} alone - a read is allowed, everything that changes
 * something is asked about.
 * </p>
 */
public class DefaultSecurityContextFactory implements SecurityContextFactory {

	/** The shared instance; this factory has no state. */
	public static final DefaultSecurityContextFactory INSTANCE = new DefaultSecurityContextFactory();

	@Override
	public SecurityContext createSecurityContext(RunPrincipal run) {
		return defaultContext(run);
	}

	/**
	 * The context this product would build for the given run.
	 *
	 * <p>
	 * The starting point of an application's own factory: build this, then override the one answer
	 * that is to differ and delegate the rest to it. Implementing {@link SecurityContext} from
	 * scratch instead would drop the role check, the model access rights and the background ceiling,
	 * none of which the approval question is about.
	 * </p>
	 *
	 * @param run
	 *        Who the run acts as and how it was started. Never <code>null</code>.
	 * @return The context, never <code>null</code>.
	 */
	public static SecurityContext defaultContext(RunPrincipal run) {
		return TopLogicSecurityContext.forRun(run.account(), run.background());
	}

}
