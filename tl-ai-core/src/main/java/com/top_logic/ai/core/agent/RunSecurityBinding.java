/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.agent;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.thread.ThreadLocalBinding;

/**
 * Thread-local binding of the {@link SecurityContext} of the run whose tool executes on the current
 * thread.
 *
 * <p>
 * A tool can execute TL-Script, and the script can start a nested run with {@code aiCallTool},
 * {@code aiAgentChat} or {@code aiChat}. These functions get no context from their caller and build
 * one with {@link ToolContext#forCurrentUser()}. This binding gives them the context of the run that
 * executes the script, so that the nested run has every limit of that run: the
 * {@code <background-allowlist>} of a background run, and each narrowing that the entry point of the
 * run added. A nested run is therefore never wider than the run that executes it.
 * </p>
 *
 * <p>
 * Only the tool dispatch of the engine binds it, for the duration of one tool call: the agent loop
 * and {@code aiCallTool} through {@link com.top_logic.ai.core.tools.ToolExecutions}, an MCP request
 * through its tool adapter.
 * Outside a tool call nothing is bound, so a script of a person or of the script console runs as
 * before. As a {@link ThreadLocalBinding}, the value goes with the other bindings of the run onto the
 * threads that a tool call starts, for example the worker threads of a nested run.
 * </p>
 *
 * @see RunCancellationBinding
 */
@FrameworkInternal
public final class RunSecurityBinding {

	private static final ThreadLocalBinding<SecurityContext> SECURITY = new ThreadLocalBinding<>();

	private RunSecurityBinding() {
		// Static utility.
	}

	/**
	 * Binds the security context of the run whose tool is about to execute on the current thread.
	 *
	 * @param security
	 *        The security context of the run, or <code>null</code> for a call that names no run. Such
	 *        a call changes nothing: a tool call that names no run inside a background run must not
	 *        end the ceiling of that run.
	 * @return The handle whose {@link Binding#close() close()} restores the previously bound value,
	 *         or its absence.
	 */
	public static Binding bind(SecurityContext security) {
		return security == null ? Binding.NONE : new Binding(SECURITY.bind(security));
	}

	/**
	 * The security context of the run whose tool executes on the current thread, or
	 * <code>null</code> outside a tool call.
	 */
	public static SecurityContext current() {
		return SECURITY.get();
	}

	/**
	 * Whether the current thread executes a tool of a {@link SecurityContext#isBackgroundRun()
	 * background run}.
	 *
	 * <p>
	 * Asks {@link SecurityContext#isBackgroundRun()} and not the background flag of the run's
	 * principal, because a detached subagent becomes a background run after its principal was
	 * described.
	 * </p>
	 *
	 * <p>
	 * Code that a script can call and that starts a run which the {@code <background-allowlist>}
	 * cannot bound asks here, and refuses in a background run.
	 * </p>
	 *
	 * @return <code>false</code> outside a tool call, and in a tool call of a run that a person can
	 *         approve.
	 */
	public static boolean executesBackgroundRun() {
		SecurityContext security = SECURITY.get();
		return security != null && security.isBackgroundRun();
	}

	/**
	 * Handle returned by {@link RunSecurityBinding#bind(SecurityContext)}.
	 */
	public static final class Binding implements AutoCloseable {

		/** The handle of a call that bound nothing: its {@link #close()} changes nothing. */
		static final Binding NONE = new Binding(null);

		private final ThreadLocalBinding<SecurityContext>.Handle _handle;

		Binding(ThreadLocalBinding<SecurityContext>.Handle handle) {
			_handle = handle;
		}

		@Override
		public void close() {
			if (_handle != null) {
				_handle.close();
			}
		}
	}
}
