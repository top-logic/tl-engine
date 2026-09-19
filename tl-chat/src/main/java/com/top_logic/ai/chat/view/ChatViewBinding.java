/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.top_logic.ai.chat.api.ChatPreferencesStore;
import com.top_logic.ai.chat.api.messages.ChatPreferencesDto;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.util.Resources;
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
 *
 * <p>
 * The layout preference is the one thing here that is <em>not</em> bound once: it is read whenever
 * {@link #writeLayoutState(BiConsumer)} is asked for it. A browser reload re-renders the control
 * tree the window already holds instead of rebuilding it, so a value captured in a constructor
 * would replay the first render's layout for as long as the window lives (Gitea #423). Reading it
 * again costs a lookup in the sub-session's personal configuration, which is already in memory,
 * plus a small JSON parse.
 * </p>
 */
public final class ChatViewBinding {

	@SuppressWarnings("unchecked")
	private static final Property<Map<String, ChatViewBinding>> BINDINGS =
		TypedAnnotatable.propertyMap("tl.chat.view.bindings");

	/** Loads the layout of a {@code chat-id}. Tests replace this to observe when a load happens. */
	static Function<String, ChatPreferencesDto> preferenceSource = ChatPreferencesStore::load;

	private final TaskProgressTracker _tracker;

	private final String _chatId;

	private final String _windowId;

	private final String _contextPath;

	private final String _userName;

	private final String _welcomeName;

	private final String _language;

	private final boolean _authenticated;

	/**
	 * The channels the elements of this chat let an agent write, by name.
	 *
	 * <p>
	 * Deny by default: a channel is in here only because a {@code <chat-conversation present="…">}
	 * named it, and it leaves again when that control is disposed. Concurrent because a running
	 * agent reads it from its own thread while the window renders.
	 * </p>
	 */
	private final Map<String, ViewChannel> _presentable = new ConcurrentHashMap<>();

	private ChatViewBinding(TLSubSessionContext subSession, String windowName, String contextPath, String chatId) {
		_chatId = chatId;
		_tracker = bindTracker(subSession, windowName);
		storeUser(_tracker);
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
	 * tracker and the user.
	 * </p>
	 */
	public static ChatViewBinding bind(ReactContext context, String chatId) {
		// Every chat element renders through here, so this is where the window becomes known to the
		// refresher: a running agent writes from a background thread, and without this the window
		// would only notice on its next command or on the 30 s heartbeat.
		AgentViewRefreshService.registerWindow(TLContextManager.getSubSession(), context.getWindowName(),
			context.getWindowRegistry());
		return bind(TLContextManager.getSubSession(), context.getWindowName(), context.getContextPath(), chatId);
	}

	/**
	 * The binding for {@code chatId}, recording that {@code element} joined that conversation.
	 *
	 * <p>
	 * This is what the elements that share a conversation by {@code chat-id} use. The join is
	 * tallied per render and logged when the render ends, so that an element which found nobody to
	 * join says so instead of rendering an empty box (see {@link ChatJoinLog}).
	 * {@link AgentChatControl} binds through {@link #bind(ReactContext, String)} instead: it keys
	 * by its own control id on purpose and joins nothing.
	 * </p>
	 *
	 * @param element
	 *        The joining element's tag, e.g. {@code chat-rail}.
	 */
	public static ChatViewBinding bind(ReactContext context, String chatId, String element) {
		ChatJoinLog.record(context.getWindowName(), chatId, element);
		return bind(context, chatId);
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
			return new ChatViewBinding(null, windowName, contextPath, key);
		}
		Map<String, ChatViewBinding> map = scope.mkMap(BINDINGS);
		synchronized (map) {
			ChatViewBinding existing = map.get(key);
			if (existing != null) {
				return existing;
			}
			TLSubSessionContext subSession = scope instanceof TLSubSessionContext session ? session : null;
			ChatViewBinding created = new ChatViewBinding(subSession, windowName, contextPath, key);
			map.put(key, created);
			return created;
		}
	}

	/** The window's {@link TaskProgressTracker}. */
	public TaskProgressTracker tracker() {
		return _tracker;
	}

	/** The layout preferences stored for this chat, as they are now. */
	public ChatPreferencesDto preferences() {
		return preferenceSource.apply(_chatId);
	}

	/** The {@code chat-id} this binding belongs to; the key its layout is stored under. */
	public String chatId() {
		return _chatId;
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
	 * Lets an agent run in this window write {@code channel} under the name it is configured with.
	 *
	 * <p>
	 * Called by {@link ChatConversationControl} for each entry of its {@code present} option, and
	 * undone when that control is disposed. Nothing else opens a channel to the agent: the allow
	 * list is the application's statement about which part of its page an agent may drive, and a
	 * channel nobody named stays closed.
	 * </p>
	 *
	 * @param name
	 *        The channel name the tool call has to use - the configured name, not a display name.
	 * @param channel
	 *        The resolved channel.
	 */
	public void allowPresenting(String name, ViewChannel channel) {
		if (name == null || name.isBlank() || channel == null) {
			return;
		}
		_presentable.put(name.trim(), channel);
	}

	/**
	 * Undoes one {@link #allowPresenting(String, ViewChannel)}, for a control that goes away.
	 *
	 * @param name
	 *        The channel name.
	 * @param channel
	 *        The channel that was registered; a later registration of another channel under the
	 *        same name is left alone.
	 */
	public void denyPresenting(String name, ViewChannel channel) {
		if (name == null || channel == null) {
			return;
		}
		_presentable.remove(name.trim(), channel);
	}

	/** The channel {@code name}, or <code>null</code> when this chat does not offer it. */
	public ViewChannel presentable(String name) {
		return name == null ? null : _presentable.get(name.trim());
	}

	/** The names an agent may present into in this chat, for telling the model what it may use. */
	public Set<String> presentableNames() {
		return new TreeSet<>(_presentable.keySet());
	}

	/**
	 * The channel {@code name} an agent running in {@code scope}'s window may write, across every
	 * chat rendered there.
	 *
	 * <p>
	 * The window rather than the chat is the unit, because that is what a run knows about itself:
	 * the agent thread runs in the window's sub-session (see
	 * {@code ChatProcessor.AIBackgroundTask}), while which of possibly several {@code chat-id}s on
	 * that page it belongs to is not something the tool layer is told.
	 * </p>
	 *
	 * <p>
	 * <b>So the allow list of a window is the union of its chats' lists.</b> Two conversations on
	 * one window - a page's own and a {@code <chat-dock>}'s, say - each naming a channel means a run
	 * in either of them may write both. That is the honest consequence of what a run can be asked
	 * about itself, and it is bounded by the same thing the whole feature is bounded by: nothing is
	 * writable that no element of that window named, and every object still resolves with the
	 * acting user's rights. A host that needs the two kept apart gives them separate windows.
	 * </p>
	 *
	 * @param scope
	 *        The window's sub-session. May be <code>null</code>, which offers nothing.
	 * @param name
	 *        The channel name the agent asked for.
	 * @return The channel, or <code>null</code> when no chat in that window allows it.
	 */
	public static ViewChannel presentable(TypedAnnotatable scope, String name) {
		for (ChatViewBinding binding : bindings(scope)) {
			ViewChannel channel = binding.presentable(name);
			if (channel != null) {
				return channel;
			}
		}
		return null;
	}

	/**
	 * Every channel name an agent running in {@code scope}'s window may write, for the refusal that
	 * has to say what <em>is</em> allowed.
	 *
	 * @param scope
	 *        The window's sub-session. May be <code>null</code>, which offers nothing.
	 * @return The names, sorted; never <code>null</code>.
	 */
	public static Set<String> presentableNames(TypedAnnotatable scope) {
		Set<String> names = new TreeSet<>();
		for (ChatViewBinding binding : bindings(scope)) {
			names.addAll(binding._presentable.keySet());
		}
		return names;
	}

	/** The bindings of every chat rendered in {@code scope}'s window. */
	private static List<ChatViewBinding> bindings(TypedAnnotatable scope) {
		if (scope == null) {
			return List.of();
		}
		Map<String, ChatViewBinding> map = scope.get(BINDINGS);
		if (map == null || map.isEmpty()) {
			return List.of();
		}
		synchronized (map) {
			return new ArrayList<>(map.values());
		}
	}

	/**
	 * Writes the bootstrap every chat element needs into the control's state.
	 *
	 * <p>
	 * Each control still has its own state map. The values are the same for every element on this
	 * chat because they come from this binding.
	 * </p>
	 *
	 * <p>
	 * Everything written here is fixed for the lifetime of the window. The layout preference is
	 * not, so it has {@link #writeLayoutState(BiConsumer)} of its own; this method writes it too,
	 * so that a caller which only ever renders once needs the one call.
	 * </p>
	 */
	public void writeSharedState(BiConsumer<String, Object> put) {
		put.accept("authenticated", Boolean.valueOf(_authenticated));
		put.accept("windowId", _windowId);
		put.accept("contextPath", _contextPath);
		put.accept("userName", _userName);
		put.accept("welcomeName", _welcomeName);
		put.accept("language", _language);
		writeLayoutState(put);
	}

	/**
	 * Writes the user's stored layout into the control's state.
	 *
	 * <p>
	 * Every chat control calls this from its {@code onBeforeWrite} hook, because that is the one
	 * moment that happens again when the browser is reloaded. The constructor does not: the engine
	 * re-renders the control tree the window already holds, deliberately, so that a table keeps its
	 * selection - which means a layout captured at construction time is the layout of the first
	 * render forever (Gitea #423).
	 * </p>
	 */
	public void writeLayoutState(BiConsumer<String, Object> put) {
		ChatPreferencesDto preferences = preferences();
		// The key the browser puts the layout back under. Not the same as the store's own chat key,
		// which falls back to the control id so that two <agent-chat> elements stay two chats.
		put.accept("layoutKey", _chatId);
		// Null, not false, while the user has decided nothing: the element's configured default is
		// what applies then.
		put.accept("railOpen", preferences.getRailOpen());
		put.accept("paneWidth", Integer.valueOf(preferences.getPaneWidth()));
		put.accept("badgeSeen", Integer.valueOf(preferences.getBadgeSeen()));
		put.accept("sectionChoices", new LinkedHashMap<>(preferences.getSections()));
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

	/**
	 * Resolves a configured text in the acting account's language.
	 *
	 * <p>
	 * The browser gets words, not keys: every element of this module resolves its configured texts
	 * here, on the server, in the session's own locale - the same place {@link #currentLanguage()}
	 * is read from. An unset option is the empty string rather than {@code null}, because the
	 * browser reads such a state entry as "nothing configured" and falls back to its own bundle.
	 * </p>
	 */
	public static String resolveText(ResKey key) {
		if (key == null) {
			return "";
		}
		return Resources.getInstance().getString(key);
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
