/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.CommaSeparatedChannelRefs;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.tl.ai.core.provider.LLMService.AllAgentNameOptions;

/**
 * Declarative {@code <chat-conversation/>} element: the transcript and the prompt field as one view
 * element.
 *
 * <p>
 * Merges what used to be the separate {@code <chat-thread/>} and {@code <chat-composer/>} elements.
 * Transcript and input almost always sit together, so they are one placeable box; a session list (a
 * {@code <data-list>} of {@code AgentSession}s) and a rail ({@code <chat-rail/>}) stay separate
 * because those genuinely want independent placement. Several chat elements on one page join the
 * same conversation through {@code chat-id}. The optional {@code input} channel carries the open
 * {@code AgentSession}; the optional {@code busy} channel is written {@code true} while the agent
 * works, so a host can disable its own commands.
 * </p>
 *
 * <p>
 * Four further channels carry what the user set up for the next turn: {@code agent}, {@code model},
 * {@code effort} and {@code mcp-servers}. Each is two-way - the composer writes the user's choice and
 * follows a write from outside - so a host can show which agent answers, gate a button on the model,
 * or pre-select an agent for a page. They carry the <em>names</em> these things are known by, because
 * a name is what every request, and the security policy behind it, is written in: only an agent
 * authored in the knowledge base is a model object at all, while the shipped agents, the models and
 * the MCP servers are configuration. See {@link ChatTurnChannels}.
 * </p>
 *
 * <p>
 * {@code present} is the other direction and the only one the <em>agent</em> drives: the channels
 * named there are the ones a run may write with the {@code present} tool, and nothing else on the
 * page is reachable from a run. <b>Deny by default</b> - an element that names none lets no agent
 * write any channel. See {@link Config#getPresent()} and
 * {@link com.top_logic.ai.chat.tools.PresentTool}.
 * </p>
 *
 * <p>
 * Two more carry the composer's unsent text: {@code draft} is the text itself, two-way, and
 * {@code reference} turns a written model object into one atomic reference token inside it. Together
 * they are what lets a button anywhere on the page put a question into the chat without a line of
 * code - an {@code <execute-script>} that builds the sentence and a {@code <write-channel>} that
 * writes it.
 * </p>
 */
public class ChatConversationElement implements UIElement {

	/**
	 * How much of the agent's execution the transcript shows.
	 *
	 * <p>
	 * One knob: it replaces the former separate trace-mode and run-summary. {@link #SUMMARY} is the
	 * collapsed-but-expandable run summary the user clicks to open.
	 * </p>
	 */
	public enum Trace implements ExternallyNamed {

		/** Only the answer; no execution detail. */
		NONE("none"),

		/** A collapsed one-line run summary the user can expand. */
		SUMMARY("summary"),

		/** The full execution trace, inline. */
		DETAILED("detailed");

		private final String _externalName;

