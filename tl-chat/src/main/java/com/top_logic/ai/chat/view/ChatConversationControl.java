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
import com.top_logic.ai.chat.view.ChatConversationElement.Controls;
import com.top_logic.ai.chat.view.ChatConversationElement.Mention;
import com.top_logic.basic.util.ResKey;
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
import com.top_logic.util.Resources;
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
 */
public class ChatConversationControl extends ReactControl {

	private final ChatViewBinding _binding;

	private final ViewChannel _inputChannel;

	private final ViewChannel _busyChannel;

	private final ViewChannel _agentChannel;

	private final ViewChannel _modelChannel;

	private final ViewChannel _effortChannel;

	private final ViewChannel _mcpChannel;

	private final boolean[] _applyingFromChannel = { false };

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

	/** Creates a {@link ChatConversationControl}. */
	public ChatConversationControl(ViewContext context, Config config) {
		super(context, null, blankToDefault(config.getReactElement(), "TLChatConversation"));

		// The element's own chat-id when it has one, otherwise the enclosing <chat>'s, otherwise
		// "default". The browser keys its store by what we push here, so it must be the resolved id.
		String chatId = ChatScope.resolve(context, config.getChatId());
		_binding = ChatViewBinding.bind(context, chatId, "chat-conversation");
		String configuredImprover = blankToNull(config.getPromptImproverAgent());
		_binding.tracker().setPromptImproverAgentName(configuredImprover);
		_binding.writeSharedState(this::putState);
		putState("chatId", chatId);

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
		putState("placeholder", resolvePlaceholder(config.getPlaceholder()));
		String referenceSource = blankToNull(config.getReferenceSource());
		putState("referenceSource", referenceSource == null ? "" : referenceSource);
		// A model-bound <mention> supplies the @ candidates declaratively and wins over the name above.
		Mention mention = config.getMention();
		if (mention != null) {
			setupMention(context, mention);
		}
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
			_inputChannel = null;
			putState("selected", "");
		} else {
			_inputChannel = context.resolveChannel(inputRef);
			ViewChannel.ChannelListener listener = (sender, oldValue, newValue) -> applyFromChannel(newValue);
			_inputChannel.addListener(listener);
			addCleanupAction(() -> _inputChannel.removeListener(listener));
			applyFromChannel(_inputChannel.get());
		}

		ChannelRef busyRef = config.getBusy();
		_busyChannel = busyRef == null ? null : context.resolveChannel(busyRef);

		_agentChannel = followTurnChannel(context, config.getAgent(), this::applyAgentFromChannel);
		_modelChannel = followTurnChannel(context, config.getModel(), this::applyModelFromChannel);
		_effortChannel = followTurnChannel(context, config.getEffort(), this::applyEffortFromChannel);
		_mcpChannel = followTurnChannel(context, config.getMcpServers(), this::applyServersFromChannel);
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
		_binding.writeLayoutState(this::putState);
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		if (_inputChannel != null) {
			applyFromChannel(_inputChannel.get());
		}
		reapply(_agentChannel, this::applyAgentFromChannel);
		reapply(_modelChannel, this::applyModelFromChannel);
		reapply(_effortChannel, this::applyEffortFromChannel);
		reapply(_mcpChannel, this::applyServersFromChannel);
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
	 * @param args
	 *        {@code id} is the owned chat session id, or empty to clear.
	 */
	@ReactCommandHandler(value = "publishSession", params = {
		@ReactParam(name = "id", type = "string", description = "Owned chat session id, or empty to clear.")
	})
	public void publishSession(Map<String, Object> args) {
		if (_inputChannel == null || _applyingFromChannel[0]) {
			return;
		}
		String id = args == null ? "" : String.valueOf(args.getOrDefault("id", "")).trim();
		TLObject session = id.isEmpty() ? null : ChatSessionIds.ownedSession(id, currentPerson());
		_inputChannel.set(session);
		putState("selected", session == null ? "" : ChatSessionIds.idOf(session));
	}

	private void applyFromChannel(Object value) {
		_applyingFromChannel[0] = true;
		try {
			putState("selected", ChatSessionIds.selectedId(value, currentPerson()));
		} finally {
			_applyingFromChannel[0] = false;
		}
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
			String id = idOf(object);
			if (id.isEmpty()) {
				continue;
			}
			String label = _mentionLabel != null ? label(_mentionLabel.execute(object)) : label(object);
			String text = _mentionText != null ? label(_mentionText.execute(object)) : label;
			String kind = object.tType() == null ? "" : object.tType().getName();

			Map<String, Object> token = new LinkedHashMap<>();
			token.put("kind", kind);
			token.put("id", id);
			token.put("label", label);
			token.put("text", text);

			Map<String, Object> candidate = new LinkedHashMap<>();
			candidate.put("id", id);
			candidate.put("label", label);
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

	/** The browser id of an object: its stable knowledge-object name (the id a mention carries). */
	private static String idOf(TLObject object) {
		if (object == null || object.tHandle() == null) {
			return "";
		}
		return object.tHandle().getObjectName().toExternalForm();
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
