/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.memory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.top_logic.tl.ai.core.agent.environment.UserMemorySettings;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.UserMessage;

/**
 * What the user agreed to in the turn that is running.
 *
 * <p>
 * A memory the user did not agree to is never written, and that is a rule of the tool rather than
 * of the prompt: the {@code remember} tool asks this object, and this object is built from what the
 * <em>user</em> said, not from what the model claims the user said. There are two ways to agree,
 * and the ticket names both:
 * </p>
 *
 * <ul>
 * <li>the user asked for it in their own message of this turn - "remember that ...";</li>
 * <li>the agent asked whether it should remember something, with {@code askUser}, and the user
 * agreed to that question in this turn.</li>
 * </ul>
 *
 * <h2>What this establishes, and what it cannot</h2>
 *
 * <p>
 * It establishes that <em>memory was the subject of this turn and the user was in favour</em>. It
 * cannot establish that the sentence the agent is about to store is the sentence the user meant -
 * no server-side check can read a sentence the way the user meant it. That is the honest limit, and
 * it is why the user also sees every memory in a list and can throw any of them away. What the
 * check does close is the failure the ticket is about: an agent quietly writing memories in a turn
 * where the user said nothing about memory at all.
 * </p>
 *
 * <p>
 * A declined question is a decline, whatever else is in the answer: an answer that carries a
 * negation ("no, that is fine") grants nothing, even when it also carries a word that would
 * otherwise read as agreement. Refusing is the safe direction here.
 * </p>
 *
 * <p>
 * Thread-safe. The run's main thread builds it, a tool thread may record an answer on it, and
 * another tool thread may ask it - see {@link MemoryConsentBinding}.
 * </p>
 */
public class MemoryConsent {

	/** One question the agent asked in this turn and the answer it got. */
	private record Exchange(String question, String answer) {
		// Purely a carrier: the two are only ever read together.
	}

	private final String _userText;

	private final List<Exchange> _exchanges = new CopyOnWriteArrayList<>();

	/**
	 * Creates a consent record for a turn in which the user wrote the given text.
	 *
	 * @param userText
	 *        The user's own message; may be {@code null} for a run started without one.
	 */
	public MemoryConsent(String userText) {
		_userText = userText;
	}

	/**
	 * The consent record of a turn whose user message is the newest one of the given conversation.
	 *
	 * <p>
	 * Only the text parts are read. An attachment is not a sentence, and the framework's own
	 * additions to a conversation - a rollback notice, a delivered subagent result - arrive later,
	 * after the record has been built, so they cannot be mistaken for something the user typed.
	 * </p>
	 */
	public static MemoryConsent forNewestTurn(Conversation conversation) {
		return new MemoryConsent(newestUserText(conversation));
	}

	/**
	 * Records that the user answered a question the agent asked in this turn.
	 *
	 * @param question
	 *        What the agent asked.
	 * @param answer
	 *        What the user answered.
	 */
	public void recordAnswer(String question, String answer) {
		if (question == null && answer == null) {
			return;
		}
		_exchanges.add(new Exchange(question, answer));
	}

	/**
	 * Whether the user agreed, in this turn, to the agent keeping something.
	 */
	public boolean granted() {
		if (askedToRemember(_userText)) {
			return true;
		}
		for (Exchange exchange : _exchanges) {
			if (askedToRemember(exchange.question()) && agreed(exchange.answer())) {
				return true;
			}
		}
		return false;
	}

	/** Whether the given text raises the subject of remembering something. */
	private static boolean askedToRemember(String text) {
		return ConsentWords.uses(text, UserMemorySettings.requestWords());
	}

	/** Whether the given answer is an agreement rather than a refusal. */
	private static boolean agreed(String answer) {
		if (ConsentWords.uses(answer, UserMemorySettings.negativeWords())) {
			return false;
		}
		return ConsentWords.uses(answer, UserMemorySettings.affirmativeWords());
	}

	/** The user's own text of this turn, for diagnostics; may be {@code null}. */
	public String userText() {
		return _userText;
	}

	private static String newestUserText(Conversation conversation) {
		if (conversation == null) {
			return null;
		}
		List<Message> messages = conversation.messages();
		for (int i = messages.size() - 1; i >= 0; i--) {
			if (messages.get(i) instanceof UserMessage user) {
				StringBuilder text = new StringBuilder();
				user.content().stream()
					.filter(TextContent.class::isInstance)
					.map(TextContent.class::cast)
					.forEach(part -> {
						if (text.length() > 0) {
							text.append('\n');
						}
						text.append(part.text());
					});
				return text.toString();
			}
		}
		return null;
	}
}
