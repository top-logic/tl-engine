/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.Locale;

import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.ai.chat.view.AgentChatElement.TraceMode;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

/**
 * Native React-view control for the agentic chat interface.
 *
 * <p>
 * The control only establishes the per-window chat state and supplies bootstrap data. The
 * {@code TLAgentChat} browser component owns the chat UI and consumes the existing REST/SSE API.
 * </p>
 */
public class AgentChatControl extends ReactControl {

	/** Creates an {@link AgentChatControl}. */
	public AgentChatControl(ReactContext context, TraceMode traceMode, String promptImproverAgent) {
		super(context, null, "TLAgentChat");

		TaskProgressTracker tracker = bindTracker(context);
		storeUser(tracker);
		String configuredImprover = blankToNull(promptImproverAgent);
		tracker.setPromptImproverAgentName(configuredImprover);

		// The chat has no anonymous mode: ChatServlet refuses every request from a session that is
		// not signed in, so the component says that rather than opening a chat nothing can be sent
		// to. The refusal itself is the servlet's; this only keeps the UI honest about it.
		putState("authenticated", !TLContext.isAnonymous());
		putState("windowId", context.getWindowName());
		putState("contextPath", context.getContextPath());
		putState("userName", valueOr(tracker.getUserName(), "User"));
		putState("welcomeName", valueOr(tracker.getWelcomeName(), ""));
		// The chat's own strings follow the user's locale, so the chat and the view layer around it
		// are never in two languages at once.
		putState("language", language());
		putState("traceMode", traceMode.getExternalName());
		putState("promptImprovementEnabled", configuredImprover != null);
	}

	private static TaskProgressTracker bindTracker(ReactContext context) {
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		TLSessionContext session = subSession == null ? null : subSession.getSessionContext();
		TaskProgressTracker tracker =
			ChatSessionRegistry.resolveTracker(session, context.getWindowName());
		if (tracker == null) {
			tracker = new TaskProgressTracker();
		}
		tracker.setWindowId(context.getWindowName());
		ChatSessionRegistry.register(subSession, tracker);
		return tracker;
	}

	private static void storeUser(TaskProgressTracker tracker) {
		TLContext tlContext = TLContext.getContext();
		Person person = tlContext == null ? null : tlContext.getPerson();
		if (person == null) {
			return;
		}
		tracker.setUserName(person.getName());
		String fullName = person.getFullName();
		if (fullName == null || fullName.isBlank()) {
			tracker.setWelcomeName(person.getName());
			return;
		}
		String trimmed = fullName.trim();
		int separator = trimmed.indexOf(' ');
		tracker.setWelcomeName(separator < 0 ? trimmed : trimmed.substring(0, separator));
	}

	/** The language tag of the current user's locale, e.g. {@code "de"}; English when unknown. */
	private static String language() {
		TLContext tlContext = TLContext.getContext();
		Locale locale = tlContext == null ? null : tlContext.getCurrentLocale();
		return locale == null ? Locale.ENGLISH.getLanguage() : locale.getLanguage();
	}

	private static String valueOr(String value, String fallback) {
		return value == null ? fallback : value;
	}

	private static String blankToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
