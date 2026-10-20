/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.agent.SecurityContext;

/**
 * Where every entry point gets the {@link SecurityContext} of the run it is about to start.
 *
 * <p>
 * One call, so that the installation's {@link SecurityContextFactory} is asked wherever a run begins
 * and an application does not have to find every entry point to be sure of it. Before this existed,
 * each entry point built {@code TopLogicSecurityContext} itself, which is the one line an application
 * could not substitute (Gitea #381).
 * </p>
 */
public final class RunSecurity {

	private RunSecurity() {
		// Static utility.
	}

	/**
	 * The security context of the run described by {@code run}.
	 *
	 * @param run
	 *        Who the run acts as and how it was started. Never <code>null</code>.
	 * @return The context to carry on the run; never <code>null</code>.
	 * @throws IllegalStateException
	 *         If the run names no account - see
	 *         {@code TopLogicSecurityContext.forRun}.
	 *         An application's factory may refuse for its own reasons too.
	 */
	public static SecurityContext contextFor(RunPrincipal run) {
		SecurityContext result = factory().createSecurityContext(run);
		if (result == null) {
			throw new IllegalStateException("The configured " + SecurityContextFactory.class.getSimpleName()
				+ " answered no security context for a run of " + run.entryPoint()
				+ ". A run without a context cannot be authorized; return the default context instead.");
		}
		return result.principal() != null ? result : new Described(result, run);
	}

	/**
	 * A context that names the run it was built for, whatever the factory answered - see
	 * {@link SecurityContext#principal()}.
	 */
	private static final class Described extends SecurityContextProxy {

		private final RunPrincipal _run;

		Described(SecurityContext impl, RunPrincipal run) {
			super(impl);
			_run = run;
		}

		@Override
		public RunPrincipal principal() {
			return _run;
		}

		@Override
		protected SecurityContext copy(SecurityContext narrowed) {
			return new Described(narrowed, _run);
		}
	}

	/**
	 * The configured factory, or the shipped default when {@link AgentRuntimeService} is not running.
	 *
	 * <p>
	 * The fallback is for the minimal setups that boot no services and still call a tool - not a
	 * permissive mode: the default factory authorizes against the account's roles exactly as the
	 * configured one is expected to. A run is not started while the service restarts - the agent
	 * factory refuses it - so a run never gets the default in place of a configured factory.
	 * </p>
	 */
	public static SecurityContextFactory factory() {
		if (!AgentRuntimeService.Module.INSTANCE.isActive()) {
			return DefaultSecurityContextFactory.INSTANCE;
		}
		return AgentRuntimeService.getInstance().getSecurityContextFactory();
	}

}
