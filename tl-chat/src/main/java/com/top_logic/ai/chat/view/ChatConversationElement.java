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
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.ListBinding;
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
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.CommaSeparatedChannelRefs;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService.AllAgentNameOptions;

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
 * write any channel, and a run on a page that names none is not offered the tool at all. See
 * {@link Config#getPresent()} and {@link com.top_logic.ai.chat.tools.ChatPresentTool}.
 * </p>
 *
 * <p>
 * Three more carry the composer's unsent text: {@code draft} is the text itself, two-way;
 * {@code reference} turns a written model object into one atomic reference token inside it; and
 * {@code quote} does the same for a passage <em>of</em> an object, which only the viewer that showed
 * it can name. Together they are what lets a button anywhere on the page put a question into the
 * chat without a line of code - an {@code <execute-script>} that builds the sentence or the passage
 * and a {@code <write-channel>} that writes it.
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
		 * Configuration property name for {@link #getSubject()}.
		 *
		 * @see #getSubject()
		 */
		String SUBJECT = "subject";

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
		 * Configuration property name for {@link #getQuote()}.
		 *
		 * @see #getQuote()
		 */
		String QUOTE = "quote";

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
		 * Configuration property name for {@link #getWorkingIndicator()}.
		 *
		 * @see #getWorkingIndicator()
		 */
		String WORKING_INDICATOR = "working-indicator";

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
		 * Configuration property name for {@link #getContext()}.
		 *
		 * @see #getContext()
		 */
		String CONTEXT = "context";

		/**
		 * Configuration property name for {@link #getSelection()}.
		 *
		 * @see #getSelection()
		 */
		String SELECTION = "selection";

		/**
		 * Configuration property name for {@link #getPrompts()}.
		 *
		 * @see #getPrompts()
		 */
		String PROMPTS = "prompts";

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
		 * Optional channel naming the object a conversation started here is about.
		 *
		 * <p>
		 * What stands on this channel when the user's first message creates the conversation is
		 * written onto that session as {@code tl.ai.execution:AgentSession#subject}, and stays
		 * there. A page that is about one object at a time - a project, an invoice, a contract -
		 * thereby gets one conversation per object without remembering anything itself:
		 * </p>
		 *
		 * <pre>
		 * &lt;chat-conversation input="chatSession" subject="project"/&gt;
		 * </pre>
		 *
		 * <p>
		 * Only on the way out. Which conversation is shown stays the {@code input} channel's
		 * business, and which conversations a user may pick from stays the {@code <data-list>}'s -
		 * scoped by asking the object for the sessions that refer to it:
		 * </p>
		 *
		 * <pre>
		 * &lt;rows&gt;project -&gt; ifElse(isEmpty($project), list(),
		 *     $project.referers(`tl.ai.execution:AgentSession#subject`)
		 *         .filter(s -&gt; $s.get(`tl.ai.execution:AgentSession#owner`) == currentUser()))&lt;/rows&gt;
		 * </pre>
		 *
		 * <p>
		 * A conversation is stamped once, when it comes into being: opening an older one while
		 * another object stands here does not move it, and a conversation started while the channel
		 * was empty keeps no subject rather than being given one later.
		 * </p>
		 */
		@Name(SUBJECT)
		@Label("Subject")
		@Format(ChannelRefFormat.class)
		@Nullable
		ChannelRef getSubject();

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
		 * The channels count while this element is displayed. A run whose page displays no element
		 * naming one is not offered the {@code present} tool at all, so the agent answers a request
		 * to see objects in text instead of with a call that is refused.
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
		 * Optional input channel: a marked passage of an object becomes one atomic reference token
		 * in the draft, written as a map of what the token carries.
		 *
		 * <p>
		 * What {@link #getReference() reference} cannot say. That channel takes an object, whose
		 * quoted text is everything this element knows about it; a reader who marked one paragraph
		 * in the host's own viewer wants that paragraph quoted, and only the viewer that showed it
		 * knows which one. So this channel takes the token itself - the object the passage is from,
		 * the passage, and optionally a label, a location and a kind:
		 * </p>
		 *
		 * <pre>
		 * &lt;chat-conversation quote="chatQuote"/&gt;
		 * </pre>
		 *
		 * <p>
		 * A write is a map, which is what a TL-Script expression produces:
		 * </p>
		 *
		 * <pre>
		 * section -&gt; {"id": $section, "location": "02 Kontext", "text": $marked}
		 * </pre>
		 *
		 * <p>
		 * {@code id} is normally the model object itself rather than a printed identifier: the
		 * element then fills the kind, the stable id and the label from it - a nested
		 * {@code <mention>} having its say in the label - and the write only has to name the
		 * passage. A map with no {@code text} says nothing, because an object without a passage is
		 * what {@link #getReference() reference} is for. Like it, this channel is read and never
		 * written.
		 * </p>
		 */
		@Name(QUOTE)
		@Label("Quote")
		@Format(ChannelRefFormat.class)
		@Nullable
		ChannelRef getQuote();

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
		 * The invitation standing in the empty prompt field, in this application's own words.
		 *
		 * <p>
		 * Written here as a text per language, or as the key of one. Empty keeps the chat's own
		 * wording, which is what every application got before this option existed.
		 * </p>
		 *
		 * <p>
		 * It is the <em>idle</em> invitation only. While the agent is working the field keeps saying
		 * "type your next message", because that sentence is about the run rather than about this
		 * application, and an application that replaced it would be telling the user to start
		 * something that is already under way.
		 * </p>
		 */
		@Name(PLACEHOLDER)
		@Label("Placeholder")
		@Nullable
		ResKey getPlaceholder();

		/**
		 * What stands where the answer will appear while the agent works and has not written
		 * anything yet, in this application's own look - e.g. its logo, animated.
		 *
		 * <p>
		 * Empty keeps the chat's three pulsing dots. Otherwise one of three forms, sized to the
		 * height of the first text line (the {@code --chat-working-indicator-size} token):
		 * </p>
		 *
		 * <ul>
		 * <li>An SVG file, e.g. {@code /images/app-working.svg}. It is placed into the page itself
		 * rather than shown as a picture, so it can draw in {@code currentColor} or in the theme's
		 * colours ({@code var(--text-secondary)}) and then fits the light and the dark theme
		 * alike. The style sheet inside it therefore applies to the whole page: give its classes a
		 * prefix of the application's.</li>
		 * <li>An icon class, e.g. {@code css:app-working}, which the application's stylesheet
		 * draws and animates - the form for an image that differs per theme, because that
		 * stylesheet can choose it by {@code [data-theme]}.</li>
		 * <li>Any other image, e.g. an animated {@code .webp}, shown as it is.</li>
		 * </ul>
		 *
		 * <p>
		 * Whoever asked the system for less motion sees the SVG and the icon class standing still:
		 * the chat stops their CSS animations and pauses an SVG's own ones. An animated raster
		 * image cannot be stopped from outside, which is the reason to prefer the other two.
		 * </p>
		 *
		 * <p>
		 * Decorative, like the dots: what the screen reader announces while the agent works is the
		 * chat's own status text, whatever is configured here.
		 * </p>
		 */
		@Name(WORKING_INDICATOR)
		@Label("Working indicator")
		@Nullable
		ThemeImage getWorkingIndicator();

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
		 * What this page tells the agent about the situation the user is asking in.
		 *
		 * <p>
		 * The facts are computed from this page's channels and travel with every message the user
		 * sends, where the harness renders the stable ones into the environment block of the system
		 * prompt. The user's own message text is never touched - what the transcript shows is what the
		 * user wrote.
		 * </p>
		 */
		@Name(CONTEXT)
		@Label("Context")
		@Nullable
		Context getContext();

		/**
		 * The object the user has selected, stated to the agent in one line.
		 *
		 * <p>
		 * The question this answers is the one every user asks first - "what about the one I have
		 * open?" - and the shorthand for the {@code <fact>} nobody writes, because writing it means
		 * knowing how this product spells an object identifier. Name the page's selection channel
		 * and the conversation states the selected object, with its type and that identifier, as a
		 * per-turn fact of the {@code <turn-context>} block:
		 * </p>
		 *
		 * <pre>
		 * &lt;chat-conversation input="chatSession"&gt;
		 *   &lt;selection channel="selectedInvoice"/&gt;
		 * &lt;/chat-conversation&gt;
		 * </pre>
		 *
		 * <p>
		 * Never stable: what the user has selected is the thing that changes most often in a
		 * conversation, so it travels in the turn and never in the system prompt.
		 * </p>
		 *
		 * <p>
		 * A {@code <chat-dock/>} states it the same way, on the {@code <chat-conversation/>} nested
		 * inside it - and should, because the dock is where a user asks about the page behind it.
		 * The dock's own channel has to be handed the page's selection for that; on a shell whose
		 * pages are {@code <view-ref>}s, that is a {@code <bind>} per page.
		 * </p>
		 */
		@Name(SELECTION)
		@Label("Selection")
		@Nullable
		Selection getSelection();

		/**
		 * The first questions this page offers, as cards the user clicks instead of typing.
		 *
		 * <p>
		 * A click fills the composer; it does not send. The user reads the sentence and presses
		 * send - the same rule {@link #getDraft() draft}, {@link #getReference() reference} and
		 * {@link #getQuote() quote} follow, and the reason a card may put a long, precise question
		 * there that nobody would type.
		 * </p>
		 *
		 * @see Prompts
		 */
		@Name(PROMPTS)
		@Label("Prompts")
		@Nullable
		Prompts getPrompts();

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
	 * What a page tells the agent about the situation the user is asking in.
	 *
	 * <p>
	 * Bound with TL-Script the way {@code <data-list>} binds its rows: the {@link #getInputs() inputs}
	 * are channels of this page whose current values become the leading arguments of every fact's
	 * {@link Fact#getValue() value} expression. The expressions are evaluated on the server, in the
	 * acting user's context, and re-evaluated whenever an input changes - so what travels with a
	 * message is what the page was showing when it was sent.
	 * </p>
	 *
	 * <p>
	 * Facts that the application states once and that hold for the whole conversation are
	 * {@link Fact#getStable() stable} and are rendered into the system prompt. A fact that changes
	 * while the conversation runs must not be: the environment block sits in front of every request
	 * of that conversation and has to stay byte-identical, or the provider's prompt cache misses on
	 * every turn. Such a fact is rendered into the per-turn {@code <turn-context>} block instead,
	 * which travels in the turn itself, beside the user's text.
	 * </p>
	 */
	@TagName("context")
	public interface Context extends ConfigurationItem {

		/**
		 * Configuration property name for {@link #getInputs()}.
		 *
		 * @see #getInputs()
		 */
		String INPUTS = "inputs";

		/**
		 * Configuration property name for {@link #getFacts()}.
		 *
		 * @see #getFacts()
		 */
		String FACTS = "facts";

		/**
		 * References to channels whose values become the leading positional arguments of every fact
		 * expression, in the order they are listed.
		 */
		@Name(INPUTS)
		@ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
		List<ChannelRef> getInputs();

		/**
		 * The facts themselves, in the order they reach the model.
		 *
		 * <p>
		 * The default container, so a fact is written as a direct child of the context rather than
		 * inside a wrapper element of its own.
		 * </p>
		 */
		@Name(FACTS)
		@DefaultContainer
		@EntryTag("fact")
		List<Fact> getFacts();
	}

	/**
	 * One fact a page states about the situation.
	 *
	 * @see Context
	 */
	@TagName("fact")
	public interface Fact extends ConfigurationItem {

		/**
		 * Configuration property name for {@link #getName()}.
		 *
		 * @see #getName()
		 */
		String NAME = "name";

		/**
		 * Configuration property name for {@link #getValue()}.
		 *
		 * @see #getValue()
		 */
		String VALUE = "value";

		/**
		 * Configuration property name for {@link #getStable()}.
		 *
		 * @see #getStable()
		 */
		String STABLE = "stable";

		/**
		 * What the fact is called in front of the model, e.g. {@code Project}.
		 */
		@Name(NAME)
		@Label("Name")
		@Mandatory
		String getName();

		/**
		 * TL-Script computing the value, taking the enclosing {@link Context#getInputs() inputs} as its
		 * leading arguments.
		 */
		@Name(VALUE)
		@Label("Value")
		@Mandatory
		@NonNullable
		Expr getValue();

		/**
		 * Whether the value holds for the whole conversation.
		 *
		 * <p>
		 * Only a stable fact reaches the system prompt. Leave it on for something the page decides
		 * once - the project a chat belongs to - and switch it off for something that follows the
		 * user's clicks.
		 * </p>
		 */
		@Name(STABLE)
		@Label("Stable")
		@BooleanDefault(true)
		boolean getStable();
	}

	/**
	 * The first questions this page offers, as cards standing under the greeting of a new chat.
	 *
	 * <p>
	 * A new chat is the one screen where a user has nothing to react to, and a page knows better
	 * than the chat what is worth asking on it. So a host names the questions here and the user
	 * clicks one instead of typing it.
	 * </p>
	 *
	 * <p>
	 * The cards stand between the greeting and the prompt field, and only there: a conversation the
	 * user opened from the list is not short of a first question, whether or not it holds a turn.
	 * That is the rule, not a default - there is nothing to switch off.
	 * </p>
	 *
	 * <p>
	 * Bound with TL-Script the way {@link Context} binds its facts: the {@link #getInputs() inputs}
	 * are channels of this page whose current values become the leading arguments of every card's
	 * {@link Prompt#getValue() value} expression, evaluated on the server in the acting user's
	 * context and re-evaluated whenever an input changes. A card can therefore name what the page is
	 * showing - "summarise <em>this</em> invoice" - rather than only a fixed sentence.
	 * </p>
	 *
	 * <p>
	 * Clicking one writes the evaluated text into the composer, exactly as a host writing the
	 * {@link Config#getDraft() draft} channel does, and the composer reports it back onto that
	 * channel. Nothing is sent.
	 * </p>
	 */
	@TagName("prompts")
	public interface Prompts extends ConfigurationItem {

		/**
		 * Configuration property name for {@link #getInputs()}.
		 *
		 * @see #getInputs()
		 */
		String INPUTS = "inputs";

		/**
		 * Configuration property name for {@link #getCards()}.
		 *
		 * @see #getCards()
		 */
		String CARDS = "cards";

		/**
		 * References to channels whose values become the leading positional arguments of every card
		 * expression, in the order they are listed.
		 */
		@Name(INPUTS)
		@ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
		List<ChannelRef> getInputs();

		/**
		 * The cards themselves, in the order they are shown.
		 *
		 * <p>
		 * The default container, so a card is written as a direct child of the section rather than
		 * inside a wrapper element of its own.
		 * </p>
		 */
		@Name(CARDS)
		@DefaultContainer
		@EntryTag("prompt")
		List<Prompt> getCards();
	}

	/**
	 * One question this page offers the user.
	 *
	 * @see Prompts
	 */
	@TagName("prompt")
	public interface Prompt extends ConfigurationItem {

		/**
		 * Configuration property name for {@link #getLabel()}.
		 *
		 * @see #getLabel()
		 */
		String LABEL = "label";

		/**
		 * Configuration property name for {@link #getIcon()}.
		 *
		 * @see #getIcon()
		 */
		String ICON = "icon";

		/**
		 * Configuration property name for {@link #getValue()}.
		 *
		 * @see #getValue()
		 */
		String VALUE = "value";

		/**
		 * What the card says, in the user's own language.
		 *
		 * <p>
		 * The short form of the question, not the question itself: the card has one line, while the
		 * {@link #getValue() value} it fills in may be a paragraph.
		 * </p>
		 */
		@Name(LABEL)
		@Label("Label")
		@Mandatory
		ResKey getLabel();

		/**
		 * The icon on the card, e.g. {@code css:bi bi-receipt}.
		 *
		 * <p>
		 * Named the way a {@code <chat-rail/>} section names its icon. Cards without one show their
		 * label alone, which is what a set of cards that say nothing an icon could add should do.
		 * </p>
		 */
		@Name(ICON)
		@Label("Icon")
		@Nullable
		ThemeImage getIcon();

		/**
		 * TL-Script computing the text the click puts into the composer, taking the enclosing
		 * {@link Prompts#getInputs() inputs} as its leading arguments.
		 */
		@Name(VALUE)
		@Label("Value")
		@Mandatory
		@NonNullable
		Expr getValue();
	}

	/**
	 * The page's current selection, stated to the agent as one per-turn fact.
	 *
	 * <p>
	 * The one-line form of what {@link Fact} can express the long way round. The value it produces
	 * names the selected object three times over - the label the user sees, the qualified type name
	 * and the identifier every tool reads back - so the agent can both answer "which one do I have
	 * open?" and act on it. See {@link ChatSelection} for the exact form.
	 * </p>
	 *
	 * <p>
	 * It is a fact of the {@code <turn-context>} block and never of the system prompt: a selection is
	 * the value that changes most often in a conversation, and the environment block has to stay
	 * byte-identical across its turns or every provider's prompt cache misses.
	 * </p>
	 *
	 * @see Config#getSelection()
	 */
	@TagName("selection")
	public interface Selection extends ConfigurationItem {

		/**
		 * Configuration property name for {@link #getChannel()}.
		 *
		 * @see #getChannel()
		 */
		String CHANNEL = "channel";

		/**
		 * Configuration property name for {@link #getName()}.
		 *
		 * @see #getName()
		 */
		String NAME = "name";

		/**
		 * Configuration property name for {@link #getLabel()}.
		 *
		 * @see #getLabel()
		 */
		String LABEL = "label";

		/**
		 * The channel holding what the user has selected - one object, several, or nothing.
		 *
		 * <p>
		 * Normally the {@code selection} channel of the table or list the user picks in. A channel
		 * of <em>this</em> view: a page of a shell hands its selection to a dock in the shell's
		 * footer with a {@code <bind>} on the {@code <view-ref>}, the way it hands over the draft.
		 * </p>
		 */
		@Name(CHANNEL)
		@Label("Channel")
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getChannel();

		/**
		 * What the fact is called in front of the model.
		 *
		 * <p>
		 * Read by the model, so it is English and not a resource, like every other text a tool puts
		 * in front of one. Name what the page holds - {@code "Open invoice"}, {@code "Selected
		 * task"} - where that says more than the default does.
		 * </p>
		 */
		@Name(NAME)
		@Label("Name")
		@StringDefault(ChatSelection.DEFAULT_NAME)
		@NonNullable
		String getName();

		/**
		 * TL-Script computing what to call one selected object, taking that object as its argument.
		 *
		 * <p>
		 * Left empty the application's own label is used - which is what the user sees on the
		 * screen, and therefore the right answer to a question about it. Configure an expression
		 * here when an object's label is not what makes it recognisable in a sentence.
		 * </p>
		 */
		@Name(LABEL)
		@Label("Label")
		@Nullable
		Expr getLabel();
	}

	/**
	 * Declarative binding that lets {@code @} mention any model object.
	 *
	 * <p>
	 * Bound with TL-Script the same way {@code <data-list>} is: {@link #getRows() rows} computes the
	 * objects {@code @} may offer; {@link #getLabel() label} and {@link #getText() text} compute what is
	 * shown and what quoted text the mention puts into the message. Each mention carries the object's
	 * id as the agent's tools read it, so the agent can read the object with {@code getInstance}. Candidates are
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
