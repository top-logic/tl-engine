/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.Map;

import com.top_logic.ai.chat.view.ChatSectionElement.Config;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ReactParam;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * React control behind the chat's rail-section elements.
 *
 * <p>
 * Binds the window once through {@link ChatViewBinding} - the same bootstrap every chat element
 * pushes, so a section may be the first of them to mount - and publishes the browser's count on the
 * configured {@code badge} channel.
 * </p>
 *
 * <p>
 * The count travels browser -&gt; server -&gt; rail because that is the only way one view element
 * can tell another one a number: the chat's counts are derived in the browser store, while the rail
 * is a separate element that can see nothing but its channels. A section whose number is a model
 * question - how many rows a list has - writes the channel on the server and pays nothing.
 * </p>
 */
public class ChatSectionControl extends ReactControl {

	private final ChatViewBinding _binding;

	private final ViewChannel _badgeChannel;

	/**
	 * Creates a {@link ChatSectionControl}.
	 *
	 * @param context
	 *        The view context of the element.
	 * @param config
	 *        The element's configuration.
	 * @param tag
	 *        The element's tag, e.g. {@code chat-progress}, for the join diagnostic.
	 * @param defaultReactElement
	 *        Registered React element rendering this section when the configuration names none.
	 */
	public ChatSectionControl(ViewContext context, Config config, String tag, String defaultReactElement) {
		super(context, null, blankToDefault(config.getReactElement(), defaultReactElement));
		setCssClass(config.getCssClass());

		// The section's own chat-id when it has one, otherwise the enclosing <chat>'s, otherwise
		// "default". The browser keys its store by what we push here, so it must be the resolved id.
		String chatId = ChatScope.resolve(context, config.getChatId());
		_binding = ChatViewBinding.bind(context, chatId, tag);
		_binding.writeSharedState(this::putState);
		putState("chatId", chatId);

		ChannelRef badgeRef = config.getBadge();
		_badgeChannel = badgeRef == null ? null : context.resolveChannel(badgeRef);
		// The browser only reports a count when somebody is listening; without a channel the round
		// trip would end nowhere.
		putState("badge", Boolean.valueOf(_badgeChannel != null));
	}

	/**
	 * Re-reads the user's stored layout.
	 *
	 * <p>
	 * A browser reload re-renders the control tree the window already holds, so this hook is the
	 * only place that runs again. Writing the layout in the constructor would replay the first
	 * render's rail state for the life of the window (Gitea #423).
	 * </p>
	 */
	@Override
	protected void onBeforeWrite() {
		super.onBeforeWrite();
		_binding.writeLayoutState(this::putState);
	}

	/**
	 * Publishes how many things this section is showing.
	 *
	 * @param args
	 *        {@code count} is the current number; anything that is not a number counts as none.
	 */
	@ReactCommandHandler(value = "badge", technical = true, params = {
		@ReactParam(name = "count", type = "int", description = "How many things this section shows.")
	})
	public void badge(Map<String, Object> args) {
		if (_badgeChannel == null) {
			return;
		}
		_badgeChannel.set(Integer.valueOf(intArg(args, "count")));
	}

	private static int intArg(Map<String, Object> args, String key) {
		Object value = args == null ? null : args.get(key);
		if (value instanceof Number number) {
			return Math.max(0, number.intValue());
		}
		if (value instanceof String text) {
			try {
				return Math.max(0, Integer.parseInt(text.trim()));
			} catch (NumberFormatException ex) {
				return 0;
			}
		}
		return 0;
	}

	private static String blankToDefault(String value, String fallback) {
		if (value == null) {
			return fallback;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? fallback : trimmed;
	}
}
