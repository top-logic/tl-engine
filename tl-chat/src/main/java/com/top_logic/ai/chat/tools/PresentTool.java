/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.tools;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.ai.chat.view.AgentViewRefreshService;
import com.top_logic.ai.chat.view.AgentViewRefreshService.WindowAccess;
import com.top_logic.ai.chat.view.ChatViewBinding;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.tools.model.TidResolver;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.util.TLContextManager;

/**
 * Puts model objects onto a channel of the page the chat is on, so that the page shows them.
 *
 * <p>
 * The agent points at data; the page decides how to render it. What the value on the channel looks
 * like is entirely the application's business - a {@code <chat-rail>}'s pane, a {@code <form>}, a
 * {@code <data-list>}, whatever reads that channel. This tool creates no user interface and knows
 * about none.
 * </p>
 *
 * <h2>What bounds it</h2>
 *
 * <ul>
 * <li><b>The channel.</b> Deny by default: only a channel a {@code <chat-conversation present="…">}
 * of this window named can be written, and any other name is refused with the list of the ones that
 * exist. See {@link ChatViewBinding#presentable(com.top_logic.basic.col.TypedAnnotatable, String)}.</li>
 * <li><b>The objects.</b> Every id is resolved with the acting user's rights
 * ({@link TidResolver}), so a run cannot put an object on a page that its user may not see - and an
 * object they may not see is reported exactly like one that does not exist.</li>
 * <li><b>The window.</b> The value is written in the sub-session of the browser window the run
 * belongs to and under that window's request lock, the way
 * {@link AgentViewRefreshService} reaches a window from the agent thread. A run without a window -
 * headless, TL-Script, the scheduler - has no channel at all, says so and does nothing.</li>
 * </ul>
 *
 * <p>
 * All or nothing: one unreadable or unknown id refuses the whole call. A page that showed three of
 * four objects the model asked for would be a page that quietly lies about what the user is looking
 * at.
 * </p>
 *
 * <p>
 * Classified {@link ToolMode#READ} in {@link ToolGroups#INTERACTION}: it reads business data and
 * changes page state, not the knowledge base, so there is nothing for a user to approve - and the
 * approval dialog it would otherwise raise sits in the very page this is trying to fill.
 * </p>
 */
public class PresentTool {

	/**
	 * Creates a {@link PresentTool}.
	 */
	public PresentTool() {
		// Required public no-arg constructor for ReflectiveToolFactory.
	}

	/**
	 * Shows the named objects in the named part of the page.
	 *
	 * @param ids
	 *        The objects to show, by the identifier a tool reported for them. One id shows one
	 *        object, several show a list.
	 * @param channel
	 *        The name of the page channel to write.
	 * @param context
	 *        The run, supplying the rights every id is resolved with.
	 * @return One sentence saying what the page now shows, or why nothing happened.
	 */
	@ToolMethod(
		description = "Show model objects to the user in the page beside this conversation. Use it "
			+ "whenever the user asks to see, show, open or display something you have found: pass "
			+ "the object's id and the page channel, and the page renders it. Pass several ids to "
			+ "show a list. Ids are the ones tool results carry as [id:...]. The channel must be one "
			+ "the page offers - if you do not know it, call this once and the refusal names them. "
			+ "This changes no data; it only changes what the user is looking at. It is not a "
			+ "substitute for answering: say in your reply what you put there.",
		mode = ToolMode.READ, group = ToolGroups.INTERACTION)
	public String present(
			@P("Ids of the objects to show, e.g. \"GenericObject:5371\". One shows one object, several show a list.") List<String> ids,
			@P("Name of the page channel to write, e.g. \"paneObject\".") String channel,
			ToolContext context) {
		String name = channel == null ? "" : channel.trim();
		if (name.isEmpty()) {
			return "Error: no channel named. Pass the name of the page channel to write.";
		}
		List<String> wanted = cleaned(ids);
		if (wanted.isEmpty()) {
			return "Error: no ids given. Pass at least one object id.";
		}

		TLSubSessionContext window = TLContextManager.getSubSession();
		if (!AgentViewRefreshService.hasWindow(window)) {
			return "No page is attached to this run, so there is nothing to present on. "
				+ "Answer in text instead.";
		}
		Set<String> allowed = ChatViewBinding.presentableNames(window);
		ViewChannel target = ChatViewBinding.presentable(window, name);
		if (target == null) {
			return allowed.isEmpty()
				? "This page offers no channel to present on. Answer in text instead."
				: "Channel '" + name + "' cannot be written here. This page offers: "
					+ String.join(", ", allowed) + ".";
		}

		// Duplicates drop out here rather than among the ids: the same object can be named twice
		// in two spellings, and a list showing it twice would be the page saying something the
		// model did not.
		Set<TLObject> objects = new LinkedHashSet<>();
		for (String id : wanted) {
			try {
				// A read check, with the acting user's rights: an object they may not see is
				// reported exactly like one that does not exist.
				objects.add(TidResolver.resolve(id, ReadFilter.of(context).security()));
			} catch (IllegalArgumentException ex) {
				// The resolver's message is a whole sentence that says which kind of wrong
				// identifier this was (TidDiagnostics); it is passed on as it stands.
				return "Nothing was presented: " + ex.getMessage();
			}
		}

		// One object stands on the channel as itself, several as a collection: an element bound to
		// the channel distinguishes the two, and wrapping a single object in a list would make
		// "the invoice" and "a list of one invoice" the same thing.
		Object value = objects.size() == 1 ? objects.iterator().next() : List.copyOf(objects);
		WindowAccess access = AgentViewRefreshService.inWindow(window, () -> target.set(value));
		switch (access) {
			case DONE:
				Logger.debug("Presented " + objects.size() + " object(s) on channel '" + name + "'.",
					PresentTool.class);
				return objects.size() == 1
					? "The page now shows that object."
					: "The page now shows those " + objects.size() + " objects.";
			case BUSY:
				return "The page was busy and could not be written. Try once more.";
			default:
				return "No page is attached to this run, so there is nothing to present on. "
					+ "Answer in text instead.";
		}
	}

	/** The given ids without the blank ones, in the order they were named. */
	private static List<String> cleaned(List<String> ids) {
		if (ids == null) {
			return List.of();
		}
		List<String> result = new ArrayList<>(ids.size());
		for (String id : ids) {
			if (id != null && !id.isBlank()) {
				result.add(id.trim());
			}
		}
		return result;
	}
}