		Trace(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/** Configuration for {@link ChatConversationElement}. */
	@TagName("chat-conversation")
	public interface Config extends UIElement.Config {

		/**
		 * Configuration property name for {@link #getChatId()}.
		 *
		 * @see #getChatId()
		 */
		String CHAT_ID = "chat-id";

		/**
		 * Configuration property name for {@link #getInput()}.
		 *
		 * @see #getInput()
		 */
		String INPUT = "input";

		/**
		 * Configuration property name for {@link #getBusy()}.
		 *
		 * @see #getBusy()
		 */
		String BUSY = "busy";

		/**
		 * Configuration property name for {@link #getAgent()}.
		 *
		 * @see #getAgent()
		 */
		String AGENT = "agent";

		/**
		 * Configuration property name for {@link #getModel()}.
		 *
		 * @see #getModel()
		 */
		String MODEL = "model";

		/**
		 * Configuration property name for {@link #getEffort()}.
		 *
		 * @see #getEffort()
		 */
		String EFFORT = "effort";

		/**
		 * Configuration property name for {@link #getMcpServers()}.
		 *
		 * @see #getMcpServers()
		 */
		String MCP_SERVERS = "mcp-servers";

		/**
		 * Configuration property name for {@link #getPresent()}.
		 *
		 * @see #getPresent()
		 */
		String PRESENT = "present";

		/**
		 * Configuration property name for {@link #getDraft()}.
		 *
		 * @see #getDraft()
		 */
		String DRAFT = "draft";

		/**
		 * Configuration property name for {@link #getReference()}.
		 *
		 * @see #getReference()
		 */
		String REFERENCE = "reference";

		/**
		 * Configuration property name for {@link #getTrace()}.
		 *
		 * @see #getTrace()
		 */
		String TRACE = "trace";

		/**
		 * Configuration property name for {@link #getCitations()}.
		 *
		 * @see #getCitations()
		 */
		String CITATIONS = "citations";

		/**
		 * Configuration property name for {@link #getCitationRenderer()}.
		 *
		 * @see #getCitationRenderer()
		 */
		String CITATION_RENDERER = "citation-renderer";

		/**
		 * Configuration property name for {@link #getActions()}.
		 *
		 * @see #getActions()
		 */
		String ACTIONS = "actions";

		/**
		 * Configuration property name for {@link #getPromptImproverAgent()}.
		 *
		 * @see #getPromptImproverAgent()
		 */
		String PROMPT_IMPROVER_AGENT = "prompt-improver-agent";

		/**
		 * Configuration property name for {@link #getPlaceholder()}.
		 *
		 * @see #getPlaceholder()
		 */
		String PLACEHOLDER = "placeholder";

		/**
		 * Configuration property name for {@link #getControls()}.
		 *
		 * @see #getControls()
		 */
		String CONTROLS = "controls";

		/**
		 * Configuration property name for {@link #getReferenceSource()}.
		 *
		 * @see #getReferenceSource()
		 */
		String REFERENCE_SOURCE = "reference-source";

		/**
		 * Configuration property name for {@link #getMention()}.
		 *
		 * @see #getMention()
		 */
		String MENTION = "mention";

		/**
		 * Configuration property name for {@link #getMenuItems()}.
		 *
		 * @see #getMenuItems()
		 */
		String MENU_ITEMS = "menu-items";

		/**
		 * Configuration property name for {@link #getReactElement()}.
		 *
		 * @see #getReactElement()
		 */
		String REACT_ELEMENT = "react-element";

		/**
		 * The element implementation.
		 *
		 * Hidden in the layout editor: the tag already fixes it, and changing it gives an element
		 * that no longer matches the options shown beside it.
		 */
		@Override
		@Hidden
		@ClassDefault(ChatConversationElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Optional override for the auto-derived personalization key.
		 *
		 * Hidden in the layout editor: the key derived from the view context is the right one, and a
		 * hand-written value silently moves what a user has personalized somewhere else.
		 */
		@Override
		@Hidden
		String getPersonalizationKey();

		/**
		 * Which conversation this element shows, when it must not be the one the enclosing
		 * {@code <chat>} names.
		 *
		 * Leave it empty: an element inside a {@code <chat>} shows that chat, and an element outside
		 * every {@code <chat>} shows {@code default}, so the pieces of one page find each other
		 * without being told. Set it only to break out of the enclosing chat, e.g. for a second
		 * conversation beside the first.
		 */
		@Name(CHAT_ID)
		@Label("Chat id")
		@Nullable
		String getChatId();

		/**
		 * Optional channel holding the open {@code AgentSession}. The conversation follows a write.
		 *
		 * Leave empty when the conversation should only talk to the other chat elements through
		 * {@code chat-id}.
		 */
		@Name(INPUT)
		@Label("Input")
		@Format(ChannelRefFormat.class)
		@Nullable
		ChannelRef getInput();

		/**
		 * Optional output channel written {@code true} while the agent is working.
		 */
		@Name(BUSY)
		@Label("Busy")
		@Format(ChannelRefFormat.class)
		@Nullable
		ChannelRef getBusy();

		/**
		 * Optional two-way channel holding the name of the agent the composer is set to.
		 *
		 * The composer writes what the user picked, and follows a write from outside. An empty
		 * channel says nothing and leaves the picker alone. A name this account may not run is
		 * ignored, and the composer's own agent is written back.
		 */
		@Name(AGENT)
		@Label("Agent")
		@Format(ChannelRefFormat.class)
		@Nullable
		ChannelRef getAgent();

		/**
		 * Optional two-way channel holding the name of the model the next turn runs on.
		 *
		 * Written and followed like {@link #getAgent()}. Picking an agent sets this to the model that
		 * agent is assigned, so a host sees the model a turn would really use.
		 */
		@Name(MODEL)
		@Label("Model")
		@Format(ChannelRefFormat.class)
		@Nullable
		ChannelRef getModel();

		/**
		 * Optional two-way channel holding how hard the model may be asked to think.
		 *
		 * One of the levels the selected model offers, empty for none. Written and followed like
		 * {@link #getAgent()}; a level the model does not know is dropped by the composer, as the
		 * thinking picker's own choice is.
		 */
		@Name(EFFORT)
		@Label("Effort")
		@Format(ChannelRefFormat.class)
		@Nullable
		ChannelRef getEffort();

		/**
		 * Optional two-way channel holding the names of the MCP servers attached to the next turn.
		 *
		 * A collection of names, or one name. Written and followed like {@link #getAgent()}; an empty
		 * channel says nothing, while an empty collection attaches no server at all.
		 */
		@Name(MCP_SERVERS)
		@Label("MCP servers")
		@Format(ChannelRefFormat.class)
		@Nullable
		ChannelRef getMcpServers();

		/**
		 * The channels this page lets the agent write, comma-separated - the allow list of the
		 * {@code present} tool.
		 *
		 * <p>
		 * <b>Deny by default.</b> Empty means the agent may write no channel of this page at all,
		 * and a {@code present} call naming any other channel is refused with a message saying
		 * which names exist. Naming a channel here is a decision about how much of the page the
		 * agent may drive, so it is stated on the element rather than derived from what happens to
		 * be wired somewhere on the view.
		 * </p>
		 *
		 * <p>
		 * The agent writes the objects, not the rendering: what the value looks like is whatever
		 * element reads that channel - a {@code <chat-rail>}'s pane, a {@code <form>}, a
		 * {@code <data-list>}. Usually one channel, the one the pane reads:
		 * {@code present="paneObject"}.
		 * </p>
		 *
		 * <p>
		 * A run without a browser window - headless, TL-Script, the scheduler - has no channels at
		 * all whatever this says; the tool reports that and does nothing.
		 * </p>
		 */
		@Name(PRESENT)
		@Label("Present")
		@Format(CommaSeparatedChannelRefs.class)
		@Nullable
		List<ChannelRef> getPresent();

		/**
		 * Optional two-way channel holding the text standing in the composer, unsent.
		 *
		 * <p>
		 * The composer reports what the user has typed - when they pause, when the field loses the
		 * focus, and just before a send - and follows a write from outside: the written text replaces
		 * the draft, the composer takes the focus and the caret goes to the end. The user still
		 * presses send, so a host that writes a question can be read before it goes.
		 * </p>
		 *
		 * <p>
		 * An empty write clears the draft; {@code null} says nothing and leaves the composer alone,
		 * which is what an unwritten channel holds.
		 * </p>
		 */
		@Name(DRAFT)
		@Label("Draft")
		@Format(ChannelRefFormat.class)
		@Nullable
		ChannelRef getDraft();

		/**
		 * Optional input channel: a model object written to it becomes one atomic reference token in
		 * the draft, exactly as picking it from the {@code @} menu would.
		 *
		 * <p>
		 * The token carries the object's label and its stable id, and the quoted text a nested
		 * {@code <mention>}'s {@code text} expression computes - without a {@code <mention>} the label
		 * is the quoted text as well, because the element has nothing else to say about the object.
		 * The token lands at the caret, so a host can write a sentence to {@link #getDraft() draft}
		 * and a reference into it.
		 * </p>
		 *
		 * <p>
		 * This channel is read, never written: the composer does not report its tokens back, because
		 * the draft they sit in is what {@link #getDraft() draft} already carries.
		 * </p>
		 */
		@Name(REFERENCE)
		@Label("Reference")
		@Format(ChannelRefFormat.class)
		@Nullable
		ChannelRef getReference();

		/**
		 * How much of the agent's execution the transcript shows: nothing, a collapsed summary, or
		 * the full inline trace. The user can still change it live with the composer's trace picker.
		 */
		@Name(TRACE)
		@Label("Trace")
		@FormattedDefault("summary")
		Trace getTrace();

		/**
		 * Whether the answer shows its cited sources at all: the inline markers and the per-answer
		 * "sources" pill. Off removes the whole citation surface from the transcript.
		 */
		@Name(CITATIONS)
		@Label("Citations")
		@BooleanDefault(true)
		boolean getCitations();

		/**
		 * Registered citation-renderer name (names a component set up in code). Empty keeps the
		 * built-in citation popover.
		 *
		 * Hidden in the layout editor: the value must match a registration in {@code TypeScript}.
		 */
		@Name(CITATION_RENDERER)
		@Label("Citation renderer")
		@Hidden
		@Nullable
		String getCitationRenderer();

		/**
		 * Which commands sit under a finished answer.
		 */
		@Name(ACTIONS)
		@Label("Actions")
		@ItemDefault
		Actions getActions();

		/**
		 * Name of the agent that improves draft prompts. Leave empty to disable prompt improvement.
		 */
		@Name(PROMPT_IMPROVER_AGENT)
		@Label("Prompt improver agent")
		@Options(fun = AllAgentNameOptions.class)
		@Nullable
		String getPromptImproverAgent();

		/**
		 * Optional placeholder for the draft field. Empty keeps the built-in wording.
		 */
		@Name(PLACEHOLDER)
		@Label("Placeholder")
		@Nullable
		ResKey getPlaceholder();

		/**
		 * Which pickers and footer controls the composer shows.
		 */
		@Name(CONTROLS)
		@Label("Controls")
		@ItemDefault
		Controls getControls();

		/**
		 * Registered reference-source name (names a component set up in code). Empty, and with no
		 * {@link #getMention() mention} binding, hides the {@code @} picker. A {@code mention} binding
		 * takes precedence over this name.
		 *
		 * Hidden in the layout editor: the value must match a registration in {@code TypeScript}; a
		 * designer binds {@code @} with the {@link #getMention() mention} group instead.
		 */
		@Name(REFERENCE_SOURCE)
		@Label("Reference source")
		@Hidden
		@Nullable
		String getReferenceSource();

		/**
		 * Declarative {@code @}-mention binding: lets {@code @} offer any model object, configured with
		 * TL-Script like {@code <data-list>}. When set, it supplies the {@code @} candidates and takes
		 * precedence over {@link #getReferenceSource()}.
		 */
		@Name(MENTION)
		@Label("Mention")
		@Nullable
		Mention getMention();

		/**
		 * Registered composer-menu-item names.
		 *
		 * Hidden in the layout editor: the values must match registrations in {@code TypeScript}.
		 */
		@Name(MENU_ITEMS)
		@Label("Menu items")
		@Hidden
		@Format(CommaSeparatedStrings.class)
		List<String> getMenuItems();

		/**
		 * Registered React element that renders this conversation.
		 *
		 * Defaults to {@code TLChatConversation}. Hidden in the layout editor: the value must match a
		 * registration in {@code TypeScript}.
		 */
		@Name(REACT_ELEMENT)
		@Label("React element")
		@Hidden
		@StringDefault("TLChatConversation")
		String getReactElement();
	}

	/**
	 * The copy / rate / regenerate row, plus named host actions.
	 */
	@TagName("actions")
	public interface Actions extends ConfigurationItem {

		/**
		 * Configuration property name for {@link #getCopy()}.
		 *
		 * @see #getCopy()
		 */
		String COPY = "copy";

		/**
		 * Configuration property name for {@link #getRate()}.
		 *
		 * @see #getRate()
		 */
		String RATE = "rate";

		/**
		 * Configuration property name for {@link #getRegenerate()}.
		 *
		 * @see #getRegenerate()
		 */
		String REGENERATE = "regenerate";

		/**
		 * Configuration property name for {@link #getTimestamp()}.
		 *
		 * @see #getTimestamp()
		 */
		String TIMESTAMP = "timestamp";

		/**
		 * Configuration property name for {@link #getHost()}.
		 *
		 * @see #getHost()
		 */
		String HOST = "host";

		/**
		 * Whether a finished answer can be copied.
		 */
		@Name(COPY)
		@Label("Copy")
		@BooleanDefault(true)
		boolean getCopy();

		/**
		 * Whether a finished answer can be rated.
		 */
		@Name(RATE)
		@Label("Rate")
		@BooleanDefault(true)
		boolean getRate();

		/**
		 * Whether the last answer can be produced again.
		 */
		@Name(REGENERATE)
		@Label("Regenerate")
		@BooleanDefault(true)
		boolean getRegenerate();

		/**
		 * Whether the answer shows its timestamp.
		 */
		@Name(TIMESTAMP)
		@Label("Timestamp")
		@BooleanDefault(true)
		boolean getTimestamp();

		/**
		 * Registered message-action names (names components set up in code). Unknown names are ignored
		 * with a debug line; they do not fail the view.
		 *
		 * Hidden in the layout editor: the values must match registrations in {@code TypeScript}.
		 */
		@Name(HOST)
		@Label("Host")
		@Hidden
		@Format(CommaSeparatedStrings.class)
		List<String> getHost();
	}

	/**
	 * The pickers and footer switches on the composer.
	 */
	@TagName("controls")
	public interface Controls extends ConfigurationItem {

		/**
		 * Configuration property name for {@link #getAutoApprove()}.
		 *
		 * @see #getAutoApprove()
		 */
		String AUTO_APPROVE = "auto-approve";

		/**
		 * Configuration property name for {@link #getEnterHint()}.
		 *
		 * @see #getEnterHint()
		 */
		String ENTER_HINT = "enter-hint";

		/**
		 * Configuration property name for {@link #getAgentPicker()}.
		 *
		 * @see #getAgentPicker()
		 */
		String AGENT_PICKER = "agent-picker";

		/**
		 * Configuration property name for {@link #getModelPicker()}.
		 *
		 * @see #getModelPicker()
		 */
		String MODEL_PICKER = "model-picker";

		/**
		 * Configuration property name for {@link #getThinking()}.
		 *
		 * @see #getThinking()
		 */
		String THINKING = "thinking";

		/**
		 * Configuration property name for {@link #getMcp()}.
		 *
		 * @see #getMcp()
		 */
		String MCP = "mcp";

		/**
		 * Configuration property name for {@link #getAttachments()}.
		 *
		 * @see #getAttachments()
		 */
		String ATTACHMENTS = "attachments";

		/**
		 * Configuration property name for {@link #getTracePicker()}.
		 *
		 * @see #getTracePicker()
		 */
		String TRACE_PICKER = "trace-picker";

		/**
		 * Whether the auto-approve switch is shown.
		 */
		@Name(AUTO_APPROVE)
		@Label("Auto approve")
		@BooleanDefault(true)
		boolean getAutoApprove();

		/**
		 * Whether the footer shows the Enter hint.
		 */
		@Name(ENTER_HINT)
		@Label("Enter hint")
		@BooleanDefault(true)
		boolean getEnterHint();

		/**
		 * Whether the agent picker is offered.
		 */
		@Name(AGENT_PICKER)
		@Label("Agent picker")
		@BooleanDefault(true)
		boolean getAgentPicker();

		/**
		 * Whether the model picker is offered.
		 */
		@Name(MODEL_PICKER)
		@Label("Model picker")
		@BooleanDefault(true)
		boolean getModelPicker();

		/**
		 * Whether effort levels are offered.
		 */
		@Name(THINKING)
		@Label("Thinking")
		@BooleanDefault(true)
		boolean getThinking();

		/**
		 * Whether MCP servers can be toggled.
		 */
		@Name(MCP)
		@Label("MCP")
		@BooleanDefault(true)
		boolean getMcp();

		/**
		 * Whether files can be attached.
		 */
		@Name(ATTACHMENTS)
		@Label("Attachments")
		@BooleanDefault(true)
		boolean getAttachments();

		/**
		 * Whether the trace-mode picker is offered.
		 */
		@Name(TRACE_PICKER)
		@Label("Trace picker")
		@BooleanDefault(true)
		boolean getTracePicker();
	}

	/**
	 * Declarative binding that lets {@code @} mention any model object.
	 *
	 * <p>
	 * Bound with TL-Script the same way {@code <data-list>} is: {@link #getRows() rows} computes the
	 * objects {@code @} may offer; {@link #getLabel() label} and {@link #getText() text} compute what is
	 * shown and what quoted text the mention puts into the message. Each mention carries the object's
	 * stable id, so a host tool given that id can resolve it (the framework ships none). Candidates are
	 * pushed to the browser and filtered there as the user types, so keep the set modest via
	 * {@link #getRows() rows} and {@link #getLimit() limit}.
	 * </p>
	 */
	@TagName("mention")
	public interface Mention extends ConfigurationItem {

		/**
		 * Configuration property name for {@link #getRows()}.
		 *
		 * @see #getRows()
		 */
		String ROWS = "rows";

		/**
		 * Configuration property name for {@link #getLabel()}.
		 *
		 * @see #getLabel()
		 */
		String LABEL = "label";

		/**
		 * Configuration property name for {@link #getText()}.
		 *
		 * @see #getText()
		 */
		String TEXT = "text";

		/**
		 * Configuration property name for {@link #getObservedTypes()}.
		 *
		 * @see #getObservedTypes()
		 */
		String OBSERVED_TYPES = "observed-types";

		/**
		 * Configuration property name for {@link #getLimit()}.
		 *
		 * @see #getLimit()
		 */
		String LIMIT = "limit";

		/**
		 * TL-Script function computing the objects {@code @} may mention (a {@link java.util.Collection}),
		 * the same way {@code <data-list>} and {@code <table>} bind {@code rows}.
		 */
		@Name(ROWS)
		@Mandatory
		@NonNullable
		Expr getRows();

		/**
		 * Optional TL-Script of one object computing its shown label and the mention's label. Empty
		 * uses the object's own label.
		 */
		@Name(LABEL)
		@Label("Label")
		@Nullable
		Expr getLabel();

		/**
		 * Optional TL-Script of one object computing the quoted text the mention puts into the message -
		 * what the agent reads. Empty uses the label.
		 */
		@Name(TEXT)
		@Label("Text")
		@Nullable
		Expr getText();

		/**
		 * Types whose object changes (create / update / delete) re-run {@link #getRows()}, so the
		 * candidate set refreshes automatically.
		 */
		@Name(OBSERVED_TYPES)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getObservedTypes();

		/**
		 * Most candidates pushed to the browser. The list is filtered in the browser as the user types,
		 * so this bounds the payload rather than the search.
		 */
		@Name(LIMIT)
		@Label("Limit")
		@IntDefault(50)
		int getLimit();
	}

	private final Config _config;

	/** Creates a {@link ChatConversationElement}. */
	@CalledByReflection
	public ChatConversationElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return new ChatConversationControl(context, _config);
	}

	/**
	 * Drops names that have no registered renderer, with a debug line each, instead of failing the
	 * view.
	 *
	 * <p>
	 * The registered set is the names {@code registerMessageAction} has published. Names still
	 * travel to the browser in the control state so the registry can pick them up; this method is
	 * what the tests pin.
	 * </p>
	 */
	public static int ignoreUnknownHostActions(List<String> names, List<String> registered) {
		if (names == null || names.isEmpty()) {
			return 0;
		}
		int ignored = 0;
		List<String> known = registered == null ? List.of() : registered;
		for (String name : names) {
			if (name == null || name.isBlank()) {
				continue;
			}
			String trimmed = name.trim();
			if (known.contains(trimmed)) {
				continue;
			}
			Logger.debug("Ignoring unknown chat message action '" + trimmed + "'.", ChatConversationElement.class);
			ignored++;
		}
		return ignored;
	}
}
