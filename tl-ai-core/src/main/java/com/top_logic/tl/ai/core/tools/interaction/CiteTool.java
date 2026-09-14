/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Records a structured citation for the current turn and returns the marker number to write.
 *
 * <p>
 * Call this after a tool returned an id or URL you will mention. The server keeps the citation
 * only when that id or URL appears in this turn's other tool results, then rewrites {@code [n]}
 * markers in the answer to match.
 * </p>
 */
public class CiteTool {

	private static final ThreadLocal<AtomicInteger> NEXT =
		ThreadLocal.withInitial(() -> new AtomicInteger(1));

	/**
	 * Creates a {@link CiteTool}.
	 */
	public CiteTool() {
		// Required public no-arg constructor for ReflectiveToolFactory.
	}

	/**
	 * Resets the per-thread marker counter at the start of a run.
	 */
	public static void reset() {
		NEXT.remove();
	}

	/**
	 * Records a citation and returns the 1-based marker to write after the fact it supports.
	 *
	 * @param kind host kind such as {@code web} or {@code invoice}
	 * @param id id the producing tool returned (a URL, a TID, …)
	 * @param label short label shown in the citation popover
	 * @param url link for a web source; omit otherwise
	 * @param toolCallId id of the tool call that produced {@code id}
	 * @return the marker number to write as {@code [n]}
	 */
	@ToolMethod(
		description = "Record a citation for a fact you just read. Pass the id or URL the producing "
			+ "tool returned, a short label, and a kind (web, invoice, …). Returns the marker "
			+ "number to write as [n] immediately after the sentence it supports. Cite only ids "
			+ "from this turn; never invent one.",
		mode = ToolMode.READ, group = ToolGroups.INTERACTION)
	public String cite(
			@P("Kind of source, e.g. web or invoice.") String kind,
			@P("Id the producing tool returned (URL, TID, invoice id, …).") String id,
			@P("Short label shown to the reader.") String label,
			@P(value = "URL for a web source. Omit for other kinds.", required = false) String url,
			@P(value = "Tool call id of the producing tool, when you have it.",
				required = false) String toolCallId) {
		if (id == null || id.isBlank()) {
			return "Error: id is required.";
		}
		int marker = NEXT.get().getAndIncrement();
		return Integer.toString(marker);
	}
}
