/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.citation;

import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.tl.ai.core.agent.ToolContext;

/**
 * The {@code [n]} marker numbers handed out to one agent run.
 *
 * <p>
 * The cite tool is {@code READ}-classified and therefore parallel-safe, so two cite calls of the
 * same turn run on two pool threads. A counter per thread would hand both of them the number 1 and
 * the answer would point two facts at the same source. The counter therefore belongs to the run:
 * one instance lives on the run's {@link ToolContext} and is shared by every copy of it, so all
 * cite calls of a run draw from it.
 * </p>
 *
 * @see ToolContext#citationMarkers()
 */
public final class CitationMarkers {

	private final AtomicInteger _next = new AtomicInteger(1);

	/** Creates a counter that starts at 1. */
	public CitationMarkers() {
		// Default state.
	}

	/**
	 * The next marker number, 1-based, distinct for every call of the run.
	 */
	public int next() {
		return _next.getAndIncrement();
	}

	/**
	 * Restarts the numbering, called by {@code AgentRunner} at the start of a run.
	 */
	public void reset() {
		_next.set(1);
	}
}
