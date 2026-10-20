/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.top_logic.ai.chat.view.ChatConversationElement.Actions;
import com.top_logic.ai.chat.view.ChatConversationElement.Config;
import com.top_logic.ai.chat.view.ChatConversationElement.Context;
import com.top_logic.ai.chat.view.ChatConversationElement.Controls;
import com.top_logic.ai.chat.view.ChatConversationElement.Fact;
import com.top_logic.ai.chat.view.ChatConversationElement.Mention;
import com.top_logic.ai.chat.view.ChatConversationElement.Prompt;
import com.top_logic.ai.chat.view.ChatConversationElement.Prompts;
import com.top_logic.ai.chat.view.ChatConversationElement.Selection;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ReactParam;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.execution.model.TlAiExecutionFactory;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.util.TLContext;

/**
 * React control for {@code <chat-conversation/>}.
 *
 * <p>
 * Binds the window once through {@link ChatViewBinding}. An optional {@code input} channel selects
 * the open session, mapped by {@link ChatSessionIds}; an optional {@code busy} channel is written
 * from the browser while the agent works. Prompt improvement is a composer concern: this control
 * names the agent on the shared tracker.
 * </p>
 *
 * <p>
 * The four turn settings - {@code agent}, {@code model}, {@code effort}, {@code mcp-servers} - are
 * two-way the same way {@code input} is: the browser reports what the user picked with one
 * {@code publishTurnSettings} command and this control writes it onto the channels, while a write
 * from outside is pushed into the control state for the composer to follow. A value the acting
 * account may not be given is refused by {@link ChatTurnChannels} and the composer's own value is
 * written back, so the channel never claims a setting the composer is not showing.
 * </p>
 *
 * <p>
 * The {@code present} option goes the other way: the channels it names are registered on the
 * binding, which is what makes them reachable from an agent run in this window
 * ({@link com.top_logic.ai.chat.tools.ChatPresentTool}). Deny by default, and registered only while
 * this control is displayed.
 * </p>
 *
 * <p>
 * The {@code draft} channel carries the composer's unsent text the same way, debounced in the
 * browser; {@code reference} is read-only and turns a written model object into one atomic reference
 * token in that draft.
 * </p>
 */
public class ChatConversationControl extends ReactControl {

	private final ChatViewBinding _binding;

	/**
	 * The {@code input} channel and what the browser is told of it, or <code>null</code> when the
	 * element wires none and the browser's store is the source.
	 */
	private final SessionSelection _selection;

	/**
	 * The channel naming what a conversation started here is about, or <code>null</code> when the
	 * element names none.
	 *
	 * @see Config#getSubject()
	 */
	private final ViewChannel _subjectChannel;

	private final ViewChannel _busyChannel;

	private final ViewChannel _agentChannel;

	private final ViewChannel _modelChannel;

	private final ViewChannel _effortChannel;

	private final ViewChannel _mcpChannel;

	private final ViewChannel _draftChannel;

	private final boolean[] _applyingFromChannel = { false };

	/**
	 * How many objects have been written to the {@code reference} channel, so the browser can tell a
	 * new write from the one it has already inserted.
	 *
	 * <p>
	 * A channel does not notify when it is written the value it already holds, but it does notify for
	 * A, B, A - and the third write pushes the same token as the first. Without a counter the browser
	 * would see unchanged state and insert nothing.
	 * </p>
	 */
	private int _referenceWrites;

	/**
	 * The same count for the {@code quote} channel, kept apart from the reference one so that a
	 * write of either channel is a write of its own.
	 *
	 * <p>
	 * Two counters and two state slots, because one slot fed by two channels would lose a token: a
	 * page that clears its quote channel in the same request in which it writes a reference would
	 * overwrite the pending write before the browser ever saw it.
	 * </p>
	 */
	private int _quoteWrites;

	/**
	 * What the composer last reported it is set to, so a channel write can be judged against the
	 * agent in force and corrected back to what the user is looking at.
	 *
	 * <p>
	 * {@code null} until the browser has said - a control lives from the first render, the store from
	 * the first mount.
	 * </p>
	 */
	private String _agent;

	private String _model;

	private List<String> _mcpServers;

	private QueryExecutor _mentionRows;

	private QueryExecutor _mentionLabel;

	private QueryExecutor _mentionText;

	private int _mentionLimit;

	/**
	 * What this page states about the situation, in the order it reaches the model: the
	 * {@code <selection/>} first, then the {@code <context>} facts as configured. Empty when the
	 * element states nothing, which is every one of them until it is configured.
	 */
	private final List<ContextEntry> _facts = new ArrayList<>();

	/** The channels whose values are the leading arguments of every fact expression. */
	private final List<ViewChannel> _factInputs = new ArrayList<>();

	/**
	 * The first questions this page offers, in the order they are shown. Empty when the element
	 * configures no {@code <prompts>}.
	 */
	private final List<PromptSpec> _prompts = new ArrayList<>();

	/** The channels whose values are the leading arguments of every prompt expression. */
	private final List<ViewChannel> _promptInputs = new ArrayList<>();

