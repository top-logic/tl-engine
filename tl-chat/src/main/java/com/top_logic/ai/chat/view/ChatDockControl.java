/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.List;

import com.top_logic.ai.chat.view.ChatDockElement.Config;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.element.ContentControls;
import com.top_logic.util.Resources;

/**
 * React control for {@code <chat-dock/>}.
 *
 * <p>
 * Builds one child control for the configured content and puts it into its own state, which is how
 * a control reaches the client. The child is built once and stays built: the browser hides the
 * panel while it is closed rather than dropping it, because the conversation's event stream, its
 * transcript and its run all live in the elements inside, and a closed dock that forgot them would
 * be a new chat every time it is opened.
 * </p>
 *
 * <p>
 * The state this control owns is the frame: which conversation the launcher reports on, its glyph,
 * its name and whether the panel starts open. Everything inside the panel belongs to the elements
 * configured there.
 * </p>
 */
public class ChatDockControl extends ChatControl {

	/**
	 * Creates a {@link ChatDockControl}.
	 *
	 * @param context
	 *        The view context of the dock element.
	 * @param config
	 *        The dock's configuration.
	 * @param content
	 *        The elements the panel shows; empty for a dock that opens onto nothing.
	 */
	public ChatDockControl(ViewContext context, Config config, List<UIElement> content) {
		// The dock's own chat-id, or "default": a <chat> nested inside cannot hand one up, so unlike
		// every other chat element this one is normally configured rather than inheriting.
		super(context, blankToDefault(config.getReactElement(), "TLChatDock"),
			ChatScope.resolve(context, config.getChatId()), "chat-dock");
		setCssClass(config.getCssClass());
		putState("icon", ChatIcons.encode(context, config.getIcon()));
		putState("label", label(config.getLabel()));
		// "dockOpen", not "open": "open" is how <chat-rail/> states its own first-load default, and
		// the whole bootstrap is read out of one state map, so a dock writing "open" would decide
		// for the rail inside it whether that rail starts open.
		putState("dockOpen", Boolean.valueOf(config.getOpen()));

		if (!content.isEmpty()) {
			// Scoped, so a <chat-conversation/> in here knows it is the dock's own and does not make
			// the dock stand down for itself - see ChatDockScope.
			putState("content", ContentControls.toControl(content, context.childContext("content")
				.withChildSlotPath("content")
				.withScope(ChatDockScope.class, ChatDockScope.INSTANCE)));
		}
	}

	private static String label(ResKey key) {
		if (key == null) {
			return "";
		}
		String label = Resources.getInstance().getString(key, null);
		return label == null ? "" : label;
	}
}
