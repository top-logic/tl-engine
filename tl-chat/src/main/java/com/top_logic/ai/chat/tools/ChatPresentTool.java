/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.tools;

import java.util.Set;

import com.top_logic.ai.chat.view.ChatViewBinding;
import com.top_logic.ai.chat.view.ChatWindowService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.layout.react.scripting.WindowAccess;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.tools.present.PresentAs;
import com.top_logic.tl.ai.core.tools.present.PresentTool;
import com.top_logic.tl.ai.core.tools.present.Presentation;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.util.TLContextManager;

/**
 * The {@code present} tool of a chat page: puts what the agent shows onto a channel of the page the
 * chat is on.
 *
 * <p>
 * What stands on the channel afterwards is a form's object for a {@link PresentAs#FORM form} - so
 * every element of the page that reads the channel as "the open object" goes on working - and the
 * {@link Presentation} itself for a table or a chart. A {@code <chat-rail input="…">} renders all of
 * them; see {@code PresentationPane}.
 * </p>
 *
 * <h2>What bounds it</h2>
 *
 * <ul>
 * <li><b>The channel.</b> Deny by default: only a channel a {@code <chat-conversation present="…">}
 * of this window named can be written, and only while that element is displayed. The model need not
 * know its name - a page offering exactly one is written without one - and a name the page does not
 * offer is refused with the list of the ones it does. See
 * {@link ChatViewBinding#presentable(com.top_logic.basic.col.TypedAnnotatable, String)}.</li>
 * <li><b>The window.</b> The value is written in the sub-session of the browser window the run
 * belongs to and within that window's interaction, through
 * {@link ChatWindowService#inWindow(TLSubSessionContext, Runnable)}. A run without a window -
 * headless, TL-Script, the scheduler - has no channel at all, says so and does nothing.</li>
 * </ul>
 *
 * <p>
 * A run that lacks either is not offered the tool ({@link #noPlaceToShow(ToolExecutionContext)}): its
 * model is not told to show anything, and answers a request to see objects in text rather than with
 * a call that can only be refused (#649). The same answer refuses a call that arrives anyway - the
 * element can go away between the request and the call.
 * </p>
 *
 * <p>
 * The objects and their rights are the base class's business.
 * </p>
 */
public class ChatPresentTool extends PresentTool {

	/**
	 * The answer for a run without a browser window: a fact about the run, and deliberately not the
	 * sentence a page that allows nothing is answered with.
	 */
	private static final String NO_PAGE =
		"No page is attached to this run, so there is nothing to present on. Answer in text instead.";

	/** The answer for a run whose page shows no element that names a channel to present on. */
	private static final String NO_PLACE = "This page offers no place to present on. Answer in text instead.";

	/**
	 * Creates a {@link ChatPresentTool}.
	 */
	public ChatPresentTool() {
		// Required public no-arg constructor for ReflectiveToolFactory.
	}

	/**
	 * Why this run has no place to show anything: no window, or a window none of whose displayed
	 * chats names a channel.
	 */
	@Override
	protected String noPlaceToShow(ToolExecutionContext context) {
		TLSubSessionContext window = TLContextManager.getSubSession();
		if (!ChatWindowService.hasWindow(window)) {
			return NO_PAGE;
		}
		if (ChatViewBinding.presentableNames(window).isEmpty()) {
			return NO_PLACE;
		}
		return null;
	}

	@Override
	protected String deliver(Presentation presentation, String channel, ToolContext context) {
		TLSubSessionContext window = TLContextManager.getSubSession();
		if (!ChatWindowService.hasWindow(window)) {
			return NO_PAGE;
		}
		// Read once, and asked again rather than taken from the offer: the elements of a page come and
		// go while the run works.
		Set<String> allowed = ChatViewBinding.presentableNames(window);
		if (allowed.isEmpty()) {
			return NO_PLACE;
		}
		String name = channel;
		if (name.isEmpty()) {
			if (allowed.size() > 1) {
				return "Nothing was presented: this page offers several places. Pass one of them as "
					+ "'channel': " + String.join(", ", allowed) + ".";
			}
			name = allowed.iterator().next();
		}
		ViewChannel target = ChatViewBinding.presentable(window, name);
		if (target == null) {
			return "Nothing was presented: channel '" + name + "' cannot be written here. This page offers: "
				+ String.join(", ", allowed) + ".";
		}

		// A form's object stands on the channel as itself: that is what "the open object" has always
		// been for the elements reading this channel, a <form>, a subject, a context fact.
		Object value = presentation.as() == PresentAs.FORM ? presentation.objects().get(0) : presentation;
		WindowAccess access = ChatWindowService.inWindow(window, () -> target.set(value));
		switch (access) {
			case DONE:
				Logger.debug("Presented " + presentation + " on channel '" + name + "'.", ChatPresentTool.class);
				return null;
			case BUSY:
				return "The page was busy and could not be written. Try once more.";
			default:
				return NO_PAGE;
		}
	}
}
