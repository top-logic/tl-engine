/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.Map;

import com.top_logic.ai.chat.view.ChatSessionBindingElement.Config;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ReactParam;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;
import com.top_logic.util.TLContext;

/**
 * React control for {@code <chat-session-binding/>}.
 *
 * <p>
 * Same selection-channel bridge as {@link ChatSessionsControl} (the channel holds the
 * {@link TLObject} session; the browser uses the id string), but headless of a list: its React
 * element renders only the "new chat" button and publishes the store's own session id back onto the
 * channel for the list to highlight.
 * </p>
 */
public class ChatSessionBindingControl extends ReactControl {

	private final ViewChannel _selectionChannel;

	private final boolean[] _applyingFromChannel = { false };

	/** Creates a {@link ChatSessionBindingControl}. */
	public ChatSessionBindingControl(ViewContext context, Config config) {
		super(context, null, blankToDefault(config.getReactElement(), "TLChatSessionBinding"));

		ChatViewBinding binding = ChatViewBinding.bind(context, config.getChatId());
		binding.writeSharedState(this::putState);
		putState("chatId", config.getChatId());
		putState("newChatButton", Boolean.valueOf(config.getNewChatButton()));

		ChannelRef selectionRef = config.getSelection();
		if (selectionRef == null) {
			_selectionChannel = null;
			putState("selected", "");
			return;
		}
		_selectionChannel = context.resolveChannel(selectionRef);
		ViewChannel.ChannelListener listener = (sender, oldValue, newValue) -> applyFromChannel(newValue);
		_selectionChannel.addListener(listener);
		addCleanupAction(() -> _selectionChannel.removeListener(listener));
		applyFromChannel(_selectionChannel.get());

		ChannelRef createSignalRef = config.getCreateSignal();
		if (createSignalRef != null) {
			ViewChannel createSignal = context.resolveChannel(createSignalRef);
			ViewChannel.ChannelListener createListener =
				(sender, oldValue, newValue) -> putState("newChatTick", newValue);
			createSignal.addListener(createListener);
			addCleanupAction(() -> createSignal.removeListener(createListener));
		}
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		if (_selectionChannel != null) {
			applyFromChannel(_selectionChannel.get());
		}
	}

	/**
	 * Publishes the given session onto the selection channel.
	 *
	 * @param args
	 *        {@code id} is the session id the browser uses with {@code /chat/*}. Empty clears the
	 *        channel (a fresh conversation has no persisted session yet).
	 */
	@ReactCommandHandler(value = "selectSession", params = {
		@ReactParam(name = "id", type = "string", description = "Owned chat session id, or empty to clear.")
	})
	public void selectSession(Map<String, Object> args) {
		if (_applyingFromChannel[0]) {
			return;
		}
		String id = stringArg(args, "id");
		if (_selectionChannel == null) {
			putState("selected", id);
			return;
		}
		TLObject session = ChatSessionIds.ownedSession(id, currentPerson());
		_selectionChannel.set(session);
		putState("selected", session == null ? "" : ChatSessionIds.idOf(session));
	}

	private void applyFromChannel(Object value) {
		_applyingFromChannel[0] = true;
		try {
			putState("selected", ChatSessionIds.selectedId(value, currentPerson()));
		} finally {
			_applyingFromChannel[0] = false;
		}
	}

	private static Person currentPerson() {
		TLContext context = TLContext.getContext();
		return context == null ? null : context.getPerson();
	}

	private static String stringArg(Map<String, Object> args, String key) {
		if (args == null) {
			return "";
		}
		Object value = args.get(key);
		return value == null ? "" : String.valueOf(value).trim();
	}

	private static String blankToDefault(String value, String fallback) {
		if (value == null) {
			return fallback;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? fallback : trimmed;
	}
}
