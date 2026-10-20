/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.authentication;

import java.util.Objects;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.security.ModelAccessRightsSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.util.TLContextManager;

/**
 * The account an MCP request was authenticated as, for the code that serves the request.
 *
 * <p>
 * {@link McpAuthenticationFilter} resolves the account and binds it here for the duration of the
 * request; the resource, completion and tool handlers ask for it through
 * {@link #inCallerContext(ComputationEx2)} and act as it. The MCP SDK is configured with
 * {@code immediateExecution(true)}, which keeps every handler on the servlet request thread (the
 * transport {@code .block()}s that thread while the handler runs), so a thread-bound principal is
 * the whole mechanism - no context propagation is needed.
 * </p>
 *
 * <h2>Why this exists rather than {@code TLContext.currentUser()}</h2>
 *
 * <p>
 * The filter also opens the {@code TLContext}, so the ambient user is in fact correct inside a
 * handler. Reading it would nevertheless be wrong here: {@code TLContextManager.inPersonContext} and
 * {@code ThreadContextManager.inSystemInteraction} are both <em>no-ops when an interaction is
 * already bound</em>, so a handler that trusted the ambient context would serve whatever context it
 * happened to find - which, before this class, was the engine's super-user system interaction. This
 * class makes the demand explicit and unsatisfiable by accident: no bound caller, no read.
 * </p>
 *
 * @see McpAuthenticationFilter
 */
public final class McpCallerContext {

	/**
	 * The account of the request being served on this thread, or <code>null</code> outside a
	 * request.
	 *
	 * <p>
	 * Holds the {@link Person} rather than its login name because the binding and every read of it
	 * happen on one thread within one request, so the wrapper cannot go stale in between. A design
	 * that carried the principal across a thread boundary would have to carry the name and re-resolve
	 * it.
	 * </p>
	 */
	private static final ThreadLocal<Person> CALLER = new ThreadLocal<>();

	private McpCallerContext() {
		// Static utility.
	}

	/**
	 * Binds {@code caller} to the calling thread and runs {@code job} with it bound.
	 *
	 * <p>
	 * Restores the previous binding afterwards, so a nested call cannot leak an identity onto a
	 * pooled thread.
	 * </p>
	 *
	 * @param caller
	 *        The authenticated account. Never <code>null</code>: an unauthenticated request is
	 *        refused before it reaches here.
	 * @param job
	 *        What to run.
	 */
	public static <T, E1 extends Throwable, E2 extends Throwable> T withCaller(Person caller,
			ComputationEx2<T, E1, E2> job) throws E1, E2 {
		Objects.requireNonNull(caller, "The MCP endpoint has no context without an account.");

		Person before = CALLER.get();
		CALLER.set(caller);
		try {
			return job.run();
		} finally {
			if (before == null) {
				CALLER.remove();
			} else {
				CALLER.set(before);
			}
		}
	}

	/**
	 * The account the request being served on this thread was authenticated as.
	 *
	 * @return The caller, or <code>null</code> if this thread is not serving an authenticated MCP
	 *         request.
	 */
	public static Person caller() {
		return CALLER.get();
	}

	/**
	 * The account this request acts as, or a failure saying that there is none.
	 *
	 * @throws IllegalStateException
	 *         If no account is bound to the calling thread. There is no fallback: a read that cannot
	 *         name the account it serves would have to apply everybody's rights or nobody's, and both
	 *         answers are wrong.
	 */
	public static Person requireCaller() {
		Person caller = CALLER.get();
		if (caller == null) {
			throw new IllegalStateException("What this MCP request may see is decided against the"
				+ " account it was authenticated as, and no account is bound to this thread. The"
				+ " request must pass McpAuthenticationFilter, and the MCP server must be built with"
				+ " immediateExecution(true) so that the handler runs on the request thread.");
		}
		return caller;
	}

	/**
	 * What the account this request acts as may see of the model.
	 *
	 * <p>
	 * The one place a {@code TLObject} becomes something an MCP client gets to see. Establishing the
	 * account's {@code TLContext} is <b>not</b> enough on its own: a {@code KnowledgeBase} read is not
	 * access-right filtered by the engine, so a resource handler that only swapped the context would
	 * still return every object it was asked for. The filtering is this object's job, and it is the
	 * same one the model tools use.
	 * </p>
	 *
	 * @return A filter bound to this request's account, never <code>null</code>.
	 * @throws IllegalStateException
	 *         If no account is bound - see {@link #requireCaller()}.
	 */
	public static ReadFilter readFilter() {
		return ReadFilter.forSecurity(new ModelAccessRightsSecurity(requireCaller()));
	}

	/**
	 * Runs {@code job} in the {@code TLContext} of the account this request acts as.
	 *
	 * <p>
	 * The replacement for {@link ThreadContextManager#inSystemInteraction} in every MCP handler that
	 * touches the {@code KnowledgeBase}. The system interaction is the engine's super-user bypass:
	 * used here it made every read act as nobody, with model access rights not applying by
	 * construction.
	 * </p>
	 *
	 * <p>
	 * Establishing the context is idempotent - {@link TLContextManager#inPersonContext} passes
	 * straight through when an interaction is already bound, which it is, because the filter opened
	 * one for the whole request. The call is kept anyway so that the handler works standalone and so
	 * that the context it needs is visible at the point that needs it.
	 * </p>
	 *
	 * @param job
	 *        The computation to run as the request's account.
	 * @throws IllegalStateException
	 *         If no account is bound - see {@link #requireCaller()}.
	 */
	public static <T, E1 extends Throwable, E2 extends Throwable> T inCallerContext(
			ComputationEx2<T, E1, E2> job) throws E1, E2 {
		return TLContextManager.inPersonContext(requireCaller(), job);
	}
}
