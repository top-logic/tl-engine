/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.ai.chat.view.ChatRailElement.Config;
import com.top_logic.ai.chat.view.ChatRailElement.SectionConfig;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.util.TLContext;

/**
 * React control for {@code <chat-rail/>}.
 *
 * <p>
 * Binds the window once through {@link ChatViewBinding}. An optional {@code input} channel selects
 * the open session. Section names, strip and pane defaults go into this control's own state; the
 * remembered layout preferences stay on the shared binding.
 * </p>
 */
public class ChatRailControl extends ReactControl {

	private final ViewChannel _inputChannel;

	private final boolean[] _applyingFromChannel = { false };

	/** Creates a {@link ChatRailControl}. */
	public ChatRailControl(ViewContext context, Config config) {
		super(context, null, blankToDefault(config.getReactElement(), "TLChatRail"));

		ChatViewBinding binding = ChatViewBinding.bind(context, config.getChatId());
		binding.writeSharedState(this::putState);
		putState("chatId", config.getChatId());
		putState("strip", Boolean.valueOf(config.getStrip()));
		putState("paneMode", config.getPaneMode().getExternalName());
		int paneWidth = config.getPaneWidth();
		if (paneWidth > 0) {
			putState("paneWidth", Integer.valueOf(paneWidth));
		}
		List<SectionConfig> configured = config.getSections();
		List<Map<String, Object>> sections = new ArrayList<>();
		List<String> collapsed = new ArrayList<>();
		if (configured != null) {
			for (SectionConfig section : configured) {
				if (section == null || section.getName() == null || section.getName().isBlank()) {
					continue;
				}
				String xmlName = section.getName().trim();
				String id = ChatRailElement.internalName(xmlName);
				Map<String, Object> row = new LinkedHashMap<>();
				row.put("name", xmlName);
				row.put("id", id);
				row.put("open", Boolean.valueOf(section.getOpen()));
				row.put("badge", Boolean.valueOf(section.getBadge()));
				sections.add(row);
				if (!section.getOpen()) {
					collapsed.add(id);
				}
			}
		}
		putState("sections", sections);
		if (!sections.isEmpty()) {
			putState("collapsedSections", collapsed);
		}

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

	private static String blankToDefault(String value, String fallback) {
		if (value == null) {
			return fallback;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? fallback : trimmed;
	}
}
