/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.top_logic.ai.chat.api.ApprovalChoice;
import com.top_logic.ai.chat.api.ChatPreferencesStore;
import com.top_logic.ai.chat.api.ChatSSEHandler;
import com.top_logic.ai.chat.api.messages.ChatPreferencesDto;
import com.top_logic.ai.chat.api.messages.PromptImprovedEvent;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.ai.chat.layout.WelcomeName;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.environment.EnvironmentFact;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

/**
 * Per-window, per-{@code chat-id} server binding for the chat view elements.
 *
 * <p>
 * What the chat controls of one conversation share in a window: the tracker, the stream, the
 * attached files, the channels the agent may write and what the page states about the situation.
 * It exists once per (sub-session, conversation) and account: a window that shows two
 * conversations has a tracker for each, registered in {@link ChatSessionRegistry}, which the
 * commands of the chat controls of that conversation act on.
 * </p>
 *
 * <p>
 * <b>A binding serves the account it was made for.</b> A login in a window keeps the window's
 * sub-session and changes its person, and the window's tree is built anew for the new person. The
 * chat controls of that tree get a binding of their own then, with the tracker of the new account
 * (see {@link ChatSessionRegistry#tracker(TLSubSessionContext, String, Person)}), so that nothing
 * the previous person had in the window - the open chat, the run, the attached files - is the new
 * one's.
 * </p>
 *
 * <p>
 * <b>What depends on the person or the session is not held here.</b> Whether somebody is signed
 * in, the user's names, the language, the stored layout and the auto-approve choice are read
 * whenever {@link #writeRenderState(BiConsumer)} is asked for them. A browser reload re-renders
 * the control tree the window already holds instead of rebuilding it, so a value captured once
 * would replay the first render's layout for as long as the window lives (Gitea #423), the first
 * render's language until the next login (Gitea #662), and a visitor's sign-in note after the
 * visitor signed in. Reading the layout again costs a lookup in the sub-session's personal
 * configuration, which is already in memory, plus a small JSON parse; the rest is read in place.
 * </p>
 */
public final class ChatViewBinding {

	/** State entry of {@link #writeRenderState(BiConsumer)}: whether somebody is signed in. */
	public static final String STATE_AUTHENTICATED = "authenticated";

	/** State entry of {@link #writeRenderState(BiConsumer)}: the name of the signed-in account. */
	public static final String STATE_USER_NAME = "userName";

	/** State entry of {@link #writeRenderState(BiConsumer)}: the first name the greeting uses. */
	public static final String STATE_WELCOME_NAME = "welcomeName";

	/** State entry of {@link #writeRenderState(BiConsumer)}: the language tag of the session. */
	public static final String STATE_LANGUAGE = "language";

	/** State entry of {@link #writeRenderState(BiConsumer)}: the key the layout is stored under. */
	public static final String STATE_LAYOUT_KEY = "layoutKey";

	/** State entry of {@link #writeRenderState(BiConsumer)}: whether the user left the rail open. */
	public static final String STATE_RAIL_OPEN = "railOpen";

	/** State entry of {@link #writeRenderState(BiConsumer)}: the width the user gave the pane. */
	public static final String STATE_PANE_WIDTH = "paneWidth";

	/** State entry of {@link #writeRenderState(BiConsumer)}: the badge count the user has seen. */
	public static final String STATE_BADGE_SEEN = "badgeSeen";

	/** State entry of {@link #writeRenderState(BiConsumer)}: the rail sections the user left open. */
	public static final String STATE_SECTION_CHOICES = "sectionChoices";

	/** State entry of {@link #writeRenderState(BiConsumer)}: the person's last auto-approve choice. */
	public static final String STATE_APPROVAL_MODE = "approvalMode";

	/** The {@link #STATE_USER_NAME} of a window nobody is signed in to. */
	private static final String DEFAULT_USER_NAME = "User";

	@SuppressWarnings("unchecked")
	private static final Property<Map<String, ChatViewBinding>> BINDINGS =
		TypedAnnotatable.propertyMap("tl.chat.view.bindings");

	/** Loads the layout of a {@code chat-id}. Tests replace this to observe when a load happens. */
	static Function<String, ChatPreferencesDto> preferenceSource = ChatPreferencesStore::load;

	private final TaskProgressTracker _tracker;

	private final String _chatId;

	/**
	 * The conversation this binding's tracker belongs to.
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

	/**
	 * The person this binding serves, <code>null</code> for a window nobody is signed in to.
	 */
	private final Person _account;

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

	/**
	 * What the displayed {@code <chat-conversation>} of this chat states about the situation,
	 * {@code null} while no displayed element states anything.
	 *
	 * @see #offerFacts(Supplier)
	 */
	private volatile Supplier<List<EnvironmentFact>> _facts;

	/**
	 * The files attached to the message being written in this conversation.
	 */
	private final ChatAttachments _attachments = new ChatAttachments();

