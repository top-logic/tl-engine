/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.top_logic.basic.util.ResKey;
import com.top_logic.tl.ai.tool.api.Tool;

/**
 * The commit message of the changes an agent made in one turn.
 *
 * <p>
 * What the revision log shows for an agent's change: the agent's name, followed by what the turn
 * did. When the model wrote text alongside its tool calls, its first paragraph says that - it is
 * the model's own account of what it is about to do. Otherwise the {@link Tool#commitMessage(java.util.Map)
 * messages} of the turn's writing tool calls say it, each once, in the order the model called
 * them.
 * </p>
 *
 * <p>
 * The message is a {@link ResKey}, so that a tool's message is shown in the language of whoever
 * reads the log; the model's text is taken over as it is.
 * </p>
 *
 * @see TransactionScope#commit(ResKey)
 */
public final class TurnCommitMessage {

	/**
	 * The longest model text taken over into a commit message, in characters, including the
	 * {@link #ELLIPSIS} of a shortened text.
	 */
	public static final int MAX_TEXT_LENGTH = 200;

	/** What marks a model text that was shortened to {@link #MAX_TEXT_LENGTH}. */
	public static final String ELLIPSIS = "…";

	/** A line break in any of its spellings. */
	private static final Pattern LINE_BREAK = Pattern.compile("\r\n?");

	/** A blank line, which ends a paragraph, in a text whose line breaks are {@code \n}. */
	private static final Pattern PARAGRAPH_BREAK = Pattern.compile("\n\\s*\n");

	/** A run of white space, which is shown as one space. */
	private static final Pattern WHITE_SPACE = Pattern.compile("\\s+");

	private TurnCommitMessage() {
		// Static utility.
	}

	/**
	 * The commit message of one turn.
	 *
	 * @param agentName
	 *        The name of the agent that made the changes, or <code>null</code> when it has none.
	 * @param modelText
	 *        The text the model wrote alongside its tool calls in this turn, or <code>null</code>.
	 * @param toolMessages
	 *        The {@link Tool#commitMessage(java.util.Map) messages} of the turn's writing tool
	 *        calls, in the order the model called them. Used only when the model wrote no text.
	 * @return The message; {@link I18NConstants#AGENT_CHANGE} prefixed with the agent's name when
	 *         neither the model nor a tool said anything.
	 */
	public static ResKey of(String agentName, String modelText, Collection<ResKey> toolMessages) {
		String summary = summary(modelText);
		ResKey description = summary != null ? ResKey.text(summary) : joined(toolMessages);
		if (description == null) {
			description = I18NConstants.AGENT_CHANGE;
		}
		if (agentName == null || agentName.isBlank()) {
			return description;
		}
		return I18NConstants.AGENT_CHANGE__AGENT_DESCRIPTION.fill(agentName.strip(), description);
	}

	/**
	 * The first paragraph of the given text, its white space normalized, shortened to
	 * {@link #MAX_TEXT_LENGTH} characters at a word boundary.
	 *
	 * @return The summary, or <code>null</code> when the text is empty or blank.
	 */
	static String summary(String text) {
		if (text == null) {
			return null;
		}
		String lines = LINE_BREAK.matcher(text).replaceAll("\n");
		for (String paragraph : PARAGRAPH_BREAK.split(lines)) {
			String normalized = WHITE_SPACE.matcher(paragraph).replaceAll(" ").strip();
			if (!normalized.isEmpty()) {
				return shortened(normalized);
			}
		}
		return null;
	}

	private static String shortened(String text) {
		if (text.length() <= MAX_TEXT_LENGTH) {
			return text;
		}
		int limit = MAX_TEXT_LENGTH - ELLIPSIS.length();
		// A space at the limit itself ends the last word that fits.
		int cut = text.lastIndexOf(' ', limit);
		if (cut <= 0) {
			// One word longer than the limit: cut inside it.
			cut = limit;
		}
		return text.substring(0, cut).stripTrailing() + ELLIPSIS;
	}

	/**
	 * The given messages, each once, in their order, as one message; <code>null</code> when there
	 * are none.
	 */
	private static ResKey joined(Collection<ResKey> messages) {
		if (messages == null) {
			return null;
		}
		Set<ResKey> distinct = new LinkedHashSet<>(messages);
		distinct.remove(null);
		ResKey result = null;
		for (ResKey message : distinct) {
			result = result == null ? message : I18NConstants.CHANGES__FIRST_NEXT.fill(result, message);
		}
		return result;
	}

}
