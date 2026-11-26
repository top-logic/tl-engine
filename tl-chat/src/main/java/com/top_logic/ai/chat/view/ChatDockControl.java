/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.List;
import java.util.Map;

import com.top_logic.ai.chat.view.ChatDockElement.Config;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactParam;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.react.control.common.BadgeDisplay;
import com.top_logic.layout.view.channel.ChannelNotificationScope;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.BadgeBinding;
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
 * Whether the panel is open is this control's state ({@link #CMD_SET_OPEN}), and it is handed to the
 * elements inside through their {@link ChatDockScope}: <b>a conversation in a closed dock offers the
 * agent nothing</b> - no channel to present on, no page command, no subject, no facts - although it
 * stays displayed, so that the store keeps following the conversation.
 * </p>
 *
 * <p>
 * The state this control owns is the frame: which conversation the launcher reports on, its glyph,
 * its name and whether the panel is open. Everything inside the panel belongs to the elements
 * configured there.
 * </p>
 */
public class ChatDockControl extends ChatControl {

	/**
	 * Command: opens or closes the panel. The browser sends it when the user opens or closes the
	 * dock, and when the page starts showing the dock's conversation itself.
	 */
	public static final String CMD_SET_OPEN = "setOpen";

	/** Argument of {@link #CMD_SET_OPEN}: whether the panel is open. */
	public static final String ARG_OPEN = "open";

	/**
	 * State key: whether the panel is open.
	 *
	 * <p>
	 * Not {@code "open"}: the whole bootstrap of the chat is read out of one state map, in which
	 * {@code "open"} is too general a name to stay unambiguous.
	 * </p>
	 */
	public static final String STATE_OPEN = "dockOpen";

	/** State key of the content control. */
	public static final String STATE_CONTENT = "content";

	/**
	 * State key: the badge of the launcher, from the {@link Config#getBadge() badge} channel; absent
	 * for none.
	 */
	public static final String STATE_BADGE = "badge";

	/** Whether the panel is open; read by the elements inside. */
	private final ChatDockScope _scope;

	/** The channel following whether the panel is open, {@code null} for none. */
	private final ViewChannel _openChannel;

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
		_scope = new ChatDockScope(config.getOpen());
		putState(STATE_OPEN, Boolean.valueOf(_scope.isOpen()));

		ChannelRef openRef = config.getOpenChannel();
		_openChannel = openRef == null ? null : context.resolveChannel(openRef);
		if (_openChannel != null) {
			_openChannel.set(Boolean.valueOf(_scope.isOpen()));
			ViewChannel.ChannelListener listener = (sender, oldValue, newValue) -> {
				if (newValue instanceof Boolean open) {
					ChannelNotificationScope.current().afterNotification(() -> setPanelOpen(open.booleanValue()));
				}
			};
			addCleanupAction(_openChannel.addListener(listener)::dispose);
		}

		BadgeBinding badge = BadgeBinding.create(context, config);
		if (badge != null) {
			writeBadge(badge.current());
			badge.bind(this, this::writeBadge);
		}

		if (!content.isEmpty()) {
			// Scoped, so a <chat-conversation/> in here knows it is the dock's own: it does not make the
			// dock stand down for itself, and it offers the agent something only while the dock is open.
			putState(STATE_CONTENT, ContentControls.toControl(content, context.childContext(STATE_CONTENT)
				.withChildSlotPath(STATE_CONTENT)
				.withScope(ChatDockScope.class, _scope)));
		}
	}

	/**
	 * Whether the panel is open, see {@link #STATE_OPEN}.
	 */
	public boolean isOpen() {
		return _scope.isOpen();
	}

	/**
	 * Opens or closes the panel, see {@link #CMD_SET_OPEN}.
	 */
	@ReactCommandHandler(value = CMD_SET_OPEN, params = {
		@ReactParam(name = ARG_OPEN, type = "boolean", description = "Whether the panel is open.")
	})
	public void setOpen(Map<String, Object> args) {
		boolean open = Boolean.TRUE.equals(args == null ? null : args.get(ARG_OPEN));
		// The conversations inside withdraw or offer their registrations; never from inside a channel
		// notification, whose pending listeners may still read them.
		ChannelNotificationScope.current().afterNotification(() -> setPanelOpen(open));
	}

	/**
	 * Opens or closes the panel, and with it what the conversations inside offer the agent.
	 */
	public void setPanelOpen(boolean open) {
		if (open == _scope.isOpen() || isDisposed()) {
			return;
		}
		putState(STATE_OPEN, Boolean.valueOf(open));
		_scope.setOpen(open);
		if (_openChannel != null) {
			_openChannel.set(Boolean.valueOf(open));
		}
	}

	private void writeBadge(BadgeDisplay badge) {
		putState(STATE_BADGE, badge == null ? null : badge.toState());
	}

	private static String label(ResKey key) {
		if (key == null) {
			return "";
		}
		String label = Resources.getInstance().getString(key, null);
		return label == null ? "" : label;
	}
}
