/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.memory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.environment.UserMemorySettings;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.MemoryKind;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.UserMemory;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.util.TLContext;

/**
 * What an agent may keep about the user it is working for, and how it reads it back.
 *
 * <p>
 * {@code remember} writes one memory, {@code recall} searches the ones that did not fit into the
 * environment block. Both act for the acting account and only for it: a memory is written with that
 * account as its owner and read back with that account as the filter, so no agent of one user ever
 * reaches another user's memories.
 * </p>
 *
 * <h2>The rule this tool enforces rather than states</h2>
 *
 * <p>
 * {@code remember} refuses a memory the user did not agree to <em>in the turn it is called in</em>.
 * The agreement is established from what the user wrote and from what they answered to an
 * {@code askUser} question - see {@link MemoryConsent} - and the check is a Java check on the call
 * path, not a line in a prompt. An agent that decides on its own that something is worth keeping
 * gets a refusal telling it to ask first.
 * </p>
 */
public class UserMemoryTool {

	/**
	 * The simple names of the tools this class defines.
	 *
	 * <p>
	 * What {@code AgentFactory.isOffered} matches an agent's tool against, so a switched-off
	 * installation offers neither of them however an agent definition names them.
	 * </p>
	 */
	public static final Set<String> MEMORY_TOOLS = Set.of("remember", "recall");

	/**
	 * What {@code remember} answers when the installation keeps no memories at all.
	 *
	 * <p>
	 * A switched-off installation offers neither tool, so this is the answer to a call that reached
	 * the tool some other way - a catalog built before the switch was read, a direct invocation.
	 * </p>
	 */
	static final String DISABLED =
		"error: This application does not keep memories about its users. Do not try again; "
			+ "answer the user's request without storing anything.";

	/** What {@code remember} answers when nobody agreed to anything in this turn. */
	static final String NO_CONSENT =
		"error: Nothing was stored, because the user has not agreed to it in this turn. "
			+ "Never store something the user did not ask for. If you believe this is worth "
			+ "keeping, ask first with the askUser tool - for example \"Shall I remember that "
			+ "you prefer short answers?\" - and call remember again only after the user agreed "
			+ "in their answer.";

	/**
	 * Keeps one thing about the user, once the user has agreed to it in this turn.
	 *
	 * @param kind
	 *        {@code preference}, {@code fact} or {@code feedback}.
	 * @param text
	 *        What to keep, as one sentence about the user.
	 * @return What was stored, or an error string the model can act on.
	 */
	@ToolMethod(description = "Keep one thing about the current user across conversations, so a later "
		+ "conversation starts knowing it. ONLY call this when the user asked you to remember "
		+ "something in this turn, or when you asked with askUser whether you should remember it and "
		+ "the user agreed. NEVER call it on your own judgement: a call without the user's agreement "
		+ "in the same turn is refused. Store one short sentence about the user, not a summary of the "
		+ "conversation.",
		mode = ToolMode.USER_SCOPED, group = ToolGroups.WORKSPACE)
	public String remember(
			@P("What kind of thing this is: 'preference' (how the user wants to be served), "
				+ "'fact' (something that is the case about the user or their work) or "
				+ "'feedback' (a judgement the user passed on the work)") String kind,
			@P("What to keep, as one short sentence about the user, in the user's language") String text,
			ToolExecutionContext context) {

		if (!UserMemorySettings.enabled()) {
			return DISABLED;
		}
		MemoryKind resolvedKind = MemoryKind.byName(kind);
		if (resolvedKind == null) {
			return "error: Unknown kind '" + kind + "'. Use one of: preference, fact, feedback.";
		}
		if (text == null || text.isBlank()) {
			return "error: Nothing to remember - 'text' is empty.";
		}
		String trimmed = text.strip();
		int maxLength = UserMemorySettings.maxLength();
		if (trimmed.length() > maxLength) {
			return "error: The text is " + trimmed.length() + " characters long; at most " + maxLength
				+ " are allowed. A memory is one sentence about the user, not a summary. "
				+ "Shorten it and try again.";
		}
		if (!MemoryConsentBinding.granted()) {
			return NO_CONSENT;
		}
		Person owner = actingAccount();
		if (owner == null) {
			return "error: This run has no user account to keep a memory for.";
		}
		if (!AgentExecutionStore.Module.INSTANCE.isActive()) {
			return "error: Memories cannot be stored right now. Tell the user and carry on.";
		}
		try {
			// The execution wrapper is handed over unread: the store dereferences it on its writer
			// thread, which sees HEAD. Reading it here would fail on a run whose execution row the
			// writer created after this thread's view of the database was fixed.
			AgentExecutionStore.getInstance()
				.createMemory(owner, resolvedKind, trimmed, currentExecution(context));
			return "Stored as a " + resolvedKind.name().toLowerCase(Locale.ROOT) + ": " + trimmed
				+ "\nIt will be part of what you are told at the start of the next conversation. "
				+ "The user can read it and delete it in their memory list.";
		} catch (RuntimeException ex) {
			Logger.error("Failed to store a user memory: " + ex.getMessage(), ex, UserMemoryTool.class);
			return "error: The memory could not be stored: " + ex.getMessage();
		}
	}