	/**
	 * The chat controls of this conversation the window displays now.
	 *
	 * <p>
	 * What keeps {@link #_stream} going: the browser holds one stream per conversation for all its
	 * chat elements, and attaches it through whichever of them mounted first. The stream must
	 * therefore outlive the control it was attached through, as long as another chat element of the
	 * conversation is displayed.
	 * </p>
	 */
	private final Set<ReactControl> _shown = Collections.newSetFromMap(new IdentityHashMap<>());

	/**
	 * The stream the browser attached for this conversation, <code>null</code> while it has none.
	 */
	private ChatSSEHandler _stream;

	private ChatViewBinding(TLSubSessionContext subSession, String chatId, String conversation, Person account) {
		_chatId = chatId;
		_conversation = conversation;
		_account = account;
		_tracker = ChatSessionRegistry.tracker(subSession, conversation, account);
	}

	/**
	 * The binding for {@code chatId} in the current window, created on first use.
	 *
	 * <p>
	 * Idempotent per (sub-session, chat-id) and account: a second element that joins the same chat
	 * reuses the tracker.
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
		return bind(TLContextManager.getSubSession(), chatId, conversation);
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
	static ChatViewBinding bind(TypedAnnotatable scope, String chatId) {
		return bind(scope, chatId, chatId);
	}

	/**
	 * The binding of {@code conversation} on {@code scope}, whose layout is stored under
	 * {@code chatId}.
	 *
	 * <p>
	 * Package-visible so a test can pass a {@link TypedAnnotatable} without a live window.
	 * </p>
	 *
	 * <p>
	 * A binding of the scope made for another person than the one acting now - the window's person
	 * before a login in it - is replaced: the chat controls built for the new person act for the
	 * new person.
	 * </p>
	 */
	static ChatViewBinding bind(TypedAnnotatable scope, String chatId, String conversation) {
		String layoutKey = ChatSessionRegistry.key(chatId);
		// Keyed by the conversation, not by the layout key. The two are the same string today; they
		// are read apart so that a chat keyed by something other than its chat-id still gets a
		// tracker and a run of its own.
		String key = ChatSessionRegistry.key(conversation);
		Person account = currentPerson();
		if (scope == null) {
			return new ChatViewBinding(null, layoutKey, key, account);
		}
		Map<String, ChatViewBinding> map = scope.mkMap(BINDINGS);
		synchronized (map) {
			ChatViewBinding existing = map.get(key);
			if (existing != null && Objects.equals(existing._account, account)) {
				return existing;
			}
			TLSubSessionContext subSession = scope instanceof TLSubSessionContext session ? session : null;
			ChatViewBinding created = new ChatViewBinding(subSession, layoutKey, key, account);
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
	 * The conversation this binding's tracker serves - the key it is registered under in
	 * {@link ChatSessionRegistry}.
	 */
	public String conversation() {
		return _conversation;
	}

	/** The person this binding serves, <code>null</code> for a window nobody is signed in to. */
	public Person account() {
		return _account;
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
	 * Read when the conversation comes into being, by {@link #subject()}, in the first send of a
	 * conversation, which creates it ({@link ChatControl#CMD_SEND}): a conversation is about what the
	 * page shows when it is created, and is stamped before its first message is sent.
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
	 * Lets a message sent in this chat be told what the page states about the situation - the
	 * {@code <context>} and {@code <selection/>} of a {@code <chat-conversation>} - for as long as
	 * its element is displayed.
	 *
	 * <p>
	 * Read when a message is sent, by {@link #facts()}, so the run is told what the page states at
	 * that moment.
	 * </p>
	 *
	 * @param facts
	 *        Evaluates the facts as the page states them now.
	 */
	public void offerFacts(Supplier<List<EnvironmentFact>> facts) {
		_facts = facts;
	}

	/**
	 * Undoes one {@link #offerFacts(Supplier)}, for an element that is no longer displayed; a later
	 * offer is left alone.
	 */
	public void withdrawFacts(Supplier<List<EnvironmentFact>> facts) {
		if (_facts == facts) {
			_facts = null;
		}
	}

	/** What the page states about the situation now; empty while it states nothing. */
	public List<EnvironmentFact> facts() {
		Supplier<List<EnvironmentFact>> facts = _facts;
		return facts == null ? List.of() : facts.get();
	}

	/** The files attached to the message being written in this conversation. */
	public ChatAttachments attachments() {
		return _attachments;
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
	 * Notes that a chat control of this conversation is displayed in the window.
	 *
	 * @see #hidden(ReactControl)
	 */
	public void shown(ReactControl control) {
		synchronized (_shown) {
			_shown.add(control);
		}
	}

	/**
	 * Notes that a chat control of this conversation is no longer displayed - detached or disposed -
	 * and stops the conversation's stream when it was the last one.
	 *
	 * <p>
	 * The browser drops a conversation's store, and with it the stream, when the last chat element of
	 * the conversation unmounts; this is the same rule on the server. It is what ends a stream whose
	 * browser cannot say so any more: a page that is reloaded or closed detaches or disposes the
	 * window's controls, while a command it would send on leaving is not sent.
	 * </p>
	 */
	public void hidden(ReactControl control) {
		ChatSSEHandler ended;
		synchronized (_shown) {
			if (!_shown.remove(control) || !_shown.isEmpty()) {
				return;
			}
			ended = _stream;
			_stream = null;
		}
		if (ended != null) {
			ended.stop();
		}
	}

	/**
	 * Starts sending this conversation's frames to the browser window of the given context, to the
	 * listener id of the given token, and stops the stream this conversation had before.
	 *
	 * @param context
	 *        The context of the control the browser asked through; names the window and its queue.
	 * @param token
	 *        The browser's part of the listener id, see {@link ChatSSEHandler#isToken(String)}.
	 * @return Whether the stream was started; <code>false</code> for a token that is not acceptable
	 *         or a context without a window.
	 */
	public boolean attachStream(ReactContext context, String token) {
		SSEUpdateQueue queue = context.getSSEQueue();
		ReactWindowRegistry registry = context.getWindowRegistry();
		if (!ChatSSEHandler.isToken(token) || queue == null || registry == null) {
			return false;
		}
		ChatSSEHandler started =
			new ChatSSEHandler(_tracker, currentPerson(), registry, context.getWindowName(), queue, token);
		ChatSSEHandler replaced;
		synchronized (_shown) {
			replaced = _stream;
			_stream = started;
		}
		started.start();
		if (replaced != null) {
			replaced.stop();
		}
		return true;
	}

	/**
	 * Stops the stream an {@link #attachStream(ReactContext, String)} with the same token started; a
	 * stream a later attach started is left alone.
	 */
	public void detachStream(String token) {
		ChatSSEHandler ended;
		synchronized (_shown) {
			ended = _stream;
			if (ended == null || !ended.writesTo(token)) {
				return;
			}
			_stream = null;
		}
		ended.stop();
	}

	/**
	 * Sends the conversation's {@code run_state} frame again, built as the conversation stands now -
	 * after the window has opened another chat, say. A no-op while the conversation has no stream.
	 */
	public void sendRunState() {
		ChatSSEHandler stream;
		synchronized (_shown) {
			stream = _stream;
		}
		if (stream != null) {
			stream.sendRunState();
		}
	}

	/**
	 * Sends the result of a prompt improvement on the conversation's stream - the one attached when
	 * the result is there, which need not be the one attached when the improvement started.
	 *
	 * <p>
	 * Called from the chat worker that ran the improvement. A conversation that has no stream by then
	 * is not displayed any more, and nobody waits for the result.
	 * </p>
	 */
	public void sendPromptImproved(PromptImprovedEvent event) {
		ChatSSEHandler stream;
		synchronized (_shown) {
			stream = _stream;
		}
		if (stream != null) {
			stream.sendPromptImproved(event);
		}
	}

	/**
	 * Writes what every chat element of this conversation needs and has to be read again on every
	 * render into the control's state: whether somebody is signed in and who, the session's
	 * language, the user's stored layout and auto-approve choice.
	 *
	 * <p>
	 * Every chat control calls this from its constructor and from its {@code onBeforeWrite} hook,
	 * because that is the one moment that happens again when the browser is reloaded. The engine
	 * re-renders the control tree the window already holds, deliberately, so that a table keeps its
	 * selection - which means a value captured at construction time is the value of the first
	 * render forever (Gitea #423). The language follows a switch in the account menu from the next
	 * render on, without a new login (Gitea #662).
	 * </p>
	 */
	public void writeRenderState(BiConsumer<String, Object> put) {
		Person person = TLContext.isAnonymous() ? null : currentPerson();
		// The browser shows a sign-in note instead of a composer while nobody is signed in.
		put.accept(STATE_AUTHENTICATED, Boolean.valueOf(person != null));
		put.accept(STATE_USER_NAME, person == null || person.getName() == null ? DEFAULT_USER_NAME : person.getName());
		put.accept(STATE_WELCOME_NAME, person == null ? "" : valueOr(WelcomeName.of(person), ""));
		// The session's language now, not the one of the first render (Gitea #662).
		put.accept(STATE_LANGUAGE, currentLanguage());
		ChatPreferencesDto preferences = preferences();
		// The key the browser puts the layout back under. Not the same as the store's own chat key,
		// which falls back to the control id so that two elements of one chat-id stay one chat.
		put.accept(STATE_LAYOUT_KEY, _chatId);
		// Null, not false, while the user has decided nothing: the element's configured default is
		// what applies then.
		put.accept(STATE_RAIL_OPEN, preferences.getRailOpen());
		put.accept(STATE_PANE_WIDTH, Integer.valueOf(preferences.getPaneWidth()));
		put.accept(STATE_BADGE_SEEN, Integer.valueOf(preferences.getBadgeSeen()));
		put.accept(STATE_SECTION_CHOICES, new LinkedHashMap<>(preferences.getSections()));
		// The person's last position of the auto-approve switch, which a new chat starts with.
		put.accept(STATE_APPROVAL_MODE, ApprovalChoice.load().name());
	}

	private static Person currentPerson() {
		TLContext tlContext = TLContext.getContext();
		return tlContext == null ? null : tlContext.getPerson();
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
