/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.function.Consumer;

import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.scripting.ReactWindowReplay;
import com.top_logic.layout.react.scripting.WindowAccess;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.tl.ai.core.agent.RunWindow;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.tool.api.AICategory;

/**
 * Connects agent runs to the browser windows the chat is displayed in.
 *
 * <p>
 * An agent tool of a run writes into the browser window the run belongs to, for example to show
 * objects on a channel of the page; such a write waits at most {@link Config#getWriteTimeoutMillis()}
 * for a browser command in progress. A chat that is deleted is dropped out of every open window of
 * its owner.
 * </p>
 *
 * <p>
 * The model-bound elements of a window follow what a run commits without this service: the engine
 * delivers commits of other threads to every affected window by itself.
 * </p>
 *
 * @see RunWindow
 * @see #inWindow(TLSubSessionContext, Runnable)
 * @see #onSessionDeleted(Person, String)
 */
@ServiceDependencies({
	AgentExecutionStore.Module.class,
})
@ServiceCategory.Of(AICategory.class)
public class ChatWindowService extends ConfiguredManagedClass<ChatWindowService.Config<?>>
		implements AgentExecutionStore.ChangeListener {

	/**
	 * Configuration of {@link ChatWindowService}.
	 */
	public interface Config<I extends ChatWindowService> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration property name for the write timeout.
		 *
		 * @see #getWriteTimeoutMillis()
		 */
		String WRITE_TIMEOUT_MILLIS = "write-timeout-millis";

		/**
		 * Default value of {@link #getWriteTimeoutMillis()}.
		 */
		int DEFAULT_WRITE_TIMEOUT_MILLIS = 5000;

		/**
		 * How long a write into a window waits for that window's interaction, in milliseconds.
		 *
		 * <p>
		 * The interaction is held while the window runs a browser command. A write that gives up
		 * tells the model that the page could not be reached, and the model may try again.
		 * </p>
		 *
		 * @see #WRITE_TIMEOUT_MILLIS
		 * @see ChatWindowService#inWindow(TLSubSessionContext, Runnable)
		 */
		@Name(WRITE_TIMEOUT_MILLIS)
		@IntDefault(DEFAULT_WRITE_TIMEOUT_MILLIS)
		int getWriteTimeoutMillis();
	}

	private static volatile ChatWindowService _instance;

	/**
	 * Creates a {@link ChatWindowService} from configuration.
	 */
	public ChatWindowService(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * The running instance, or <code>null</code> when the service is not started.
	 */
	public static ChatWindowService getInstance() {
		return _instance;
	}

	@Override
	protected void startUp() {
		super.startUp();
		_instance = this;
		AgentExecutionStore.getInstance().addChangeListener(this);
		RunWindow.install(ChatWindowService::inWindowNow);
	}

	@Override
	protected void shutDown() {
		RunWindow.install(null);
		if (AgentExecutionStore.Module.INSTANCE.isActive()) {
			AgentExecutionStore.getInstance().removeChangeListener(this);
		}
		_instance = null;
		super.shutDown();
	}

	/**
	 * Whether a live browser window displays {@code subSession}.
	 *
	 * <p>
	 * The question a tool asks before it explains itself: a run with no window is a headless, a
	 * scripted or a scheduled one, and telling its model that a channel name is not on the allow
	 * list would blame the application for something that is a fact about the run.
	 * </p>
	 *
	 * @param subSession
	 *        The run's sub-session. May be <code>null</code>.
	 * @return Whether {@link #inWindow(TLSubSessionContext, Runnable)} has a window to reach.
	 */
	public static boolean hasWindow(TLSubSessionContext subSession) {
		return ReactWindowRegistry.registryDisplaying(subSession) != null;
	}

	/**
	 * Runs {@code action} in the browser window of {@code subSession}, waiting for a browser
	 * command in progress at most {@link Config#getWriteTimeoutMillis()}.
	 *
	 * <p>
	 * What lets an agent tool put a value onto a view channel of the window the run belongs to: the
	 * action runs in the window's sub-session within the interaction of its session, after the
	 * window's pending model events were synthesized, so that the window knows the objects the run
	 * has committed.
	 * </p>
	 *
	 * @param subSession
	 *        The window's sub-session; on the agent thread that is the bound one. May be
	 *        <code>null</code>, which is {@link WindowAccess#NO_WINDOW}.
	 * @param action
	 *        What to do in the window. Runs on the calling thread; an exception it throws is thrown
	 *        here. A caller that already holds the interaction - an agent run synchronously from a
	 *        button, on the request thread - acts within it at once.
	 * @return What became of it; never <code>null</code>.
	 *
	 * @see ReactWindowReplay#inWindow(TLSubSessionContext, long, Consumer)
	 */
	public static WindowAccess inWindow(TLSubSessionContext subSession, Runnable action) {
		ChatWindowService service = getInstance();
		long timeout = service == null ? Config.DEFAULT_WRITE_TIMEOUT_MILLIS : service.getConfig().getWriteTimeoutMillis();
		return ReactWindowReplay.inWindow(subSession, timeout, root -> action.run());
	}

	/**
	 * The {@link RunWindow} of a run: enters the window of the run's sub-session on the calling
	 * thread and waits for a browser command in progress to finish.
	 */
	private static boolean inWindowNow(TLSubSessionContext subSession, Consumer<Object> action) {
		return ReactWindowReplay.inWindow(subSession, action) == WindowAccess.DONE;
	}

	@Override
	public void onAgentDataChanged(Person owner) {
		// The engine delivers the commit to the windows it affects.
	}

	/**
	 * Drops the deleted chat out of the in-memory state of every live window of its owner.
	 *
	 * <p>
	 * Done inline: a window whose active session was just deleted must not accept another message
	 * into it, and the caller is the request that deleted it. The account of a window is the person
	 * of its sub-session, which is a field of the sub-session and needs no context to be read.
	 * </p>
	 */
	@Override
	public void onSessionDeleted(Person owner, String sessionId) {
		for (TLSubSessionContext subSession : ReactWindowRegistry.windowSubSessions()) {
			if (owner != null && !owner.equals(subSession.getPerson())) {
				continue;
			}
			// Every conversation of the window, not only one of them: a chat that was deleted is
			// gone for the page's chat and for the dock's alike.
			for (TaskProgressTracker tracker : ChatSessionRegistry.trackers(subSession)) {
				try {
					if (tracker.getSessionManager().forgetSession(sessionId)) {
						Logger.info("Dropped deleted chat session " + sessionId + " from a window.",
							ChatWindowService.class);
					}
				} catch (RuntimeException ex) {
					Logger.warn("Could not drop deleted chat session " + sessionId + " from a window: "
						+ ex.getMessage(), ex, ChatWindowService.class);
				}
			}
		}
	}

	/**
	 * {@link TypedRuntimeModule} for {@link ChatWindowService}.
	 */
	public static final class Module extends TypedRuntimeModule<ChatWindowService> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<ChatWindowService> getImplementation() {
			return ChatWindowService.class;
		}
	}
}
