/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.ai.chat.view.AgentChatElement.TraceMode;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * Native React-view control for the agentic chat interface.
 *
 * <p>
 * The control only establishes the per-window chat state and supplies bootstrap data. The
 * {@code TLAgentChat} browser component owns the chat UI and consumes the existing REST/SSE API.
 * Tracker, user and layout preferences come from {@link ChatViewBinding}, so a second chat element
 * on the same {@code chat-id} does not load them again.
 * </p>
 */
public class AgentChatControl extends ReactControl {

	/** Creates an {@link AgentChatControl}. */
	public AgentChatControl(ReactContext context, TraceMode traceMode, String promptImproverAgent) {
		this(context, traceMode, promptImproverAgent, "TLAgentChat");
	}

	/**
	 * Creates an {@link AgentChatControl} that mounts the named React element.
	 *
	 * @param reactElement registered component name, {@code TLAgentChat} when empty
	 */
	public AgentChatControl(ReactContext context, TraceMode traceMode, String promptImproverAgent,
			String reactElement) {
		super(context, null, blankToNull(reactElement) == null ? "TLAgentChat" : reactElement.trim());

		// Empty chat-id: this one-element form stays isolated from the four pieces, which default
		// to "default". App then keys the browser store by the control id.
		ChatViewBinding binding = ChatViewBinding.bind(context, "");
		TaskProgressTracker tracker = binding.tracker();
		String configuredImprover = blankToNull(promptImproverAgent);
		tracker.setPromptImproverAgentName(configuredImprover);

		binding.writeSharedState(this::putState);
		putState("traceMode", traceMode.getExternalName());
		putState("promptImprovementEnabled", Boolean.valueOf(configuredImprover != null));
	}

	private static String blankToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
