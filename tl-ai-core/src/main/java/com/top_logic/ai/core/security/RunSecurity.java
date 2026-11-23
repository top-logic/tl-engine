/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.security;

import com.top_logic.ai.core.agent.AgentRuntimeService;
import com.top_logic.ai.core.agent.SecurityContext;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;

/**
 * Where every entry point gets the {@link SecurityContext} of the run it is about to start.
 *
 * <p>
 * One call, so that the installation's {@link SecurityContextFactory} is asked wherever a run begins
 * and an application does not have to find every entry point to be sure of it. Before this existed,
 * each entry point built {@code TopLogicSecurityContext} itself, which is the one line an application
 * could not substitute (Gitea #381).
 * </p>
 *
 * <p>
 * It is also the one place that checks the {@link RunPrincipal#subject() subject} of a run. The
 * model reads the subject in the environment block, so a subject the run may not read would
 * disclose an object the account may not see. The check is here, and not at each entry point or in
 * an application hook, so that no entry point can forget it.
 * </p>
 */
public final class RunSecurity {

	private RunSecurity() {
		// Static utility.
	}

	/**
	 * The security context of the run described by {@code run}.
	 *
	 * <p>
	 * The factory is asked with the subject as the entry point stated it. The context returned names
	 * the subject only if the run may read it, decided by the {@link SecurityContext#modelSecurity()
	 * model access} of that context: the rights the run really has, also where a factory narrows
	 * them. Without Read the run has no subject, and the log gets one line that names the type and
	 * the identifier of the object but not its label.
	 * </p>
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
		return contextFor(run, factory());
	}

	/**
	 * {@link #contextFor(RunPrincipal)} with the given factory in place of the configured one.
	 *
	 * <p>
	 * Package-visible so that a test can give the check the context of a factory of its own.
	 * </p>
	 */
	static SecurityContext contextFor(RunPrincipal run, SecurityContextFactory factory) {
		SecurityContext result = factory.createSecurityContext(run);
		if (result == null) {
			throw new IllegalStateException("The configured " + SecurityContextFactory.class.getSimpleName()
				+ " answered no security context for a run of " + run.entryPoint()
				+ ". A run without a context cannot be authorized; return the default context instead.");
		}
		RunPrincipal named = result.principal() != null ? result.principal() : run;
		RunPrincipal checked = withReadableSubject(named, result);
		return checked == result.principal() ? result : new Described(result, checked);
	}

	/**
	 * The given principal, without its subject if the given context may not read it.
	 */
	private static RunPrincipal withReadableSubject(RunPrincipal run, SecurityContext context) {
		TLObject subject = run.subject();
		if (subject == null || mayRead(context, run, subject)) {
			return run;
		}
		return new RunPrincipal(run.account(), run.entryPoint(), run.background(), run.conversationId(), null);
	}

	private static boolean mayRead(SecurityContext context, RunPrincipal run, TLObject subject) {
		String failure;
		try {
			if (context.modelSecurity().canRead(subject)) {
				return true;
			}
			failure = "the account may not read it";
		} catch (RuntimeException ex) {
			// A subject that was deleted after the entry point named it, for example. The run goes
			// on without one rather than failing for an object it would not be told about anyway.
			// Only the class of the exception goes into the line: its message can name the object.
			failure = "its read access cannot be checked (" + ex.getClass().getName() + ")";
			Logger.debug("Read access to the subject of a run could not be checked.", ex, RunSecurity.class);
		}
		Logger.info("A run (" + run.entryPoint() + ") of the account '" + accountName(run.account())
			+ "' has no subject: " + objectName(subject) + " was stated, and " + failure + ".", RunSecurity.class);
		return false;
	}

	private static String accountName(Person account) {
		return account == null ? "" : account.getName();
	}

	/** The type and the identifier of an object. Never its label, which is business data. */
	private static String objectName(TLObject object) {
		String id = object.tId() == null ? "?" : object.tId().asString();
		try {
			return object.tType() == null ? id : TLModelUtil.qualifiedName(object.tType()) + " " + id;
		} catch (RuntimeException ex) {
			// The type of a deleted object cannot be read; its identifier still names it.
			return id;
		}
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