	/** Creates a {@link ChatConversationControl}. */
	public ChatConversationControl(ViewContext context, Config config) {
		super(context, null, blankToDefault(config.getReactElement(), "TLChatConversation"));
		setCssClass(config.getCssClass());

		// The element's own chat-id when it has one, otherwise the enclosing <chat>'s, otherwise
		// "default". The browser keys its store by what we push here, so it must be the resolved id.
		String chatId = ChatScope.resolve(context, config.getChatId());
		_binding = ChatViewBinding.bind(context, chatId, "chat-conversation");
		String configuredImprover = blankToNull(config.getPromptImproverAgent());
		_binding.tracker().setPromptImproverAgentName(configuredImprover);
		_binding.writeSharedState(this::putState);
		putState("chatId", chatId);
		// Whether this conversation is the one a <chat-dock/> frames. A dock stands down while a
		// conversation of its chat is on the page, and its own must not be what triggers that.
		putState("insideDock", Boolean.valueOf(ChatDockScope.isInsideDock(context)));

		// Transcript. The trace level seeds the store's own axis (state key stays "traceMode"), which
		// the composer's trace picker then changes live.
		putState("traceMode", config.getTrace().getExternalName());
		putState("citations", Boolean.valueOf(config.getCitations()));
		String citationRenderer = config.getCitationRenderer();
		putState("citationRenderer", citationRenderer == null ? "" : citationRenderer.trim());
		Actions actions = config.getActions();
		putState("copy", Boolean.valueOf(actions.getCopy()));
		putState("rate", Boolean.valueOf(actions.getRate()));
		putState("regenerate", Boolean.valueOf(actions.getRegenerate()));
		putState("timestamp", Boolean.valueOf(actions.getTimestamp()));
		List<String> host = actions.getHost();
		putState("hostActions", host == null ? List.of() : List.copyOf(host));

		// Composer.
		putState("promptImprovementEnabled", Boolean.valueOf(configuredImprover != null));
		setupPlaceholder(context, config);
		putState("workingIndicator", ChatIcons.encode(context, config.getWorkingIndicator()));
		String referenceSource = blankToNull(config.getReferenceSource());
		putState("referenceSource", referenceSource == null ? "" : referenceSource);
		// A model-bound <mention> supplies the @ candidates declaratively and wins over the name above.
		Mention mention = config.getMention();
		if (mention != null) {
			setupMention(context, mention);
		}
		// What this page states about the situation. Evaluated here - on the server, in the acting
		// user's context - and pushed to the browser, which sends it back with every message. The
		// user's own text is never touched by any of this.
		// The selection first, because it is the one thing the user is most likely asking about, and
		// the block is read top down.
		setupSelection(context, config.getSelection());
		setupContext(context, config.getContext());
		publishFacts();
		// The other direction: what this page offers the user to ask, evaluated the same way and for
		// the same reason - a card that names the open invoice has to be re-evaluated when another
		// one is opened.
		setupPrompts(context, config.getPrompts());
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

		ChannelRef inputRef = config.getInput();
		// Whether a selection channel is wired. The browser distinguishes "no channel, the store is the
		// source" from "channel wired but empty, which means a fresh conversation".
		putState("hasInput", Boolean.valueOf(inputRef != null));
		if (inputRef == null) {
			_selection = null;
			putState(SessionSelection.SELECTED, "");
		} else {
			ViewChannel input = context.resolveChannel(inputRef);
			_selection = new SessionSelection(input, this::putState, ChatConversationControl::currentPerson);
			ViewChannel.ChannelListener listener = _selection.listener();
			input.addListener(listener);
			addCleanupAction(() -> input.removeListener(listener));
			_selection.apply();
		}

		// The browser reads the open chat's title from its own copy of the chat list; this is what
		// tells it that copy is out of date (#596).
		SessionListWatch sessions = new SessionListWatch(TlAiExecutionFactory.getAgentSessionType(),
			ChatConversationControl::currentPerson, this::putState);
		addAttachListener(() -> sessions.attach(context.getModelScope()));
		addDetachListener(sessions::detach);

		// What a conversation started here is about. Read only when one comes into being, so the
		// browser has to report that - which it does through publishSession, whether or not this
		// element also wires an input channel.
		ChannelRef subjectRef = config.getSubject();
		_subjectChannel = subjectRef == null ? null : context.resolveChannel(subjectRef);
		putState("hasSubject", Boolean.valueOf(_subjectChannel != null));

		ChannelRef busyRef = config.getBusy();
		_busyChannel = busyRef == null ? null : context.resolveChannel(busyRef);

		_agentChannel = followTurnChannel(context, config.getAgent(), this::applyAgentFromChannel);
		_modelChannel = followTurnChannel(context, config.getModel(), this::applyModelFromChannel);
		_effortChannel = followTurnChannel(context, config.getEffort(), this::applyEffortFromChannel);
		_mcpChannel = followTurnChannel(context, config.getMcpServers(), this::applyServersFromChannel);

		allowPresenting(context, config.getPresent());

		// The composer's unsent text, and the objects a host puts into it as reference tokens. Both
		// follow the same rule as the turn settings: a *write* is what the composer follows.
		_draftChannel = followTurnChannel(context, config.getDraft(), this::applyDraftFromChannel);
		// Read-only: a token the composer holds is part of the draft the channel above already
		// carries, so nothing is ever written back and the channel itself is not kept.
		followTurnChannel(context, config.getReference(), this::applyReferenceFromChannel);
		// The same, for a passage of an object rather than the object: read-only for the same reason.
		followTurnChannel(context, config.getQuote(), this::applyQuoteFromChannel);
	}

