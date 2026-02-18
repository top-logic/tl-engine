/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.handoff;

/**
 * Structured payload for sub-agent calls so callers send a distilled "work order,"
 * not the full user prompt.
 *
 * <p>
 * Used when Orchestrator (or TLScript) delegates to {@code ModelExplorer}, {@code ModelEditor}, or
 * {@code TLScriptAgent}. Prevents prompt dumping and ensures clear handoff.
 * </p>
 *
 * @param goal
 *        Short intent (e.g. "list types in application modules").
 * @param task
 *        Concrete question or instruction.
 * @param context
 *        Context: model id, entity hints, relevant type/part names.
 * @param expectedOutputFormat
 *        Expected output format (e.g. "JSON with {@code elementIds}, fields, annotations, notes").
 */
public record AgentWorkOrder(
	String goal,
	String task,
	String context,
	String expectedOutputFormat) {

	/**
	 * Creates a work order with null-safe empty strings for optional fields.
	 */
	public static AgentWorkOrder of(String goal, String task, String context, String expectedOutputFormat) {
		return new AgentWorkOrder(
			goal != null ? goal : "",
			task != null ? task : "",
			context != null ? context : "",
			expectedOutputFormat != null ? expectedOutputFormat : "");
	}

	/**
	 * One-line summary for trace/logging.
	 */
	public String toSummary() {
		String t = task != null && !task.isEmpty() ? task : goal;
		return (t.length() > 60 ? t.substring(0, 57) + "..." : t);
	}
}
