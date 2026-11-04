/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.io.PrintStream;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.monitor.UserSession;
import com.top_logic.model.search.expr.config.operations.AdminOnly;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.monitoring.log.LogLine;
import com.top_logic.util.TopLogicServlet;

/**
 * TL-Script functions about the entries of the application log.
 *
 * <p>
 * Each entry written while a request of a session is handled carries the mark of that session,
 * the {@link TopLogicServlet#hashSessionIdForLog(String) hashed session id}, so that the entries of
 * a session can be found without the session id itself being written to the log.
 * </p>
 *
 * @implNote Every public static method here is a TL-Script function named by the
 *           {@link ScriptPrefix} of this class followed by the capitalized method name, so
 *           {@link #dump(LogLine)} is called <code>logDump</code>.
 */
@ScriptPrefix(LogFunctions.PREFIX)
public class LogFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "log";

	/**
	 * The mark the log entries of the session of the current request carry.
	 *
	 * @return The mark, <code>null</code> outside a request of a session.
	 */
	@Label("Log mark of the current session")
	public static String currentSessionMark() {
		InteractionContext interaction = ThreadContextManager.getInteraction();
		HttpServletRequest request = interaction == null ? null : interaction.asRequest();
		HttpSession session = request == null ? null : request.getSession(false);
		return session == null ? null : TopLogicServlet.hashSessionIdForLog(session.getId());
	}

	/**
	 * The mark the log entries of the given recorded session carry.
	 *
	 * @param session
	 *        The recorded session, e.g. a row of the session monitor.
	 * @return The mark, <code>null</code> without a session or for a session without id.
	 */
	@Label("Log mark of a session")
	@AdminOnly
	public static String sessionMark(UserSession session) {
		if (session == null) {
			return null;
		}
		String id = session.getSessionId();
		return StringServices.isEmpty(id) ? null : TopLogicServlet.hashSessionIdForLog(id);
	}

	/**
	 * Prints the given log entry to the console of the application.
	 *
	 * <p>
	 * The entry is printed with a heading naming the log viewer as its origin, so that a reader
	 * of the console does not take it for a new problem. It goes to the standard output rather
	 * than the error output, so that it does not alarm an observer of the error output.
	 * </p>
	 *
	 * @implNote The entry is written to {@link System#out}, not to {@link System#err}.
	 *
	 * @param entry
	 *        The log entry to print.
	 */
	@Label("Print a log entry to the console")
	@AdminOnly
	public static void dump(@Mandatory LogLine entry) {
		PrintStream out = System.out;
		out.println("Log entry printed from the log viewer:");
		out.println("--------------------------------------");
		out.println(entry.getMessage());
		out.println(entry.getDetails());
	}

}