	/**
	 * Opens the configured channels to an agent running in this window - the {@code present} tool's
	 * allow list - for as long as this control is displayed.
	 *
	 * <p>
	 * Deny by default: what is not named here cannot be reached from a run. And only while displayed:
	 * the registration follows this control's attach and detach, so a page the user has left - a page
	 * of the shell they navigated away from, a classic tab that is not selected, the React page a
	 * browser tab showed before it showed the classic UI - offers no channel, and a run is neither
	 * offered the tool for it nor shows a table nobody sees there (#649). The channels are resolved
	 * here, in the view context, because that is the only place that has one - the agent thread knows a
	 * name and nothing else.
	 * </p>
	 */
	private void allowPresenting(ViewContext context, List<ChannelRef> refs) {
		if (refs == null || refs.isEmpty()) {
			return;
		}
		for (ChannelRef ref : refs) {
			if (ref == null) {
				continue;
			}
			String name = ref.getChannelName();
			ViewChannel channel = context.resolveChannel(ref);
			Runnable open = () -> _binding.allowPresenting(name, channel);
			Runnable close = () -> _binding.denyPresenting(name, channel);
			addAttachListener(open);
			addDetachListener(close);
			addCleanupAction(close);
			if (isAttached()) {
				open.run();
			}
		}
	}

	/**
	 * Resolves one turn-setting channel and makes the composer follow it.
	 *
	 * @param ref
	 *        The configured channel, or <code>null</code> when the element wires none.
	 * @param apply
	 *        What to do with a value written to it, now and on every later write.
	 * @return The resolved channel, or <code>null</code>.
	 */
	private ViewChannel followTurnChannel(ViewContext context, ChannelRef ref, Consumer<Object> apply) {
		if (ref == null) {
			return null;
		}
		ViewChannel channel = context.resolveChannel(ref);
		ViewChannel.ChannelListener listener = (sender, oldValue, newValue) -> apply.accept(newValue);
		channel.addListener(listener);
		addCleanupAction(() -> channel.removeListener(listener));
		apply.accept(channel.get());
		return channel;
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
		_binding.writeRenderState(this::putState);
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		if (_selection != null) {
			_selection.apply();
		}
		reapply(_agentChannel, this::applyAgentFromChannel);
		reapply(_modelChannel, this::applyModelFromChannel);
		reapply(_effortChannel, this::applyEffortFromChannel);
		reapply(_mcpChannel, this::applyServersFromChannel);
		// Deliberately re-applied: a host may have written the draft while this element was on
		// another page of the shell and its browser component was gone. The composer's own text is
		// what stands on the channel by then, so re-applying it is otherwise a no-op.
		reapply(_draftChannel, this::applyDraftFromChannel);
	}

