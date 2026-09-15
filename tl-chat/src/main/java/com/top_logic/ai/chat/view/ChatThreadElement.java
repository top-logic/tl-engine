/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.List;

import com.top_logic.ai.chat.view.AgentChatElement.TraceMode;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;

/**
 * Declarative {@code <chat-thread/>} element: the transcript as a view element of its own.
 *
 * <p>
 * Several chat elements on one page join the same conversation through {@code chat-id}. The
 * optional {@code input} channel carries the open {@code AgentSession}; the optional {@code busy}
 * channel is written {@code true} while the agent works, so a host can disable its own commands.
 * </p>
 */
public class ChatThreadElement implements UIElement {

	/**
	 * How a finished run is shown above the answer.
	 */
	public enum RunSummary implements ExternallyNamed {

		/** One closed line; the user can expand it. */
		COLLAPSED("collapsed"),

		/** The steps are shown. */
		EXPANDED("expanded"),

		/** No run summary. */
		HIDDEN("hidden");

		private final String _externalName;

		RunSummary(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/** Configuration for {@link ChatThreadElement}. */
	@TagName("chat-thread")
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
		 * Configuration property name for {@link #getTraceMode()}.
		 *
		 * @see #getTraceMode()
		 */
		String TRACE_MODE = "trace-mode";

		/**
		 * Configuration property name for {@link #getRunSummary()}.
		 *
		 * @see #getRunSummary()
		 */
		String RUN_SUMMARY = "run-summary";

		/**
		 * Configuration property name for {@link #getCitationMarkers()}.
		 *
		 * @see #getCitationMarkers()
		 */
		String CITATION_MARKERS = "citation-markers";

		/**
		 * Configuration property name for {@link #getRailToggle()}.
		 *
		 * @see #getRailToggle()
		 */
		String RAIL_TOGGLE = "rail-toggle";

		/**
		 * Configuration property name for {@link #getAnnotations()}.
		 *
		 * @see #getAnnotations()
		 */
		String ANNOTATIONS = "annotations";

		/**
		 * Configuration property name for {@link #getActions()}.
		 *
		 * @see #getActions()
		 */
		String ACTIONS = "actions";

		/**
		 * Configuration property name for {@link #getReactElement()}.
		 *
		 * @see #getReactElement()
		 */
		String REACT_ELEMENT = "react-element";

		@Override
		@ClassDefault(ChatThreadElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Shared key that lets this thread, a session list and a composer show one conversation.
		 *
		 * Defaults to {@code default}, so unconfigured pieces on one page find each other.
		 */
		@Name(CHAT_ID)
		@Label("Chat id")
		@StringDefault("default")
		String getChatId();

		/**
		 * Optional channel holding the open {@code AgentSession}. The thread follows a write.
		 *
		 * Leave empty when the thread should only talk to the other chat elements through
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
		 * Amount of agent execution detail shown in the thread.
		 */
		@Name(TRACE_MODE)
		@Label("Trace mode")
		@FormattedDefault("overview")
		TraceMode getTraceMode();

		/**
		 * How a finished run is shown above the answer.
		 */
		@Name(RUN_SUMMARY)
		@Label("Run summary")
		@FormattedDefault("collapsed")
		RunSummary getRunSummary();

		/**
		 * Whether cited sources appear as markers in the answer.
		 */
		@Name(CITATION_MARKERS)
		@Label("Citation markers")
		@BooleanDefault(true)
		boolean getCitationMarkers();

		/**
		 * Whether the thread bar shows the rail badge button.
		 */
		@Name(RAIL_TOGGLE)
		@Label("Rail toggle")
		@BooleanDefault(true)
		boolean getRailToggle();

		/**
		 * Registered annotation-renderer name. Empty keeps the built-in citation popover.
		 */
		@Name(ANNOTATIONS)
		@Label("Annotations")
		@Nullable
		String getAnnotations();

		/**
		 * Which commands sit under a finished answer.
		 */
		@Name(ACTIONS)
		@Label("Actions")
		@ItemDefault
		Actions getActions();

		/**
		 * Registered React element that renders this thread.
		 *
		 * Defaults to {@code TLChatThread}.
		 */
		@Name(REACT_ELEMENT)
		@Label("React element")
		@StringDefault("TLChatThread")
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
		 * Registered message-action names. Unknown names are ignored with a debug line; they do
		 * not fail the view.
		 */
		@Name(HOST)
		@Label("Host")
		@Format(CommaSeparatedStrings.class)
		List<String> getHost();
	}

	private final Config _config;

	/** Creates a {@link ChatThreadElement}. */
	@CalledByReflection
	public ChatThreadElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return new ChatThreadControl(context, _config);
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
			Logger.debug("Ignoring unknown chat message action '" + trimmed + "'.", ChatThreadElement.class);
			ignored++;
		}
		return ignored;
	}
}
