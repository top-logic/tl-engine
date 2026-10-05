/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session.persist;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedConversationV2;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedPart;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Closes the transcript of a run the process ended in the middle of (#598).
 *
 * <p>
 * A running run's transcript is a checkpoint: what the turn had recorded when the run last reached
 * a message boundary - a tool call, an approval, the end of an agent-loop iteration. When the
 * process ends, that checkpoint is all that is left of the turn, and it is still written as a turn
 * in progress. Shown as it is, it would offer an approval card nobody can answer and a tool call
 * that runs forever, and report writes as done that the process took down with it.
 * </p>
 *
 * <p>
 * {@link #close(String)} rewrites it as a turn that ended:
 * </p>
 * <ul>
 * <li>an approval still waiting for a decision gets
 * {@link com.top_logic.tl.ai.core.session.Part.ApprovalDecision#INTERRUPTED};</li>
 * <li>a tool call still running fails, with a sentence saying the result is unknown;</li>
 * <li>when the turn's KB transaction was still open, every write it reported as done is marked
 * {@link com.top_logic.tl.ai.core.session.Part.ToolCallStatus#ROLLED_BACK} and the rollback is
 * recorded, exactly as a live rollback is - the transaction never reached its commit;</li>
 * <li>the last turn ends with a
 * {@link com.top_logic.tl.ai.core.session.Part.SystemEventType#INTERRUPTED} part.</li>
 * </ul>
 *
 * <p>
 * Everything else - the user's message, what the agent said and did before it stopped, every id -
 * is kept exactly as it was recorded, because it is what the user saw.
 * </p>
 */
public final class InterruptedTranscript {

	/**
	 * What a tool call that was running when the process ended failed with.
	 *
	 * <p>
	 * Read by the model when the conversation is continued, so it is an English literal: the call's
	 * outcome is unknown, and the model must not rely on anything it may have done.
	 * </p>
	 */
	public static final String INTERRUPTED_CALL =
		"The server stopped while this call was running. Its result is unknown.";

	/**
	 * Why the writes of a turn whose transaction was still open are rolled back.
	 */
	public static final String INTERRUPTED_ROLLBACK =
		"The server stopped before the changes of this turn were saved.";

	/**
	 * The details of the {@link com.top_logic.tl.ai.core.session.Part.SystemEventType#INTERRUPTED}
	 * part, for an operator reading the transcript; the chat phrases its own notice in the reader's
	 * language.
	 */
	public static final String INTERRUPTED_DETAILS =
		"The server stopped before this run finished; no agent loop survives a restart.";

	private InterruptedTranscript() {
		// Static utility
	}

	/**
	 * The given run transcript, closed as a run the process ended.
	 *
	 * @param transcript
	 *        A run's serialized turns, as {@link ConversationLogSerializer#toJson(java.util.List)}
	 *        writes them; <code>null</code> or empty when the run recorded none.
	 * @return The closed transcript, or {@code transcript} itself when it holds no turn.
	 * @throws IOException
	 *         When the transcript cannot be read.
	 */
	public static String close(String transcript) throws IOException {
		if (transcript == null || transcript.isEmpty()) {
			return transcript;
		}
		PersistedConversationV2 conversation =
			PersistedConversationV2.readPersistedConversationV2(new JsonReader(new StringR(transcript)));
		List<PersistedTurn> turns = conversation.getTurns();
		if (turns.isEmpty()) {
			return transcript;
		}
		for (int i = 0; i < turns.size(); i++) {
			closeTurn(turns.get(i), i == turns.size() - 1);
		}
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			conversation.writeTo(json);
		}
		return buffer.toString();
	}

	private static void closeTurn(PersistedTurn turn, boolean last) {
		List<Part> parts = new ArrayList<>();
		for (PersistedPart dto : turn.getAssistantParts()) {
			parts.add(ConversationLogSerializer.fromPartDto(dto));
		}

		// Stamped with the turn's last recorded moment rather than with the time of the sweep: the
		// chat measures "worked for" up to the last part, and the turn did not work through the
		// hours the server was down.
		Instant end = Instant.ofEpochMilli(turn.getTimestamp());
		for (Part part : parts) {
			if (part.timestamp().isAfter(end)) {
				end = part.timestamp();
			}
		}
		Instant now = Instant.now();

		for (int i = 0; i < parts.size(); i++) {
			Part part = parts.get(i);
			if (part instanceof Part.ApprovalPart approval && approval.decision() == null) {
				parts.set(i, approval.resolve(Part.ApprovalDecision.INTERRUPTED, null, now));
			} else if (part instanceof Part.ToolCallPart call
				&& call.status() == Part.ToolCallStatus.RUNNING) {
				parts.set(i, call.failed(INTERRUPTED_CALL, Duration.ZERO));
			}
		}

		int openFrom = openTransactionStart(parts);
		if (openFrom >= 0) {
			for (int i = openFrom; i < parts.size(); i++) {
				if (parts.get(i) instanceof Part.ToolCallPart call
					&& call.status() == Part.ToolCallStatus.COMPLETED
					&& (call.mode() == null || call.mode().writesApplicationState())) {
					parts.set(i, call.rolledBack(INTERRUPTED_ROLLBACK));
				}
			}
			parts.add(new Part.TransactionPart(UUID.randomUUID().toString(), end,
				Part.TransactionType.ROLLED_BACK, -1, INTERRUPTED_ROLLBACK));
		}

		if (last) {
			parts.add(new Part.SystemPart(UUID.randomUUID().toString(), end,
				Part.SystemEventType.INTERRUPTED, INTERRUPTED_DETAILS));
		}

		List<PersistedPart> dtos = new ArrayList<>(parts.size());
		for (Part part : parts) {
			dtos.add(ConversationLogSerializer.toPartDto(part));
		}
		turn.setAssistantParts(dtos);
	}

	/**
	 * The index from which the parts belong to a KB transaction that was never closed, or
	 * {@code -1} when the turn's last transaction was committed or rolled back, or there was none.
	 *
	 * <p>
	 * The window starts right after the transaction that was closed before it, not at its own
	 * {@link com.top_logic.tl.ai.core.session.Part.TransactionType#BEGAN} part: the loop opens the
	 * transaction while it executes the first write tool, so that call is recorded <em>before</em>
	 * the part that says the transaction began - and every transaction of a turn is one agent-loop
	 * iteration, so nothing between the previous close and the open one belongs to another.
	 * </p>
	 */
	private static int openTransactionStart(List<Part> parts) {
		boolean open = false;
		for (int i = parts.size() - 1; i >= 0; i--) {
			if (parts.get(i) instanceof Part.TransactionPart tx) {
				if (tx.type() != Part.TransactionType.BEGAN) {
					return open ? i + 1 : -1;
				}
				open = true;
			}
		}
		return open ? 0 : -1;
	}
}
