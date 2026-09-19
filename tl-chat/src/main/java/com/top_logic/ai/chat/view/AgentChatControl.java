/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.ai.chat.view.AgentChatElement.TraceMode;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * Native React-view control for the agentic chat interface.
 *
 * <p>
 * The control only establishes the per-window chat state and supplies bootstrap data. The
 * {@code TLAgentChat} browser component owns the chat UI and consumes the existing REST/SSE API.
 * Tracker and user come from {@link ChatViewBinding}, so a second chat element
 * on the same {@code chat-id} does not bind them again. The layout preference is re-read on every
 * render, so a browser reload replays the current one rather than the first render's.
 * </p>
 */
public class AgentChatControl extends ReactControl {

	private final ChatViewBinding _binding;

	/** Creates an {@link AgentChatControl}. */
	public AgentChatControl(ReactContext context, TraceMode traceMode, String promptImproverAgent) {
		this(context, traceMode, promptImproverAgent, "TLAgentChat", null);
	}

	/**
	 * Creates an {@link AgentChatControl} that mounts the named React element.
	 *
	 * @param reactElement
	 *        registered component name, {@code TLAgentChat} when empty
	 */
	public AgentChatControl(ReactContext context, TraceMode traceMode, String promptImproverAgent,
			String reactElement) {
		this(context, traceMode, promptImproverAgent, reactElement, null);
	}

	/**
	 * Creates an {@link AgentChatControl} that mounts the named React element and invites the user
	 * with the host's own wording.
	 *
	 * @param reactElement
	 *        registered component name, {@code TLAgentChat} when empty
	 * @param placeholder
	 *        what the empty prompt field invites the user with, {@code null} for the chat's own
	 *        wording
	 */
	public AgentChatControl(ReactContext context, TraceMode traceMode, String promptImproverAgent,
			String reactElement, ResKey placeholder) {
		super(context, null, blankToNull(reactElement) == null ? "TLAgentChat" : reactElement.trim());

		// Empty chat-id: this one-element form stays isolated from the four pieces, which default
		// to "default". App then keys the browser store by the control id - and so does the server,
		// because the conversation this control shows is its own: two <agent-chat/> elements in one
		// window are two conversations with a run each, not one they overwrite for each other.
		_binding = ChatViewBinding.bindOwn(context, getID());
		putState("chatId", _binding.conversation());
		TaskProgressTracker tracker = _binding.tracker();
		String configuredImprover = blankToNull(promptImproverAgent);
		tracker.setPromptImproverAgentName(configuredImprover);

		_binding.writeSharedState(this::putState);
		putState("traceMode", traceMode.getExternalName());
		putState("promptImprovementEnabled", Boolean.valueOf(configuredImprover != null));
		// The composer's idle invitation, in this account's language. Empty means "nothing
		// configured", which is what makes the browser keep its own wording.
		putState("placeholder", ChatViewBinding.resolveText(placeholder));
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

	private static String blankToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
