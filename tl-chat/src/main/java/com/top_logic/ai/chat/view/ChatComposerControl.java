/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.List;

import com.top_logic.ai.chat.view.ChatComposerElement.Config;
import com.top_logic.ai.chat.view.ChatComposerElement.Controls;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * React control for {@code <chat-composer/>}.
 *
 * <p>
 * Binds the window once through {@link ChatViewBinding} and, when an input channel is configured,
 * follows that channel the way {@code <chat-thread/>} does. Prompt improvement is a composer
 * concern: this control names the agent on the shared tracker.
 * </p>
 */
public class ChatComposerControl extends ReactControl {

	private final ViewChannel _inputChannel;

	private final boolean[] _applyingFromChannel = { false };

	/** Creates a {@link ChatComposerControl}. */
	public ChatComposerControl(ViewContext context, Config config) {
		super(context, null, blankToDefault(config.getReactElement(), "TLChatComposer"));

		ChatViewBinding binding = ChatViewBinding.bind(context, config.getChatId());
		String configuredImprover = blankToNull(config.getPromptImproverAgent());
		binding.tracker().setPromptImproverAgentName(configuredImprover);
		binding.writeSharedState(this::putState);
		putState("chatId", config.getChatId());
		putState("promptImprovementEnabled", Boolean.valueOf(configuredImprover != null));
		putState("placeholder", resolvePlaceholder(config.getPlaceholder()));
		String referenceSource = blankToNull(config.getReferenceSource());
		putState("referenceSource", referenceSource == null ? "" : referenceSource);
		List<String> menuItems = config.getMenuItems();
		putState("menuItems", menuItems == null ? List.of() : List.copyOf(menuItems));

		Controls controls = config.getControls();
		putState("autoApprove", Boolean.valueOf(controls.getAutoApprove()));
		putState("enterHint", Boolean.valueOf(controls.getEnterHint()));
		putState("agentPicker", Boolean.valueOf(controls.getAgentPicker()));
		putState("modelPicker", Boolean.valueOf(controls.getModelPicker()));
		putState("thinking", Boolean.valueOf(controls.getThinking()));
		putState("mcp", Boolean.valueOf(controls.getMcp()));
		putState("attachments", Boolean.valueOf(controls.getAttachments()));
		putState("tracePicker", Boolean.valueOf(controls.getTracePicker()));
		putState("agentSlash", Boolean.valueOf(controls.getAgentSlash()));

		ChannelRef inputRef = config.getInput();
		if (inputRef == null) {
			_inputChannel = null;
			putState("selected", "");
			return;
		}
		_inputChannel = context.resolveChannel(inputRef);
		ViewChannel.ChannelListener listener = (sender, oldValue, newValue) -> applyFromChannel(newValue);
		_inputChannel.addListener(listener);
		addCleanupAction(() -> _inputChannel.removeListener(listener));
		applyFromChannel(_inputChannel.get());
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		if (_inputChannel != null) {
			applyFromChannel(_inputChannel.get());
		}
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

	private static String resolvePlaceholder(ResKey key) {
		if (key == null) {
			return "";
		}
		return Resources.getInstance().getString(key);
	}

	private static String blankToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private static String blankToDefault(String value, String fallback) {
		if (value == null) {
			return fallback;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? fallback : trimmed;
	}
}