	private static void reapply(ViewChannel channel, Consumer<Object> apply) {
		if (channel != null) {
			apply.accept(channel.get());
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

	/**
	 * Publishes the store's own session onto the selection channel, so a session list highlights it.
	 *
	 * <p>
	 * This makes the {@code input} channel two-way: the conversation follows a selection and writes its
	 * own session back when the store creates one (first send) or restores one (reload). It replaces the
	 * former separate {@code <chat-session-binding/>}. Empty id clears the channel - a fresh conversation
	 * that has not persisted a session yet.
	 * </p>
	 *
	 * <p>
	 * What the browser reports is a proposal: a host may answer the write by putting another
	 * conversation onto the channel, or none, and the browser then follows the channel - see
	 * {@link SessionSelection}.
	 * </p>
	 *
	 * <p>
	 * It is also where a conversation learns what it is about: a session the browser reports as
	 * <em>created</em> here is stamped with the {@code subject} channel's object. The browser has to
	 * say which of the two this is, because the server cannot tell a first send from a restore - a
	 * reload re-opens what this window had, and re-opening a conversation must not move it to
	 * whatever the page is showing now.
	 * </p>
	 *
	 * @param args
	 *        {@code id} is the owned chat session id, or empty to clear; {@code created} says
	 *        whether that session is the one this send brought into being.
	 */
	@ReactCommandHandler(value = "publishSession", params = {
		@ReactParam(name = "id", type = "string", description = "Owned chat session id, or empty to clear."),
		@ReactParam(name = "created", type = "boolean",
			description = "Whether the chat was created by this window, rather than opened.")
	})
	public void publishSession(Map<String, Object> args) {
		String id = args == null ? "" : String.valueOf(args.getOrDefault("id", "")).trim();
		TLObject session = id.isEmpty() ? null : ChatSessionIds.ownedSession(id, currentPerson());
		stampSubject(session, truthy(args == null ? null : args.get("created")));
		if (_selection == null) {
			return;
		}
		_selection.publish(id, session);
	}

	/**
	 * Writes what this page is about onto a conversation that has just come into being.
	 *
	 * <p>
	 * Only on the way out, and only once: the store refuses a session that is already about
	 * something.
	 * </p>
	 */
	private void stampSubject(TLObject session, boolean created) {
		if (_subjectChannel == null || !(session instanceof AgentSession conversation)) {
			return;
		}
		TLObject subject = subjectFor(_subjectChannel.get(), created);
		if (subject == null) {
			return;
		}
		AgentExecutionStore.getInstance().setSubject(conversation, subject);
	}

	/**
	 * What a conversation the browser has just reported is about, or <code>null</code> to stamp
	 * nothing.
	 *
	 * <p>
	 * Three ways to stamp nothing, and each is the honest answer rather than a fallback: a
	 * conversation that was <em>opened</em> rather than created keeps the subject it was started
	 * with, an empty channel means the page was showing nothing, and several objects on it name no
	 * single thing the conversation could be about. A conversation with no subject is simply in no
	 * scoped list.
	 * </p>
	 *
	 * @param channelValue
	 *        What stands on the {@code subject} channel: one object, a one-row selection, several,
	 *        or nothing.
	 * @param created
	 *        Whether this window is what brought the conversation into being.
	 */
	static TLObject subjectFor(Object channelValue, boolean created) {
		return created ? referencedObject(channelValue) : null;
	}

	/**
	 * Reports what the composer is set to for the next turn, onto the four channels a host wired.
	 *
	 * <p>
	 * One command for all four because the composer settles them together: picking an agent also sets
	 * the model it is assigned and the servers it declares, and four commands would put a host
	 * through the intermediate states of one user action.
	 * </p>
	 *
	 * @param args
	 *        {@code agent}, {@code model} and {@code effort} are names, empty for none;
	 *        {@code mcpServers} is the list of attached server names.
	 */
	@ReactCommandHandler(value = "publishTurnSettings", params = {
		@ReactParam(name = "agent", type = "string", description = "Selected agent name, or empty."),
		@ReactParam(name = "model", type = "string", description = "Selected model name, or empty."),
		@ReactParam(name = "effort", type = "string", description = "Selected effort level, or empty."),
		@ReactParam(name = "mcpServers", type = "string[]", description = "Attached MCP server names.")
	})
	public void publishTurnSettings(Map<String, Object> args) {
		if (_applyingFromChannel[0]) {
			return;
		}
		_agent = stringArg(args, "agent");
		_model = stringArg(args, "model");
		_mcpServers = listArg(args, "mcpServers");

		// A value equal to what the channel holds notifies nobody, so the common case - the browser
		// re-reporting what a channel write just told it - stops here.
		setChannel(_agentChannel, _agent);
		setChannel(_modelChannel, _model);
		// No field for the effort: no role gates it, so there is never a refusal to correct.
		setChannel(_effortChannel, stringArg(args, "effort"));
		setChannel(_mcpChannel, _mcpServers);
	}

	/**
	 * Reports the text standing unsent in the composer onto the {@code draft} channel.
	 *
	 * <p>
	 * The browser decides when: on a typing pause, when the field loses the focus, and immediately
	 * before a send. A channel write is a round trip, and a keystroke is not worth one.
	 * </p>
	 *
	 * @param args
	 *        {@code text} is the whole draft, references serialised as they are on send.
	 */
	@ReactCommandHandler(value = "publishDraft", params = {
		@ReactParam(name = "text", type = "string", description = "The composer's unsent text.")
	})
	public void publishDraft(Map<String, Object> args) {
		if (_draftChannel == null || _applyingFromChannel[0]) {
			return;
		}
		_draftChannel.set(args == null ? "" : String.valueOf(args.getOrDefault("text", "")));
	}

	/**
	 * Pushes a draft written from outside at the composer, which replaces its text with it.
	 *
	 * <p>
	 * {@code null} - what an unwritten channel holds - says nothing and leaves the composer alone. An
	 * empty string is an instruction: it clears the draft.
	 * </p>
	 */
	private void applyDraftFromChannel(Object value) {
		if (value == null) {
			putState("draftText", null);
			return;
		}
		applying(() -> putState("draftText", ChatQuotes.text(value)));
	}

	/**
	 * Turns a model object written to the {@code reference} channel into one atomic reference token
	 * for the composer to insert at the caret.
	 *
	 * <p>
	 * The token is the same shape the {@code @} picker inserts, so it serialises to the same
	 * {@code <quote/>} on send. Anything that is not a model object clears the pending write.
	 * </p>
	 */
	private void applyReferenceFromChannel(Object value) {
		TLObject object = referencedObject(value);
		if (object == null) {
			putState("referenceWrite", null);
			return;
		}
		Map<String, Object> fields = tokenOf(object);
		if (fields.isEmpty()) {
			putState("referenceWrite", null);
			return;
		}
		Map<String, Object> token = new LinkedHashMap<>(fields);
		token.put("seq", Integer.valueOf(++_referenceWrites));
		applying(() -> putState("referenceWrite", token));
	}

	/**
	 * Turns a passage written to the {@code quote} channel into one atomic reference token for the
	 * composer to insert at the caret.
	 *
	 * <p>
	 * The sibling of {@link #applyReferenceFromChannel(Object)} for the case it cannot serve: the
	 * user marked a <em>piece</em> of an object in the host's own viewer, and only the host knows
	 * which. The token is the same shape either way, so it serialises to the same {@code <quote/>}
	 * on send - what differs is that its quoted text and its location come from the write rather
	 * than from the element's {@code <mention>} expressions. Anything that carries no passage clears
	 * the pending write; see {@link ChatQuotes#token}.
	 * </p>
	 */
	private void applyQuoteFromChannel(Object value) {
		Map<String, Object> token = ChatQuotes.token(value, this::objectToken);
		if (token == null) {
			putState("quoteWrite", null);
			return;
		}
		token.put("seq", Integer.valueOf(++_quoteWrites));
		applying(() -> putState("quoteWrite", token));
	}

	/** The object a host wrote, also when a selection channel handed over its single row. */
	private static TLObject referencedObject(Object value) {
		if (value instanceof TLObject object) {
			return object;
		}
		if (value instanceof Collection<?> many && many.size() == 1) {
			Object single = many.iterator().next();
			return single instanceof TLObject object ? object : null;
		}
		return null;
	}

	private void applyAgentFromChannel(Object value) {
		String name = ChatTurnChannels.name(value);
		if (name == null) {
			putState("agentSelected", null);
			return;
		}
		if (!ChatTurnChannels.allowsAgent(currentPerson(), name)) {
			refuse(_agentChannel, _agent);
			return;
		}
		applying(() -> putState("agentSelected", name));
	}

	private void applyModelFromChannel(Object value) {
		String name = ChatTurnChannels.name(value);
		if (name == null) {
			putState("modelSelected", null);
			return;
		}
		if (!ChatTurnChannels.allowsModel(currentPerson(), currentAgent(), name)) {
			refuse(_modelChannel, _model);
			return;
		}
		applying(() -> putState("modelSelected", name));
	}

	/**
	 * How hard the model may think, which no role gates: what the providers accept is a property of
	 * the model, and the composer drops a level the selected one does not offer.
	 */
	private void applyEffortFromChannel(Object value) {
		String name = ChatTurnChannels.name(value);
		applying(() -> putState("effortSelected", name));
	}

	private void applyServersFromChannel(Object value) {
		List<String> names = ChatTurnChannels.names(value);
		if (names == null) {
			putState("mcpSelected", null);
			return;
		}
		if (!ChatTurnChannels.allowsServers(currentPerson(), currentAgent(), names)) {
			refuse(_mcpChannel, _mcpServers);
			return;
		}
		applying(() -> putState("mcpSelected", List.copyOf(names)));
	}

	/**
	 * Puts the composer's own setting back onto a channel that was written something forbidden.
	 *
	 * <p>
	 * The refusal itself is logged where it is decided. Writing back matters because the channel is
	 * two-way and a host reads it: left alone it would claim a setting the composer is not showing
	 * and the next send would be answered 403 for. Nothing to write back before the browser has
	 * reported anything, and the composer then simply does not follow.
	 * </p>
	 */
	private void refuse(ViewChannel channel, Object composersOwn) {
		if (channel == null || composersOwn == null) {
			return;
		}
		applying(() -> channel.set(composersOwn));
	}

	/** The agent a model or a server write is judged against: the channel's, else the composer's. */
	private String currentAgent() {
		String wired = _agentChannel == null ? null : ChatTurnChannels.name(_agentChannel.get());
		return wired != null ? wired : _agent;
	}

	private void applying(Runnable action) {
		_applyingFromChannel[0] = true;
		try {
			action.run();
		} finally {
			_applyingFromChannel[0] = false;
		}
	}

	private static void setChannel(ViewChannel channel, Object value) {
		if (channel != null) {
			channel.set(value);
		}
	}

	/** A command argument as a name, or <code>null</code> when it is absent or blank. */
	private static String stringArg(Map<String, Object> args, String key) {
		Object value = args == null ? null : args.get(key);
		return value == null ? null : blankToNull(String.valueOf(value));
	}

	/** A command argument as a list of names; an empty list is an answer, a missing one is not. */
	private static List<String> listArg(Map<String, Object> args, String key) {
		Object value = args == null ? null : args.get(key);
		if (!(value instanceof Collection<?> many)) {
			return null;
		}
		List<String> names = new ArrayList<>(many.size());
		for (Object element : many) {
			String name = element == null ? null : blankToNull(String.valueOf(element));
			if (name != null) {
				names.add(name);
			}
		}
		return List.copyOf(names);
	}

	/**
	 * Compiles the {@code <selection/>} and makes it a per-turn fact.
	 *
	 * <p>
	 * Not {@link Context#getFacts() a fact} the host wrote, but the same thing once it reaches the
	 * model: one line of the {@code <turn-context>} block, re-evaluated whenever the selection
	 * channel changes. What it says is {@link ChatSelection}'s to decide, so that every page states
	 * a selection the same way - label, type and the identifier the tools read back.
	 * </p>
	 */
	private void setupSelection(ViewContext context, Selection selection) {
		if (selection == null) {
			return;
		}
		ViewChannel channel = context.resolveChannel(selection.getChannel());
		QueryExecutor naming =
			selection.getLabel() == null ? null : QueryExecutor.compile(selection.getLabel());
		_facts.add(new SelectionSpec(selection.getName(), channel, naming));
		refreshFactsOn(channel);
	}

	/**
	 * Compiles the {@code <context>} facts and keeps them current.
	 *
	 * <p>
	 * A fact is re-evaluated whenever one of its input channels changes, so what the browser holds is
	 * what the page was showing. The harness renders the stable ones into the environment block of
	 * the system prompt and the rest into the turn; it does not touch the message text.
	 * </p>
	 */
	private void setupContext(ViewContext context, Context facts) {
		if (facts == null || facts.getFacts().isEmpty()) {
			// A <context> that states no fact wires no input either: the inputs exist to be the
			// arguments of expressions there are none of.
			return;
		}
		for (Fact fact : facts.getFacts()) {
			_facts.add(new FactSpec(fact.getName(), fact.getStable(), QueryExecutor.compile(fact.getValue())));
		}
		for (ChannelRef ref : facts.getInputs()) {
			ViewChannel channel = context.resolveChannel(ref);
			_factInputs.add(channel);
			refreshFactsOn(channel);
		}
	}

	/** Re-evaluates everything this page states whenever the given channel changes. */
	private void refreshFactsOn(ViewChannel channel) {
		ViewChannel.ChannelListener listener = (sender, oldValue, newValue) -> serializeFacts();
		channel.addListener(listener);
		addCleanupAction(() -> channel.removeListener(listener));
	}

	/** Publishes what this page states, once both sources have been compiled. */
	private void publishFacts() {
		if (_facts.isEmpty()) {
			return;
		}
		serializeFacts();
		// A channel written while this control was detached notified nobody, so the values are read
		// again when it comes back - the same reason the mention candidates are.
		addAttachListener(this::serializeFacts);
	}

	/** Evaluates every fact and pushes the result to the browser. */
	private void serializeFacts() {
		Object[] arguments = new Object[_factInputs.size()];
		for (int n = 0; n < arguments.length; n++) {
			arguments[n] = _factInputs.get(n).get();
		}
		List<Map<String, Object>> out = new ArrayList<>(_facts.size());
		for (ContextEntry fact : _facts) {
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put("name", fact.name());
			entry.put("value", fact.evaluate(arguments));
			entry.put("stable", Boolean.valueOf(fact.stable()));
			out.add(entry);
		}
		putState("contextFacts", out);
	}

	/**
	 * Compiles the {@code <prompts>} cards and keeps their texts current.
	 *
	 * <p>
	 * A card's text is evaluated here rather than on the click, for the same reason a
	 * {@code <fact>} is: the expression reads the page's channels, and the page is the server's to
	 * read. The browser then holds a finished sentence, so clicking a card costs no round trip -
	 * the composer is filled in the same tick as the caret moves.
	 * </p>
	 */
	private void setupPrompts(ViewContext context, Prompts prompts) {
		if (prompts == null || prompts.getCards().isEmpty()) {
			// A <prompts> that offers no card wires no input either: the inputs exist to be the
			// arguments of expressions there are none of.
			return;
		}
		for (Prompt prompt : prompts.getCards()) {
			_prompts.add(new PromptSpec(ChatViewBinding.resolveText(prompt.getLabel()),
				ChatIcons.encode(context, prompt.getIcon()), QueryExecutor.compile(prompt.getValue())));
		}
		for (ChannelRef ref : prompts.getInputs()) {
			ViewChannel channel = context.resolveChannel(ref);
			_promptInputs.add(channel);
			ViewChannel.ChannelListener listener = (sender, oldValue, newValue) -> serializePrompts();
			channel.addListener(listener);
			addCleanupAction(() -> channel.removeListener(listener));
		}
		serializePrompts();
		// A channel written while this control was detached notified nobody, the same reason the
		// facts and the mention candidates are read again on the way back.
		addAttachListener(this::serializePrompts);
	}

	/**
	 * Pushes the placeholder of the prompt field, and keeps it current when it is computed from the
	 * page - see {@link ChatConversationElement.Config#getPlaceholderBy()}.
	 */
	private void setupPlaceholder(ViewContext context, ChatConversationElement.Config config) {
		String fixed = ChatViewBinding.resolveText(config.getPlaceholder());
		ChatConversationElement.PlaceholderBy by = config.getPlaceholderBy();
		if (by == null) {
			putState("placeholder", fixed);
			return;
		}
		QueryExecutor value = QueryExecutor.compile(by.getValue());
		List<ViewChannel> inputs = new ArrayList<>();
		Runnable publish = () -> {
			Object[] arguments = new Object[inputs.size()];
			for (int n = 0; n < arguments.length; n++) {
				arguments[n] = inputs.get(n).get();
			}
			String computed;
			try {
				computed = label(value.execute(arguments));
			} catch (RuntimeException ex) {
				Logger.warn("Chat placeholder could not be evaluated: " + ex.getMessage(), ex,
					ChatConversationControl.class);
				computed = "";
			}
			putState("placeholder", computed.isBlank() ? fixed : computed);
		};
		for (ChannelRef ref : by.getInputs()) {
			ViewChannel channel = context.resolveChannel(ref);
			inputs.add(channel);
			ViewChannel.ChannelListener listener = (sender, oldValue, newValue) -> publish.run();
			channel.addListener(listener);
			addCleanupAction(() -> channel.removeListener(listener));
		}
		publish.run();
		// A channel written while this control was detached notified nobody, as for the cards.
		addAttachListener(publish);
	}

	/** Evaluates every card's text and pushes the set to the browser. */
	private void serializePrompts() {
		Object[] arguments = new Object[_promptInputs.size()];
		for (int n = 0; n < arguments.length; n++) {
			arguments[n] = _promptInputs.get(n).get();
		}
		List<Map<String, Object>> out = new ArrayList<>(_prompts.size());
		for (PromptSpec prompt : _prompts) {
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put("label", prompt.label());
			entry.put("icon", prompt.icon());
			entry.put("text", prompt.evaluate(arguments));
			out.add(entry);
		}
		putState("prompts", out);
	}

	/**
	 * One compiled {@code <prompt>}: its label and icon, which the configuration fixed, and the
	 * expression that says what clicking it puts into the composer.
	 *
	 * <p>
	 * A card whose expression throws is pushed with an empty text and the browser leaves it out -
	 * the same trade the facts make. A mistyped expression costs the user one card; it does not
	 * cost them the page they were reading.
	 * </p>
	 */
	private record PromptSpec(String label, String icon, QueryExecutor value) {

		/**
		 * The text as it stands now.
		 *
		 * @param arguments
		 *        The current values of the {@code <prompts>} inputs, in configured order.
		 */
		String evaluate(Object[] arguments) {
			try {
				// Qualified: the record's own `label` component would otherwise shadow the helper.
				return ChatConversationControl.label(value.execute(arguments));
			} catch (RuntimeException ex) {
				Logger.warn("Chat prompt card '" + label + "' could not be evaluated: " + ex.getMessage(),
					ex, ChatConversationControl.class);
				return "";
			}
		}
	}

	/**
	 * One line this page states about the situation, whoever wrote it.
	 *
	 * <p>
	 * Two implementations - a host's {@code <fact>} and the {@code <selection/>} - because they are
	 * one thing to everything downstream: the browser echoes them back with the message and the
	 * harness sorts them into the system prompt or the turn by {@link #stable()} alone.
	 * </p>
	 */
	private interface ContextEntry {

		/** What the fact is called in front of the model. */
		String name();

		/** Whether the value holds for the whole conversation. */
		boolean stable();

		/**
		 * The value as it stands now.
		 *
		 * @param arguments
		 *        The current values of the {@code <context>} inputs, in configured order.
		 */
		String evaluate(Object[] arguments);
	}

	/**
	 * One compiled {@code <fact>}.
	 *
	 * <p>
	 * A fact that throws is sent as an empty value rather than taking the page down: a mistyped
	 * expression costs the agent one line of context, and the user is in the middle of a sentence.
	 * </p>
	 */
	private record FactSpec(String name, boolean stable, QueryExecutor value) implements ContextEntry {

		@Override
		public String evaluate(Object[] arguments) {
			try {
				return label(value.execute(arguments));
			} catch (RuntimeException ex) {
				Logger.warn("Chat context fact '" + name + "' could not be evaluated: " + ex.getMessage(),
					ex, ChatConversationControl.class);
				return "";
			}
		}
	}

	/**
	 * The compiled {@code <selection/>}: what stands on the page's selection channel, right now.
	 *
	 * <p>
	 * It reads its own channel rather than an argument, because the channel is its own and not one
	 * of the {@code <context>} inputs - a page states a selection without writing a {@code <context>}
	 * at all.
	 * </p>
	 */
	private record SelectionSpec(String name, ViewChannel channel, QueryExecutor naming)
			implements ContextEntry {

		@Override
		public boolean stable() {
			// Never. What the user has selected is the value that changes most often in a
			// conversation, and the environment block has to stay byte-identical across its turns.
			return false;
		}

		@Override
		public String evaluate(Object[] arguments) {
			try {
				return ChatSelection.describe(channel.get(),
					naming == null ? null : object -> label(naming.execute(object)));
			} catch (RuntimeException ex) {
				Logger.warn("The chat could not state the selection '" + name + "': " + ex.getMessage(),
					ex, ChatConversationControl.class);
				return "";
			}
		}
	}

	/**
	 * Compiles the {@code <mention>} expressions, pushes the initial candidate set and keeps it live.
	 *
	 * <p>
	 * The candidates are shaped exactly as the browser's {@code ReferenceCandidate}, so the composer's
	 * existing {@code @} picker consumes them unchanged. The filtering as the user types happens in the
	 * browser, so {@link Mention#getLimit()} bounds the payload.
	 * </p>
	 */
	private void setupMention(ViewContext context, Mention mention) {
		_mentionRows = QueryExecutor.compile(mention.getRows());
		_mentionLabel = mention.getLabel() == null ? null : QueryExecutor.compile(mention.getLabel());
		_mentionText = mention.getText() == null ? null : QueryExecutor.compile(mention.getText());
		_mentionLimit = Math.max(1, mention.getLimit());
		Set<TLStructuredType> observed = resolveObservedTypes(mention.getObservedTypes());

		List<Object> initial = new ArrayList<>(executeRows(_mentionRows));
		serializeCandidates(initial);

		RowSourceObserver<Object> observer = new RowSourceObserver<>(
			initial,
			args -> new ArrayList<>(executeRows(_mentionRows)),
			observed,
			List.of(),
			this::serializeCandidates);
		// Re-run on (re)attach as well: an object created while the user was on another view (the
		// observer detached) would otherwise be missed until a full reload.
		addAttachListener(() -> {
			observer.attach(context.getModelScope());
			serializeCandidates(executeRows(_mentionRows));
		});
		addDetachListener(observer::detach);
	}

	/** Serialises the mention candidates to browser state, capped at the configured limit. */
	private void serializeCandidates(Collection<?> rows) {
		List<Map<String, Object>> out = new ArrayList<>();
		int count = 0;
		for (Object row : rows) {
			if (count >= _mentionLimit) {
				break;
			}
			if (!(row instanceof TLObject object)) {
				continue;
			}
			Map<String, Object> token = tokenOf(object);
			if (token.isEmpty()) {
				continue;
			}

			Map<String, Object> candidate = new LinkedHashMap<>();
			candidate.put("id", token.get("id"));
			candidate.put("label", token.get("label"));
			candidate.put("token", token);
			out.add(candidate);
			count++;
		}
		putState("referenceCandidates", out);
	}

	private static Collection<?> executeRows(QueryExecutor rows) {
		Object result = rows.execute();
		if (result instanceof Collection<?> collection) {
			return collection;
		}
		return result == null ? Collections.emptyList() : Collections.singletonList(result);
	}

	private static Set<TLStructuredType> resolveObservedTypes(List<TLModelPartRef> refs) {
		if (refs == null || refs.isEmpty()) {
			return Set.of();
		}
		Set<TLStructuredType> types = new HashSet<>();
		for (TLModelPartRef ref : refs) {
			TLStructuredType type = (TLStructuredType) ref.resolveType();
			if (type == null) {
				throw new RuntimeException("Failed to resolve observed type: " + ref.qualifiedName());
			}
			types.add(type);
		}
		return types;
	}

	/**
	 * One object as the browser's reference token: what {@code @} inserts and what the
	 * {@code reference} channel writes.
	 *
	 * <p>
	 * A nested {@code <mention>} says how this application labels an object and what quoted text it
	 * puts into the message; without one the object's own label is both. Empty for an object with no
	 * stable id - there would be nothing for a host tool to resolve.
	 * </p>
	 */
	private Map<String, Object> tokenOf(TLObject object) {
		String id = idOf(object);
		if (id.isEmpty()) {
			return Map.of();
		}
		String label = _mentionLabel != null ? label(_mentionLabel.execute(object)) : label(object);
		Map<String, Object> token = new LinkedHashMap<>();
		// The qualified type and the identifier the tools read (#616): with them the agent can answer
		// about a referenced object without a call, or read it with getInstance in one.
		token.put("kind", object.tType() == null ? "" : TLModelUtil.qualifiedName(object.tType()));
		token.put("id", id);
		token.put("label", label);
		token.put("text", _mentionText != null ? label(_mentionText.execute(object)) : label);
		return token;
	}

	/**
	 * The same for a value a channel handed over, which is a model object or nothing this element
	 * can make a token of.
	 */
	private Map<String, Object> objectToken(Object value) {
		return value instanceof TLObject object ? tokenOf(object) : Map.of();
	}

	/**
	 * The id a mention carries: the {@code TypeName:id} every tool reads back
	 * ({@link ReadFilter#idOf(TLObject)}), so a referenced object is one {@code getInstance} away.
	 */
	private static String idOf(TLObject object) {
		if (object == null || object.tHandle() == null) {
			return "";
		}
		String id = ReadFilter.idOf(object);
		return id == null ? "" : id;
	}

	/** A display label for an expression result (formats dates, objects and text uniformly). */
	private static String label(Object value) {
		return value == null ? "" : MetaLabelProvider.INSTANCE.getLabel(value);
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
