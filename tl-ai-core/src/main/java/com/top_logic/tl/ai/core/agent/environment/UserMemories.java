/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.MemoryKind;
import com.top_logic.tl.ai.core.execution.model.UserMemory;

/**
 * What the agent already knows about the account a run acts for.
 *
 * <p>
 * The newest memories of that account are rendered into the environment block, so an agent opens
 * every conversation knowing what the user told it in the ones before. What does not fit the cap is
 * still stored and still findable with the {@code recall} tool.
 * </p>
 *
 * <h2>Why it is a snapshot</h2>
 *
 * <p>
 * The block sits in front of every request of a conversation and the providers' prompt caches key
 * on it, so it must not change while the conversation runs - see {@link RunEnvironment}. A memory
 * written in turn 3 must therefore not appear in turn 4's block; it appears in the next
 * conversation. The entry point of a conversation binds the list it read at its start through
 * {@link UserMemoryBinding}, and every run of that conversation renders that same list. A run that
 * binds none - a headless one, a scheduled one, each of which is a conversation of its own - reads
 * the store directly.
 * </p>
 *
 * @see UserMemorySettings
 */
public class UserMemories {

	/**
	 * One memory, reduced to what the model is told about it.
	 *
	 * <p>
	 * Not the {@link UserMemory} wrapper: the snapshot outlives the request that read it and is
	 * rendered on a background thread, where a wrapper would be dereferenced against another
	 * revision of the database.
	 * </p>
	 *
	 * @param kind
	 *        What kind of thing is kept; may be {@code null} for a row that carries none.
	 * @param text
	 *        What is kept; never {@code null}.
	 */
	public record Entry(MemoryKind kind, String text) {

		/** An {@link Entry} for the given row, or {@code null} when the row has no text. */
		public static Entry of(UserMemory memory) {
			String text = memory == null ? null : memory.getText();
			if (text == null || text.isBlank()) {
				return null;
			}
			return new Entry(AgentExecutionStore.memoryKindOf(memory), text.strip());
		}
	}

	private UserMemories() {
		// Static utility.
	}

	/**
	 * The memories this run is to render: the conversation's snapshot when one is bound, otherwise
	 * what the store holds for the account right now.
	 *
	 * @param account
	 *        The account the run acts for; {@code null} yields an empty list.
	 */
	public static List<Entry> forRun(Person account) {
		if (!UserMemorySettings.enabled()) {
			return List.of();
		}
		List<Entry> bound = UserMemoryBinding.snapshot();
		return bound != null ? bound : load(account);
	}

	/**
	 * The newest memories of the given account, capped by
	 * {@link UserMemorySettings#maxLoaded()}, newest first.
	 *
	 * <p>
	 * Answers with an empty list rather than failing when the store is not running or the model has
	 * no such type yet: the block is prompt text, and no run should fail because it could not be
	 * looked up.
	 * </p>
	 */
	public static List<Entry> load(Person account) {
		if (account == null || !UserMemorySettings.enabled()
				|| !AgentExecutionStore.Module.INSTANCE.isActive()) {
			return List.of();
		}
		try {
			List<UserMemory> rows =
				AgentExecutionStore.getInstance().memoriesOf(account, UserMemorySettings.maxLoaded());
			List<Entry> entries = new ArrayList<>(rows.size());
			for (UserMemory row : rows) {
				Entry entry = Entry.of(row);
				if (entry != null) {
					entries.add(entry);
				}
			}
			return List.copyOf(entries);
		} catch (RuntimeException ex) {
			Logger.debug("Failed to read the acting account's memories; "
				+ "the environment block carries none.", ex, UserMemories.class);
			return List.of();
		}
	}

	/**
	 * The {@code {{memories}}} section: the heading and one line per memory, or nothing when there
	 * is none.
	 *
	 * <p>
	 * Each line names its kind, because the three are acted on differently: a preference is
	 * honoured, a fact is used, a piece of feedback is a correction to apply again. A text is put on
	 * one line - the block is read line by line, and a second line would read as a memory of its
	 * own.
	 * </p>
	 *
	 * @param entries
	 *        What to render; {@code null} or empty yields the empty string.
	 * @param heading
	 *        The section heading, already in the account's language.
	 * @param label
	 *        Resolves a kind to its label in the account's language.
	 */
	static String section(List<Entry> entries, String heading, Function<MemoryKind, String> label) {
		if (entries == null || entries.isEmpty()) {
			return "";
		}
		List<String> lines = new ArrayList<>(entries.size());
		for (Entry entry : entries) {
			String text = oneLine(entry.text());
			if (text.isEmpty()) {
				continue;
			}
			String kind = entry.kind() == null ? null : label.apply(entry.kind());
			lines.add("- " + (kind == null || kind.isBlank() ? "" : "[" + kind + "] ") + text);
		}
		if (lines.isEmpty()) {
			return "";
		}
		return heading + "\n" + String.join("\n", lines);
	}

	/** Collapses every run of whitespace, so one memory occupies exactly one line. */
	private static String oneLine(String text) {
		return text == null ? "" : text.replaceAll("\\s+", " ").strip();
	}

	/** The resource key naming the given kind to the model. */
	static ResKey labelKey(MemoryKind kind) {
		return switch (kind) {
			case PREFERENCE -> I18NConstants.MEMORY_KIND_PREFERENCE;
			case FACT -> I18NConstants.MEMORY_KIND_FACT;
			case FEEDBACK -> I18NConstants.MEMORY_KIND_FEEDBACK;
		};
	}
}
