/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.citation;

import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;

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
 * <p>
 * The numbering is the <b>conversation's</b>, not the single run's. A run restarting it at 1 is
 * what made citing stop working after the first answer of a chat (#495): the model's context is
 * the whole conversation - it reads back the raw text of its earlier answers, markers and all -
 * while the counter handed out {@code 1} again on every send. A model that continued the
 * conversation's numbering then wrote a number no cite call had claimed, and the validator, which
 * cannot tell that from an invented one, deleted the marker and the source with it. Continuing the
 * numbering makes both readings of "which number do I write" agree: the number the tool returned is
 * also the next number the conversation has not used.
 * </p>
 *
 * <p>
 * What the <em>reader</em> sees is unaffected: {@code CitationValidator} renumbers each answer's
 * surviving markers to {@code [1]}, {@code [2]}, … for display. The large numbers are between the
 * model and the tool.
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
	 * Restarts the numbering at 1.
	 */
	public void reset() {
		_next.set(1);
	}

	/**
	 * Continues this conversation's numbering, called by {@code AgentRunner} at the start of a run.
	 *
	 * <p>
	 * The next number is one above the highest this conversation has already handed out, so no
	 * number the model can read back in its own context means two different sources. A conversation
	 * that never cited - and a run without a log - starts at 1.
	 * </p>
	 *
	 * @param log
	 *        the conversation this run continues; <code>null</code> for a run that keeps none
	 */
	public void resumeFrom(ConversationLog log) {
		_next.set(nextFreeMarker(log));
	}

	/**
	 * The first marker number {@code log} has not handed out yet.
	 *
	 * <p>
	 * Read from the cite calls' own results rather than from the answers' text: the text of a
	 * recorded turn has been renumbered for the reader, while the tool result is what the model was
	 * actually told to write.
	 * </p>
	 */
	public static int nextFreeMarker(ConversationLog log) {
		if (log == null) {
			return 1;
		}
		int highest = 0;
		for (Turn turn : log.turns()) {
			for (Part part : turn.assistantParts()) {
				if (part instanceof Part.ToolCallPart call && CitationValidator.isCite(call.toolName())) {
					highest = Math.max(highest, markerOf(call.result()));
				}
			}
		}
		return highest + 1;
	}

	/** The number a cite call returned, or 0 when it returned none. */
	private static int markerOf(String result) {
		if (result == null) {
			return 0;
		}
		try {
			return Math.max(0, Integer.parseInt(result.trim()));
		} catch (NumberFormatException ex) {
			return 0;
		}
	}
}
