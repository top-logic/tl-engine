/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.io.IOException;
import java.util.List;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

import com.top_logic.basic.StringServices;

/**
 * One piece of news a background subagent brought its lead: it ended, or it asks something.
 *
 * <p>
 * What a chat says about a lead turn that no message of the user started - who reported back and
 * why - and links to that subagent's card. Plain data, written into the lead's log as JSON so a
 * reloaded chat says the same.
 * </p>
 *
 * @param subagentId
 *        The subagent's id within its chat, e.g. {@code "sa-2"}.
 * @param agentName
 *        The agent it runs.
 * @param reason
 *        Why it reported back.
 * @param parentToolCallId
 *        The id of the lead's tool call that started it - where its card stands; empty if unknown.
 * @param question
 *        For {@link Reason#ASKS}, the start of what it asks; empty otherwise.
 */
public record SubagentNews(String subagentId, String agentName, Reason reason, String parentToolCallId,
		String question) {

	/** How much of a question the news keeps: it is a line in the chat, not the question itself. */
	static final int QUESTION_CHARS = 200;

	/** Why a subagent reported back. */
	public enum Reason {
		/** It finished its task. */
		FINISHED,
		/** It stopped in order at a limit - turns, budget or output; its answer may be incomplete. */
		LIMIT,
		/** It ended with an error. */
		FAILED,
		/** It was stopped; its result is handed over with the other news. */
		CANCELLED,
		/** It asks the lead something through {@code ask_lead}. */
		ASKS;
	}

	/** The news of a job that has ended. */
	public static SubagentNews ended(SubagentJob job) {
		Reason reason = switch (job.status()) {
			case FAILED -> Reason.FAILED;
			case CANCELLED -> Reason.CANCELLED;
			default -> stoppedAtALimit(job) ? Reason.LIMIT : Reason.FINISHED;
		};
		return new SubagentNews(job.subagentId(), StringServices.nonNull(job.targetAgentName()), reason,
			StringServices.nonNull(job.parentToolCallId()), "");
	}

	private static boolean stoppedAtALimit(SubagentJob job) {
		SubagentResult result = job.result();
		return result != null && result.stoppedAtLimit();
	}

	/** The news of a job that asks its lead something. */
	public static SubagentNews asking(SubagentJob job) {
		LeadQuestion asked = job.leadQuestion();
		String question = asked == null ? "" : StringServices.nonNull(asked.question()).strip();
		if (question.length() > QUESTION_CHARS) {
			question = question.substring(0, QUESTION_CHARS) + "...";
		}
		return new SubagentNews(job.subagentId(), StringServices.nonNull(job.targetAgentName()), Reason.ASKS,
			StringServices.nonNull(job.parentToolCallId()), question);
	}

	/** The given news as a JSON array. */
	public static String toJson(List<SubagentNews> news) {
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			json.beginArray();
			for (SubagentNews item : news) {
				json.beginObject();
				json.name("subagentId").value(item.subagentId());
				json.name("agentName").value(item.agentName());
				json.name("reason").value(item.reason().name());
				json.name("parentToolCallId").value(item.parentToolCallId());
				json.name("question").value(item.question());
				json.endObject();
			}
			json.endArray();
		} catch (IOException ex) {
			throw new RuntimeException("Cannot write subagent news.", ex);
		}
		return buffer.toString();
	}
}
