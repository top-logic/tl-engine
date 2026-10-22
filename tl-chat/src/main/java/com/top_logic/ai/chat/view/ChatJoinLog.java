/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.thread.ThreadContextManager;

/**
 * Which conversation every chat element of one rendered window joined, written to the log when the
 * render is done.
 *
 * <p>
 * Elements show one conversation only when their {@code chat-id} matches, and a mismatch has no
 * symptom a user can read: the element renders, it is simply empty, and it stays empty forever.
 * This is the only place where every {@code (window, chat-id)} pair of one render is visible at
 * once, so it is where a mismatch can be named. The tally lives on the {@link InteractionContext},
 * which is exactly one render pass; when the interaction leaves its thread the tally is logged and
 * dropped.
 * </p>
 *
 * <p>
 * It used to be where the <b>one conversation per window</b> rule was checked as well, because
 * every {@code /chat/*} call carried a {@code windowId} and nothing else and two chat ids that each
 * showed a transcript in one window then took each other's turns. Every call now names its
 * conversation and the server holds a tracker for each (Gitea #502), so that is an ordinary page
 * and this log says nothing about it. What remains is the spelling check below, which is about an
 * element that joined nobody.
 * </p>
 *
 * <p>
 * The rule for the warning is deliberately narrow, so that a correct page is silent: a window with
 * a single chat id never warns, however many elements joined it. A second id is what makes a lone
 * element suspicious, and a lone element is what a typo produces. Two genuinely separate
 * single-element chats on one page trip the same rule; the message says what to check and costs a
 * line in the log.
 * </p>
 */
final class ChatJoinLog {

	private static final Property<ChatJoinLog> LOG =
		TypedAnnotatable.property(ChatJoinLog.class, "tl.chat.view.joins");

	/** Where a finished render reports to. Tests replace this to read what was said. */
	static Consumer<Message> sink = ChatJoinLog::write;

	/**
	 * One line of the diagnostic.
	 *
	 * @param warning
	 *        Whether this line reports a suspected mismatch rather than the plain tally.
	 * @param text
	 *        What to write to the log.
	 */
	record Message(boolean warning, String text) {
		// Sole record.
	}

	/** Window name, then chat id, then the element tags that joined it, in creation order. */
	private final Map<String, Map<String, List<String>>> _windows = new LinkedHashMap<>();

	/**
	 * Records that an element joined a conversation.
	 *
	 * @param windowId
	 *        The window the element was rendered in.
	 * @param chatId
	 *        The resolved conversation the element joined.
	 * @param element
	 *        The element's tag, e.g. {@code chat-rail}, for the message.
	 */
	void join(String windowId, String chatId, String element) {
		_windows
			.computeIfAbsent(windowId == null ? "" : windowId, window -> new LinkedHashMap<>())
			.computeIfAbsent(chatId, chat -> new ArrayList<>())
			.add(element);
	}


	/**
	 * What this render has to say, the tally of each window first and a suspected mismatch after
	 * it.
	 */
	List<Message> messages() {
		List<Message> result = new ArrayList<>();
		for (Map.Entry<String, Map<String, List<String>>> window : _windows.entrySet()) {
			Map<String, List<String>> joins = window.getValue();
			if (joins.isEmpty()) {
				continue;
			}
			String where = " in window '" + window.getKey() + "'";
			result.add(new Message(false, "Chat elements" + where + ": " + counts(joins) + "."));

			List<String> alone = joins.entrySet().stream()
				.filter(join -> join.getValue().size() == 1)
				.map(join -> "<" + join.getValue().get(0) + " chat-id='" + join.getKey() + "'>")
				.collect(Collectors.toList());
			if (joins.size() < 2 || alone.isEmpty()) {
				continue;
			}
			Map<String, List<String>> shared = joins.entrySet().stream()
				.filter(join -> join.getValue().size() > 1)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (l, r) -> l, LinkedHashMap::new));
			String others = shared.isEmpty()
				? "No other chat id" + where + " holds more than one element."
				: "The chat ids joined by several elements" + where + " are: " + counts(shared) + ".";
			result.add(new Message(true, String.join(", ", alone) + " joins no other chat element" + where + ". "
				+ others
				+ " Elements show one conversation only when their chat-id matches."
				+ " Check the spelling, or put the elements into one <chat chat-id='...'>"
				+ " and drop the attribute from the elements inside."));
		}
		return result;
	}

	/**
	 * Records a join on the current render, starting the tally and arranging for it to be logged if
	 * this is the first one.
	 *
	 * <p>
	 * Silently does nothing outside an interaction, which is how a unit test that builds a control
	 * without a request behaves.
	 * </p>
	 */
	static void record(String windowId, String chatId, String element) {
		InteractionContext interaction = ThreadContextManager.getInteraction();
		if (interaction == null) {
			return;
		}
		ChatJoinLog log = interaction.get(LOG);
		if (log == null) {
			log = new ChatJoinLog();
			interaction.set(LOG, log);
			ChatJoinLog started = log;
			interaction.addUnboundListener(context -> started.report());
		}
		log.join(windowId, chatId, element);
	}

	private void report() {
		messages().forEach(sink);
	}

	private static void write(Message message) {
		if (message.warning()) {
			Logger.warn(message.text(), ChatJoinLog.class);
		} else {
			Logger.info(message.text(), ChatJoinLog.class);
		}
	}

	private static String counts(Map<String, List<String>> joins) {
		return joins.entrySet().stream()
			.map(join -> "'" + join.getKey() + "' (" + join.getValue().size() + ")")
			.collect(Collectors.joining(", "));
	}
}
