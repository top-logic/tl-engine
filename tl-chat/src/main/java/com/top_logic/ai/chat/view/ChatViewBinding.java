/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.top_logic.ai.chat.api.ChatPreferencesStore;
import com.top_logic.ai.chat.api.messages.ChatPreferencesDto;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

/**
 * Per-window, per-{@code chat-id} server binding for the chat view elements.
 *
 * <p>
 * {@link AgentChatControl} used to bind the tracker, the user and the layout preferences in every
 * constructor. Four elements on one chat would then load {@link ChatPreferencesStore} four times
 * and race on one {@code PersonalConfiguration} key. This class does that work once per
 * (sub-session, chat-id). The tracker itself stays one per window:
 * {@link com.top_logic.ai.chat.api.ChatServlet} still resolves it from {@code windowId} alone.
 * </p>
 */
public final class ChatViewBinding {

	@SuppressWarnings("unchecked")
	private static final Property<Map<String, ChatViewBinding>> BINDINGS =
		TypedAnnotatable.propertyMap("tl.chat.view.bindings");

	/** Loads layout preferences. Tests replace this so a second bind can be shown not to load. */
	static Supplier<ChatPreferencesDto> preferenceSource = ChatPreferencesStore::load;

	private final TaskProgressTracker _tracker;

	private final ChatPreferencesDto _preferences;

	private final String _windowId;

	private final String _contextPath;

	private final String _userName;

	private final String _welcomeName;

	private final String _language;

	private final boolean _authenticated;

	private ChatViewBinding(TLSubSessionContext subSession, String windowName, String contextPath) {
		_tracker = bindTracker(subSession, windowName);
		storeUser(_tracker);
		_preferences = preferenceSource.get();
		_windowId = windowName;
		_contextPath = contextPath == null ? "" : contextPath;
		_userName = valueOr(_tracker.getUserName(), "User");
		_welcomeName = valueOr(_tracker.getWelcomeName(), "");
		_language = currentLanguage();
		_authenticated = !TLContext.isAnonymous();
	}

	/**
	 * The binding for {@code chatId} in the current window, created on first use.
	 *
	 * <p>
	 * Idempotent per (sub-session, chat-id): a second element that joins the same chat reuses the
	 * tracker, the user and the already loaded preferences.
	 * </p>
	 */
	public static ChatViewBinding bind(ReactContext context, String chatId) {
		return bind(TLContextManager.getSubSession(), context.getWindowName(), context.getContextPath(), chatId);
	}

	/**
	 * The binding for {@code chatId} on {@code scope}.
	 *
	 * <p>
	 * Package-visible so a test can pass a {@link TypedAnnotatable} without a live window.
	 * </p>
	 */
	static ChatViewBinding bind(TypedAnnotatable scope, String windowName, String contextPath, String chatId) {
		String key = chatId == null ? "" : chatId.trim();
		if (scope == null) {
			return new ChatViewBinding(null, windowName, contextPath);
		}
		Map<String, ChatViewBinding> map = scope.mkMap(BINDINGS);
		synchronized (map) {
			ChatViewBinding existing = map.get(key);
			if (existing != null) {
				return existing;
			}
			TLSubSessionContext subSession = scope instanceof TLSubSessionContext session ? session : null;
			ChatViewBinding created = new ChatViewBinding(subSession, windowName, contextPath);
			map.put(key, created);
			return created;
		}
	}

	/** The window's {@link TaskProgressTracker}. */
	public TaskProgressTracker tracker() {
		return _tracker;
	}

	/** Layout preferences loaded once for this chat. */
	public ChatPreferencesDto preferences() {
		return _preferences;
	}

	/** User name shown in the chat. */
	public String userName() {
		return _userName;
	}

	/** First name used in the empty-state greeting. */
	public String welcomeName() {
		return _welcomeName;
	}

	/** Language tag of the current user's locale, e.g. {@code "de"}. */
	public String language() {
		return _language;
	}

	/** Registry key of the sub-session this chat was rendered in. */
	public String windowId() {
		return _windowId;
	}

	/** Webapp context path, for building {@code /chat/*} URLs. */
	public String contextPath() {
		return _contextPath;
	}

	/** Whether the session is signed in. */
	public boolean authenticated() {
		return _authenticated;
	}

	/**
	 * Writes the bootstrap every chat element needs into the control's state.
	 *
	 * <p>
	 * Each control still has its own state map. The values are the same for every element on this
	 * chat because they come from this binding.
	 * </p>
	 */
	public void writeSharedState(BiConsumer<String, Object> put) {
		put.accept("authenticated", Boolean.valueOf(_authenticated));
		put.accept("windowId", _windowId);
		put.accept("contextPath", _contextPath);
		put.accept("userName", _userName);
		put.accept("welcomeName", _welcomeName);
		put.accept("language", _language);
		put.accept("railOpen", Boolean.valueOf(_preferences.isRailOpen()));
		put.accept("paneWidth", Integer.valueOf(_preferences.getPaneWidth()));
		put.accept("badgeSeen", Integer.valueOf(_preferences.getBadgeSeen()));
	}

	private static TaskProgressTracker bindTracker(TLSubSessionContext subSession, String windowName) {
		TLSessionContext session = subSession == null ? null : subSession.getSessionContext();
		TaskProgressTracker tracker = ChatSessionRegistry.resolveTracker(session, windowName);
		if (tracker == null) {
			tracker = new TaskProgressTracker();
		}
		tracker.setWindowId(windowName);
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

	private static String currentLanguage() {
		TLContext tlContext = TLContext.getContext();
		Locale locale = tlContext == null ? null : tlContext.getCurrentLocale();
		return locale == null ? Locale.ENGLISH.getLanguage() : locale.getLanguage();
	}

	private static String valueOr(String value, String fallback) {
		return value == null ? fallback : value;
	}
}