	/**
	 * Searches the memories of the acting account.
	 *
	 * @param query
	 *        The words to look for; empty returns the newest memories.
	 * @return The matching memories, newest first.
	 */
	@ToolMethod(description = "Search what you have kept about the current user. The newest memories "
		+ "are already part of what you were told at the start of this conversation, so use this only "
		+ "to look further back - for example when the user refers to something they told you a long "
		+ "time ago. An empty query returns the newest memories.",
		mode = ToolMode.READ, group = ToolGroups.WORKSPACE)
	public String recall(
			@P("The words to look for; leave empty for the newest memories") String query,
			ToolExecutionContext context) {

		if (!UserMemorySettings.enabled()) {
			return DISABLED;
		}
		Person owner = actingAccount();
		if (owner == null || !AgentExecutionStore.Module.INSTANCE.isActive()) {
			return "No memories are available for this run.";
		}
		List<UserMemory> all = AgentExecutionStore.getInstance().memoriesOf(owner, 0);
		List<Scored> matches = rank(all, query);
		if (matches.isEmpty()) {
			return query == null || query.isBlank()
				? "Nothing has been kept about this user yet."
				: "Nothing kept about this user matches '" + query.strip() + "'.";
		}
		StringBuilder result = new StringBuilder();
		result.append("Kept about this user").append(
			query == null || query.isBlank() ? "" : ", matching '" + query.strip() + "'").append(":");
		for (Scored match : matches) {
			MemoryKind kind = AgentExecutionStore.memoryKindOf(match.memory());
			result.append("\n- [")
				.append(kind == null ? "memory" : kind.name().toLowerCase(Locale.ROOT))
				.append("] ")
				.append(match.memory().getText().replaceAll("\\s+", " ").strip());
		}
		return result.toString();
	}

	/** How many memories one {@code recall} answers with. */
	static final int RECALL_LIMIT = 20;

	/** One memory and how well it answers the query. */
	private record Scored(UserMemory memory, int score) {
		// Purely a carrier for the sort below.
	}

	/**
	 * The memories that answer the query, best first.
	 *
	 * <p>
	 * A word-count match, deliberately: it is the behaviour a reader can predict from the query,
	 * and it is what the vector index is meant to replace later. A memory scores one per query word
	 * it contains, and ties keep the newest first because {@code memoriesOf} already sorted them
	 * that way.
	 * </p>
	 */
	static List<Scored> rank(List<UserMemory> memories, String query) {
		List<String> words = queryWords(query);
		List<Scored> scored = new ArrayList<>();
		for (UserMemory memory : memories) {
			String text = memory.getText();
			if (text == null || text.isBlank()) {
				continue;
			}
			if (words.isEmpty()) {
				scored.add(new Scored(memory, 0));
				continue;
			}
			int score = 0;
			for (String word : words) {
				if (ConsentWords.uses(text, List.of(word))) {
					score++;
				}
			}
			if (score > 0) {
				scored.add(new Scored(memory, score));
			}
		}
		// A stable sort, so equal scores keep the newest-first order memoriesOf produced.
		scored.sort(Comparator.comparingInt(Scored::score).reversed());
		return scored.size() > RECALL_LIMIT ? List.copyOf(scored.subList(0, RECALL_LIMIT))
			: List.copyOf(scored);
	}

	/** The query, split into the words a memory is matched against. */
	private static List<String> queryWords(String query) {
		if (query == null || query.isBlank()) {
			return List.of();
		}
		List<String> words = new ArrayList<>();
		for (String word : query.strip().split("\\s+")) {
			if (word.length() > 2) {
				words.add(word);
			}
		}
		// A query of nothing but short words is still a query: fall back to the whole of it rather
		// than answering with the newest memories, which is what an empty word list means.
		return words.isEmpty() ? List.of(query.strip()) : List.copyOf(words);
	}

	/**
	 * The account this run acts for.
	 *
	 * <p>
	 * The bound sub-session, the way every other tool that needs an account reads it: the agent
	 * loop propagates the caller's sub-session onto its worker threads, so a parallel tool call is
	 * in the same context as a serial one.
	 * </p>
	 */
	private static Person actingAccount() {
		TLContext context = TLContext.getContext();
		return context == null ? null : context.getPerson();
	}

	/**
	 * The recorded execution of the current run, or {@code null} when the run is not recorded - in
	 * which case the memory simply records no origin.
	 */
	private static AgentExecution currentExecution(ToolExecutionContext context) {
		return context instanceof ToolContext toolContext ? toolContext.executionHandle().get() : null;
	}
}
