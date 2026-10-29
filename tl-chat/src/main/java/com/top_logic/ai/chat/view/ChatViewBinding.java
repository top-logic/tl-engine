/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.top_logic.ai.chat.api.ApprovalChoice;
import com.top_logic.ai.chat.api.ChatPreferencesStore;
import com.top_logic.ai.chat.api.messages.ChatPreferencesDto;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.ai.chat.layout.WelcomeName;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

/**
 * Per-window, per-{@code chat-id} server binding for the chat view elements.
 *
 * <p>
 * Every chat control used to bind the tracker, the user and the layout preferences in every
 * constructor. Four elements on one chat would then load {@link ChatPreferencesStore} four times
 * and race on one {@code PersonalConfiguration} key. This class does that work once per
 * (sub-session, conversation), and the tracker with it: a window that shows two conversations has a
 * tracker for each, which the {@link com.top_logic.ai.chat.api.ChatServlet} resolves from the
 * {@code windowId} and the {@code chatId} the client sends - see {@link ChatSessionRegistry}.
 * </p>
 *
 * <p>
 * The layout preference and the session's language are the things here that are <em>not</em> bound
 * once: they are read whenever {@link #writeRenderState(BiConsumer)} is asked for them. A browser
 * reload re-renders the control tree the window already holds instead of rebuilding it, so a value
 * captured in a constructor would replay the first render's layout for as long as the window lives
 * (Gitea #423) - and the first render's language, until the next login (Gitea #662). Reading the
 * layout again costs a lookup in the sub-session's personal configuration, which is already in
 * memory, plus a small JSON parse; the language is the session's locale, read in place.
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

	/**
	 * The conversation this binding's tracker belongs to - what the client echoes as {@code chatId}.
	 *
	 * <p>
	 * The same string as {@link #_chatId} for every element there is: a chat's conversation is its
	 * {@code chat-id}. It is a key of its own because the two answer different questions - the
	 * layout is stored per {@code chat-id}, a tracker and a run belong to one conversation - and a
	 * chat that shared its conversation with nobody would key them differently. The one-element
	 * {@code <agent-chat/>} was such a chat until #487 removed it.
	 * </p>
	 */
	private final String _conversation;

	private final String _windowId;

	private final String _contextPath;

	private final String _userName;

	private final String _welcomeName;

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

	/**
	 * The channel holding what a conversation created in this chat is about, {@code null} while
	 * no displayed element of the chat names one.
	 *
	 * @see #offerSubject(ViewChannel)
	 */
	private volatile ViewChannel _subject;

	private ChatViewBinding(TLSubSessionContext subSession, String windowName, String contextPath, String chatId,
			String conversation) {
		_chatId = chatId;
		_conversation = conversation;
		_tracker = bindTracker(subSession, windowName, conversation);
		storeUser(_tracker);
		_windowId = windowName;
		_contextPath = contextPath == null ? "" : contextPath;
		_userName = valueOr(_tracker.getUserName(), "User");
		_welcomeName = valueOr(_tracker.getWelcomeName(), "");
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
	private static ChatViewBinding bind(ReactContext context, String chatId) {
		return bindConversation(context, chatId, chatId);
	}

	/**
	 * The binding of {@code conversation}, whose layout is stored under {@code chatId}.
	 *
	 * <p>
	 * The two are the same string for every element there is: a chat's conversation is its
	 * {@code chat-id}. They are separate parameters because the tracker and the run are keyed by the
	 * conversation while the layout is keyed by the {@code chat-id} (#502), and a chat that shared
	 * its conversation with nobody would key them differently - the one-element {@code <agent-chat/>}
	 * did exactly that before #487 removed it.
	 * </p>
	 */
	private static ChatViewBinding bindConversation(ReactContext context, String chatId, String conversation) {
		return bind(TLContextManager.getSubSession(), context.getWindowName(), context.getContextPath(), chatId,
			conversation);
	}

	/**
	 * The binding for {@code chatId}, recording that {@code element} joined that conversation.
	 *
	 * <p>
	 * This is what the elements that share a conversation by {@code chat-id} use. The join is
	 * tallied per render and logged when the render ends, so that an element which found nobody to
	 * join says so instead of rendering an empty box (see {@link ChatJoinLog}).
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
		return bind(scope, windowName, contextPath, chatId, chatId);
	}

	/**
	 * The binding of {@code conversation} on {@code scope}, whose layout is stored under
	 * {@code chatId}.
	 *
	 * <p>
	 * Package-visible so a test can pass a {@link TypedAnnotatable} without a live window.
	 * </p>
	 */
	static ChatViewBinding bind(TypedAnnotatable scope, String windowName, String contextPath, String chatId,
			String conversation) {
		String layoutKey = ChatSessionRegistry.key(chatId);
		// Keyed by the conversation, not by the layout key. The two are the same string today; they
		// are read apart so that a chat keyed by something other than its chat-id still gets a
		// tracker and a run of its own.
		String key = ChatSessionRegistry.key(conversation);
		if (scope == null) {
			return new ChatViewBinding(null, windowName, contextPath, layoutKey, key);
		}
		Map<String, ChatViewBinding> map = scope.mkMap(BINDINGS);
		synchronized (map) {
			ChatViewBinding existing = map.get(key);
			if (existing != null) {
				return existing;
			}
			TLSubSessionContext subSession = scope instanceof TLSubSessionContext session ? session : null;
			ChatViewBinding created = new ChatViewBinding(subSession, windowName, contextPath, layoutKey, key);
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

	/**
	 * The conversation this binding's tracker serves - what the client echoes as {@code chatId} on
	 * every {@code /chat/*} call.
	 */
	public String conversation() {
		return _conversation;
	}

	/** User name shown in the chat. */
	public String userName() {
		return _userName;
	}

	/** First name used in the empty-state greeting. */
	public String welcomeName() {
		return _welcomeName;
	}

	/** The language tag of the session's locale now, e.g. {@code "de"}; read per render, not at bind time. */
	public String language() {
		return currentLanguage();
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

	/**
	 * Lets a conversation created in this chat be about the object on the given channel - the
	 * {@code subject} of a {@code <chat-conversation>} - for as long as its element is displayed.
	 *
	 * <p>
	 * Read when the conversation comes into being, by {@link #subjectOf(TypedAnnotatable, String)},
	 * on the request that creates it: a conversation is about what the page shows when it is
	 * created, and is stamped before its first message is sent.
	 * </p>
	 */
	public void offerSubject(ViewChannel channel) {
		_subject = channel;
	}

	/**
	 * Undoes one {@link #offerSubject(ViewChannel)}, for an element that is no longer displayed; a
	 * later offer of another channel is left alone.
	 */
	public void withdrawSubject(ViewChannel channel) {
		if (_subject == channel) {
			_subject = null;
		}
	}

	/**
	 * What a conversation created now in this chat is about, or <code>null</code> for nothing.
	 *
	 * @see #singleObject(Object)
	 */
	public TLObject subject() {
		ViewChannel channel = _subject;
		return channel == null ? null : singleObject(channel.get());
	}

	/**
	 * What a conversation created now in the chat {@code conversation} of {@code scope}'s window is
	 * about, or <code>null</code> when the window shows no such chat or the chat names no subject.
	 *
	 * @param scope
	 *        The window's sub-session. May be <code>null</code>, which names nothing.
	 * @param conversation
	 *        The conversation of the window, as the client names it.
	 */
	public static TLObject subjectOf(TypedAnnotatable scope, String conversation) {
		if (scope == null) {
			return null;
		}
		Map<String, ChatViewBinding> map = scope.get(BINDINGS);
		if (map == null) {
			return null;
		}
		ChatViewBinding binding;
		synchronized (map) {
			binding = map.get(ChatSessionRegistry.key(conversation));
		}
		return binding == null ? null : binding.subject();
	}

	/**
	 * The one model object a channel value names: the object itself, or the single row of a
	 * selection; <code>null</code> for nothing, several objects or anything that is no object.
	 *
	 * <p>
	 * As a subject: an empty channel means the page was showing nothing, and several objects on it
	 * name no single thing the conversation could be about; neither stamps a subject. A conversation
	 * with no subject is simply in no scoped list.
	 * </p>
	 *
	 * @param channelValue
	 *        What stands on a channel: one object, a one-row selection, several, or nothing.
	 */
	public static TLObject singleObject(Object channelValue) {
		if (channelValue instanceof TLObject object) {
			return object;
		}
		if (channelValue instanceof Collection<?> many && many.size() == 1) {
			Object single = many.iterator().next();
			return single instanceof TLObject object ? object : null;
		}
		return null;
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
	 * Everything written here is fixed for the lifetime of the window. The layout preference and
	 * the session's language are not, so they have {@link #writeRenderState(BiConsumer)} of their
	 * own; this method writes them too, so that a caller which only ever renders once needs the
	 * one call.
	 * </p>
	 */
	public void writeSharedState(BiConsumer<String, Object> put) {
		put.accept("authenticated", Boolean.valueOf(_authenticated));
		put.accept("windowId", _windowId);
		put.accept("contextPath", _contextPath);
		put.accept("userName", _userName);
		put.accept("welcomeName", _welcomeName);
		writeRenderState(put);
	}

	/**
	 * Writes what has to be read again on every render into the control's state: the user's stored
	 * layout and auto-approve choice, and the session's language.
	 *
	 * <p>
	 * Every chat control calls this from its {@code onBeforeWrite} hook, because that is the one
	 * moment that happens again when the browser is reloaded. The constructor does not: the engine
	 * re-renders the control tree the window already holds, deliberately, so that a table keeps its
	 * selection - which means a value captured at construction time is the value of the first
	 * render forever (Gitea #423). The language is written here for the same reason (Gitea #662):
	 * a user who switches it in the account menu is answered in the new language from the next
	 * render on, without a new login.
	 * </p>
	 */
	public void writeRenderState(BiConsumer<String, Object> put) {
		// The session's language now, not the one of the first render (Gitea #662).
		put.accept("language", currentLanguage());
		ChatPreferencesDto preferences = preferences();
		// The key the browser puts the layout back under. Not the same as the store's own chat key,
		// which falls back to the control id so that two elements of one chat-id stay one chat.
		put.accept("layoutKey", _chatId);
		// Null, not false, while the user has decided nothing: the element's configured default is
		// what applies then.
		put.accept("railOpen", preferences.getRailOpen());
		put.accept("paneWidth", Integer.valueOf(preferences.getPaneWidth()));
		put.accept("badgeSeen", Integer.valueOf(preferences.getBadgeSeen()));
		put.accept("sectionChoices", new LinkedHashMap<>(preferences.getSections()));
		// The person's last position of the auto-approve switch, which a new chat starts with.
		put.accept("approvalMode", ApprovalChoice.load().name());
	}

	private static TaskProgressTracker bindTracker(TLSubSessionContext subSession, String windowName,
			String conversation) {
		TLSessionContext session = subSession == null ? null : subSession.getSessionContext();
		TaskProgressTracker tracker = ChatSessionRegistry.resolveTracker(session, windowName, conversation);
		if (tracker == null) {
			tracker = new TaskProgressTracker();
		}
		tracker.setWindowId(windowName);
		ChatSessionRegistry.register(subSession, conversation, tracker);
		return tracker;
	}

	private static void storeUser(TaskProgressTracker tracker) {
		TLContext tlContext = TLContext.getContext();
		Person person = tlContext == null ? null : tlContext.getPerson();
		if (person == null) {
			return;
		}
		tracker.setUserName(person.getName());
		tracker.setWelcomeName(WelcomeName.of(person));
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
