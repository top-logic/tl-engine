/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.function.Consumer;

import com.top_logic.base.context.TLSubSessionContext;

/**
 * The browser window an agent run belongs to, for a tool that reads or moves the page the user
 * looks at.
 *
 * <p>
 * Which windows exist is known only to the module that renders the chat into them, and this module
 * does not depend on the view layer. That module {@link #install(RunWindow) installs} its
 * implementation when it starts; without one every run is windowless.
 * </p>
 */
public interface RunWindow {

	/**
	 * Runs {@code action} in the browser window of {@code subSession} on the calling thread: in the
	 * window's sub-session, as one interaction of its session, with the window's root control.
	 *
	 * <p>
	 * The calling thread keeps its security context, so what the action reads is what the run's
	 * user may see.
	 * </p>
	 *
	 * @param subSession
	 *        The run's sub-session, on the agent thread the bound one. May be <code>null</code>.
	 * @param action
	 *        Receives the window's root control (a {@code ReactControl}).
	 * @return Whether the window was reached and the action ran; <code>false</code> for a run
	 *         without a window.
	 */
	boolean inWindow(TLSubSessionContext subSession, Consumer<Object> action);

	/**
	 * The installed implementation; one that reaches no window when none is installed.
	 */
	static RunWindow get() {
		RunWindow installed = Holder.INSTALLED;
		return installed == null ? (subSession, action) -> false : installed;
	}

	/**
	 * Installs the implementation, or removes it with <code>null</code>.
	 */
	static void install(RunWindow implementation) {
		Holder.INSTALLED = implementation;
	}

	/**
	 * Holds the {@link #install(RunWindow) installed} implementation.
	 */
	final class Holder {

		static volatile RunWindow INSTALLED;

		private Holder() {
			// Holder only.
		}
	}
}
