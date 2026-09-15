/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.List;
import java.util.Map;

import com.top_logic.ai.chat.view.ChatThreadElement.Actions;
import com.top_logic.ai.chat.view.ChatThreadElement.Config;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ReactParam;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.util.TLContext;

/**
 * React control for {@code <chat-thread/>}.
 *
 * <p>
 * Binds the window once through {@link ChatViewBinding}. An optional {@code input} channel selects
 * the open session (same mapping as {@code <chat-sessions/>}). An optional {@code busy} channel is
 * written from the browser while the agent works.
 * </p>
 */
public class ChatThreadControl extends ReactControl {

	private final ViewChannel _inputChannel;

	private final ViewChannel _busyChannel;

	private final boolean[] _applyingFromChannel = { false };

	/** Creates a {@link ChatThreadControl}. */
	public ChatThreadControl(ViewContext context, Config config) {
		super(context, null, blankToDefault(config.getReactElement(), "TLChatThread"));

		ChatViewBinding binding = ChatViewBinding.bind(context, config.getChatId());
		binding.writeSharedState(this::putState);
		putState("chatId", config.getChatId());
		putState("traceMode", config.getTraceMode().getExternalName());
		putState("runSummary", config.getRunSummary().getExternalName());
		putState("citationMarkers", Boolean.valueOf(config.getCitationMarkers()));
		putState("railToggle", Boolean.valueOf(config.getRailToggle()));
		String annotations = config.getAnnotations();
		putState("annotations", annotations == null ? "" : annotations.trim());
		Actions actions = config.getActions();
		putState("copy", Boolean.valueOf(actions.getCopy()));
		putState("rate", Boolean.valueOf(actions.getRate()));
		putState("regenerate", Boolean.valueOf(actions.getRegenerate()));
		List<String> host = actions.getHost();
		putState("hostActions", host == null ? List.of() : List.copyOf(host));

		ChannelRef inputRef = config.getInput();
		if (inputRef == null) {
			_inputChannel = null;
			putState("selected", "");
		} else {
			_inputChannel = context.resolveChannel(inputRef);
			ViewChannel.ChannelListener listener = (sender, oldValue, newValue) -> applyFromChannel(newValue);
			_inputChannel.addListener(listener);
			addCleanupAction(() -> _inputChannel.removeListener(listener));
			applyFromChannel(_inputChannel.get());
		}

		ChannelRef busyRef = config.getBusy();
		_busyChannel = busyRef == null ? null : context.resolveChannel(busyRef);
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		if (_inputChannel != null) {
			applyFromChannel(_inputChannel.get());
		}
	}

	/**
	 * Writes whether the agent is working onto the busy channel.
	 *
	 * @param args
	 *        {@code busy} is {@code true} while a run is in flight.
	 */
	@ReactCommandHandler(value = "setBusy", params = {
		@ReactParam(name = "busy", type = "boolean", description = "True while the agent is working.")
	})
	public void setBusy(Map<String, Object> args) {
		if (_busyChannel == null) {
			return;
		}
		_busyChannel.set(Boolean.valueOf(truthy(args == null ? null : args.get("busy"))));
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

	private static boolean truthy(Object value) {
		if (value instanceof Boolean flag) {
			return flag.booleanValue();
		}
		return value != null && Boolean.parseBoolean(String.valueOf(value));
	}

	private static String blankToDefault(String value, String fallback) {
		if (value == null) {
			return fallback;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? fallback : trimmed;
	}
}
