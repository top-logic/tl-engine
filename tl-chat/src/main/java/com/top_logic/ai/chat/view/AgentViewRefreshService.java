/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.scripting.ReactWindowReplay;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.tl.ai.core.agent.RunWindow;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;

/**
 * Makes the model-bound view elements of an open chat window follow what a running agent writes.
 *
 * <p>
 * An agent runs on a background thread and commits through the {@link AgentExecutionStore}'s writer
 * thread. A browser window only learns of such a commit when its
 * {@code GlobalModelEventForwarder} is asked to synthesize the pending model events, and today that
 * happens on a browser command or on the SSE heartbeat - so without this service a
 * {@code <data-list>} over the agent's tasks would stand still for up to 30 seconds while the run
 * ticks them off.
 * </p>
 *
 * <p>
 * The service listens for committed changes, coalesces them (many task updates within one turn cost
 * one refresh, see {@link Config#getRefreshDelayMillis()}) and then, for each registered window of
 * the affected account, synthesizes the events in that window's sub-session and <em>within the
 * window's {@link com.top_logic.layout.react.window.Interaction interaction}</em>, as the engine's
 * own request handling does. The interaction is what makes this safe beside a browser command: a
 * React control's state map is a plain {@code HashMap}, so pushing state from two threads at once
 * would corrupt it. (The engine's SSE heartbeat synthesizes without it; this deliberately does not
 * copy that.) It is entered with a time limit, see {@link WindowInteraction}.
 * </p>
 *
 * <p>
 * <b>Coalescing never forgets a change.</b> A pass takes time - it enters every registered window's
 * sub-session and waits for that window's interaction - and the agent writes throughout. A change
 * that arrives while a pass is running is therefore carried into the next pass rather than folded
 * into the running one, which has already taken its batch; {@link PendingRefreshes} is the whole of
 * that rule. Without it such a change was dropped, and the row it was about stood still until an
 * unrelated event happened to synthesize the window's model events - a browser command, or the 30 s
 * heartbeat if the window still has one (#497).
 * </p>
 *
 * <p>
 * <b>TEMPORARY FORK.</b> Enumerating the live windows of an account is not something the engine
 * offers, so this service keeps its own weak register, fed by {@link ChatViewBinding} while a chat
 * renders - which means only windows that have a chat element on them are refreshed. <b>Upstream
 * target:</b> the engine should push model events to every live window by itself when a background
 * thread commits, and offer a way to enumerate a session's windows; then delete this class.
 * </p>
 */
@ServiceDependencies({
	AgentExecutionStore.Module.class,
})
public class AgentViewRefreshService extends ConfiguredManagedClass<AgentViewRefreshService.Config<?>>
		implements AgentExecutionStore.ChangeListener {

	/**
	 * Configuration of {@link AgentViewRefreshService}.
	 */
	public interface Config<I extends AgentViewRefreshService> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration property name for the refresh delay.
		 *
		 * @see #getRefreshDelayMillis()
		 */
		String REFRESH_DELAY_MILLIS = "refresh-delay-millis";

		/**
		 * Configuration property name for the lock timeout.
		 *
		 * @see #getLockTimeoutMillis()
		 */
		String LOCK_TIMEOUT_MILLIS = "lock-timeout-millis";

		/**
		 * Configuration property name for the write timeout.
		 *
		 * @see #getWriteTimeoutMillis()
		 */
		String WRITE_TIMEOUT_MILLIS = "write-timeout-millis";

		/**
		 * How long changes are collected before the windows are refreshed, in milliseconds.
		 *
		 * <p>
		 * A turn that ticks off five tasks in a second must not cost five refreshes. Raise this to
		 * trade immediacy for load; it is the upper bound on how long a window lags behind the run.
		 * </p>
		 *
		 * @see #REFRESH_DELAY_MILLIS
		 */
		@Name(REFRESH_DELAY_MILLIS)
		@IntDefault(200)
		int getRefreshDelayMillis();

		/**
		 * How long a refresh waits for a window's interaction, in milliseconds.
		 *
		 * <p>
		 * The interaction is held while that window runs a browser command, and such a command
		 * synthesizes the model events itself when it finishes - so giving up costs nothing but a
		 * little latency, whereas waiting would tie up a scheduler thread behind a slow command.
		 * </p>
		 *
		 * @see #LOCK_TIMEOUT_MILLIS
		 */
		@Name(LOCK_TIMEOUT_MILLIS)
		@IntDefault(500)
		int getLockTimeoutMillis();

		/**
		 * How long a write into a window waits for that window's interaction, in milliseconds.
		 *
		 * <p>
		 * Longer than {@link #getLockTimeoutMillis()}, and for the opposite reason: a refresh that
		 * gives up loses nothing, because the command holding the interaction synthesizes the same
		 * events when it finishes. A write has no such second chance - nobody else is going to put
		 * the agent's value on the channel - so giving up means telling the model that the page
		 * could not be reached.
		 * </p>
		 *
		 * @see #WRITE_TIMEOUT_MILLIS
		 * @see #inWindow(TLSubSessionContext, Runnable)
		 */
		@Name(WRITE_TIMEOUT_MILLIS)
		@IntDefault(5000)
		int getWriteTimeoutMillis();
	}

	/**
	 * What became of a write a background thread asked to make in a browser window.
	 *
	 * @see #inWindow(TLSubSessionContext, Runnable)
	 */
	public enum WindowAccess {

		/** The window was reached and the write ran. */
		DONE,

		/**
		 * This run has no window: a headless run, a scheduled one, or one whose browser window is
		 * gone. Nothing was written, and nothing can be.
		 */
		NO_WINDOW,

		/**
		 * The window is there but stayed busy with a browser command for longer than
		 * {@link Config#getWriteTimeoutMillis()}. Nothing was written; the same write can be tried
		 * again.
		 */
		BUSY;
	}

	/**
	 * The windows to refresh, keyed by their sub-session so an expired session drops out by itself.
	 *
	 * <p>
	 * The {@link ReactWindowRegistry} is held weakly as well: it belongs to the {@code HttpSession},
	 * and this register must not be what keeps a logged-out session alive.
	 * </p>
	 */
	private final Map<TLSubSessionContext, WindowRef> _windows =
		Collections.synchronizedMap(new WeakHashMap<>());

	/**
	 * What still has to be refreshed, and whether a pass is armed for it.
	 *
	 * <p>
	 * Owns the one rule that makes this service dependable: a change that arrives while a pass is
	 * running is carried into the next pass rather than dropped.
	 * </p>
	 */
	private final PendingRefreshes _pending = new PendingRefreshes();

	/** Enters a window's interaction for the refresh and for {@link #inWindow}, bounded in time. */
	private WindowInteraction _interaction;

	/** The scheduled refresh, or <code>null</code> when none is due. */
	private volatile ScheduledFuture<?> _scheduled;

	private static volatile AgentViewRefreshService _instance;

	/**
	 * Runs at the end of a refresh pass, before it is closed.
	 *
	 * <p>
	 * Tests replace this to commit while a pass is running, which is the one moment a change used
	 * to be dropped in and is otherwise not reproducible from the outside.
	 * </p>
	 */
	static Runnable duringPass = () -> {
		// Nothing in production; see the field comment.
	};

	/**
	 * Creates an {@link AgentViewRefreshService} from configuration.
	 */
	public AgentViewRefreshService(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * The running instance, or <code>null</code> when the service is not started.
	 */
	public static AgentViewRefreshService getInstance() {
		return _instance;
	}

	@Override
	protected void startUp() {
		super.startUp();
		_interaction = new WindowInteraction();
		_instance = this;
		AgentExecutionStore.getInstance().addChangeListener(this);
		RunWindow.install(this::inWindowNow);
	}

	@Override
	protected void shutDown() {
		RunWindow.install(null);
		if (AgentExecutionStore.Module.INSTANCE.isActive()) {
			AgentExecutionStore.getInstance().removeChangeListener(this);
		}
		ScheduledFuture<?> scheduled = _scheduled;
		if (scheduled != null) {
			scheduled.cancel(false);
			_scheduled = null;
		}
		_windows.clear();
		_pending.clear();
		_instance = null;
		_interaction.close();
		super.shutDown();
	}

	/**
	 * Remembers a window that shows a chat, so a running agent's writes reach it.
	 *
	 * <p>
	 * Idempotent, and a no-op when the service is not running - a host that leaves it switched off
	 * falls back to the engine's own refresh points (a browser command, the SSE heartbeat).
	 * </p>
	 *
	 * @param subSession
	 *        The window's sub-session; the register key, held weakly.
	 * @param windowName
	 *        The window's registry key, the same value the chat client echoes as its window id.
	 * @param registry
	 *        The window registry of the login the window belongs to.
	 */
	public static void registerWindow(TLSubSessionContext subSession, String windowName,
			ReactWindowRegistry registry) {
		AgentViewRefreshService service = getInstance();
		if (service == null || subSession == null || registry == null
			|| windowName == null || windowName.isEmpty()) {
			return;
		}
		if (registry.getQueue(windowName) == null) {
			// A tree built for no browser window - a view rendered headlessly to check it - renders
			// its chat elements too. It must not replace the window the sub-session really shows:
			// a browser window's queue is in its registry before its tree is built.
			return;
		}
		// The account is read here, on the rendering request's thread, and kept: the refresh runs on
		// a scheduler thread with no interaction context of its own, and asking a sub-session for
		// its account there is a knowledge-base access outside any context. It also cannot change -
		// a login gets its own sub-session, so an entry is about one account for its whole life.
		// It may be null (a window nobody is logged into), which then matches no account.
		service._windows.put(subSession,
			new WindowRef(windowName, subSession.getPerson(), new WeakReference<>(registry)));
	}

	/**
	 * Whether a browser window of {@code subSession} is reachable at all.
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
		AgentViewRefreshService service = getInstance();
		if (service == null || subSession == null) {
			return false;
		}
		WindowRef window = service._windows.get(subSession);
		return window != null && window.registry().get() != null;
	}

	/**
	 * The {@link RunWindow} of a run: enters the window on the calling thread, through the engine's
	 * {@link ReactWindowReplay#inWindow(ReactWindowRegistry, String, Runnable) way into a window}.
	 *
	 * <p>
	 * Unlike {@link #inWindow(TLSubSessionContext, Runnable)}, which hands the action to a thread of
	 * its own, this keeps the run's security context: a tool reading the page sees what the user may
	 * see, not what a scheduler thread may. It waits for a browser command in progress to finish.
	 * </p>
	 */
	private boolean inWindowNow(TLSubSessionContext subSession, Consumer<Object> action) {
		WindowRef window = subSession == null ? null : _windows.get(subSession);
		ReactWindowRegistry registry = window == null ? null : window.registry().get();
		if (registry == null) {
			return false;
		}
		String windowName = window.windowName();
		return ThreadContextManager.inContext(subSession,
			(ComputationEx2<Boolean, RuntimeException, RuntimeException>) () -> ReactWindowReplay
				.inWindow(registry, windowName, () -> {
					// The tree the window displays, where the engine's own replay looks for it.
					SSEUpdateQueue queue = registry.getQueue(windowName);
					action.accept(queue == null ? null : queue.getRootControl());
				}));
	}

	/**
	 * Runs {@code action} in the browser window of {@code subSession}, within that window's
	 * interaction, with the window's pending model changes applied first.
	 *
	 * <p>
	 * This is the write counterpart of the refresh: same window register, same interaction, but
	 * the caller supplies what happens rather than only asking for a synthesize. It is what lets an
	 * agent tool put a value onto a view channel of the window the run belongs to - a React
	 * control's state map is a plain {@code HashMap}, so a write from the agent thread beside a
	 * browser command would corrupt it.
	 * </p>
	 *
	 * <p>
	 * The pending model events are synthesized <em>before</em> {@code action} runs, so the window
	 * sees the objects this run has committed; without it a value written onto a channel could name
	 * an object the window does not know yet.
	 * </p>
	 *
	 * <p>
	 * A run with no window - headless, TL-Script, the scheduler, or a browser window that has since
	 * closed - answers {@link WindowAccess#NO_WINDOW} and runs nothing. That is a fact about the
	 * run, not a failure: there is no page to write to.
	 * </p>
	 *
	 * @param subSession
	 *        The window's sub-session; on the agent thread that is the bound one. May be
	 *        <code>null</code>, which is {@link WindowAccess#NO_WINDOW}.
	 * @param action
	 *        What to do in the window. Runs on a thread that holds the window's interaction, in the
	 *        window's sub-session, while the calling thread waits for it; an exception it throws is
	 *        thrown here.
	 * @return What became of it; never <code>null</code>.
	 */
	public static WindowAccess inWindow(TLSubSessionContext subSession, Runnable action) {
		AgentViewRefreshService service = getInstance();
		if (service == null || subSession == null) {
			return WindowAccess.NO_WINDOW;
		}
		WindowRef window = service._windows.get(subSession);
		if (window == null) {
			return WindowAccess.NO_WINDOW;
		}
		ReactWindowRegistry registry = window.registry().get();
		if (registry == null) {
			return WindowAccess.NO_WINDOW;
		}
		boolean ran = service._interaction.run(registry, subSession, service.getConfig().getWriteTimeoutMillis(),
			() -> {
				registry.synthesizeModelEvents(window.windowName());
				action.run();
			});
		return ran ? WindowAccess.DONE : WindowAccess.BUSY;
	}

	@Override
	public void onAgentDataChanged(Person owner) {
		if (_pending.changed(owner)) {
			arm();
		}
	}

	/**
	 * Drops the deleted chat out of the in-memory state of every window that still holds it.
	 *
	 * <p>
	 * The window register this service keeps for the refresh is also the only list of the windows a
	 * deletion has to reach. Done inline rather than on the scheduler: a window whose active session
	 * was just deleted must not accept another message into it, and the caller is the request that
	 * deleted it.
	 * </p>
	 */
	@Override
	public void onSessionDeleted(Person owner, String sessionId) {
		for (Map.Entry<TLSubSessionContext, WindowRef> entry : snapshot()) {
			TLSubSessionContext subSession = entry.getKey();
			if (owner != null && !owner.equals(entry.getValue().account())) {
				continue;
			}
			// Every conversation of the window, not only one of them: a chat that was deleted is
			// gone for the page's chat and for the dock's alike.
			for (TaskProgressTracker tracker : ChatSessionRegistry.trackers(subSession)) {
				try {
					if (tracker.getSessionManager().forgetSession(sessionId)) {
						Logger.info("Dropped deleted chat session " + sessionId + " from window '"
							+ entry.getValue().windowName() + "'.", AgentViewRefreshService.class);
					}
				} catch (RuntimeException ex) {
					Logger.warn("Could not drop deleted chat session " + sessionId + " from window '"
						+ entry.getValue().windowName() + "': " + ex.getMessage(), ex,
						AgentViewRefreshService.class);
				}
			}
		}
	}

	/**
	 * Arms a refresh pass after the coalescing delay.
	 *
	 * <p>
	 * Only ever called when {@link PendingRefreshes} said one is needed, so this does not decide
	 * whether to coalesce - that decision is the pending state's, which is the one place that knows
	 * whether a pass is due, running, or neither.
	 * </p>
	 */
	private void arm() {
		if (!SchedulerService.Module.INSTANCE.isActive()) {
			_pending.notArmed();
			return;
		}
		try {
			_scheduled = SchedulerService.getInstance().schedule(this::runPass,
				getConfig().getRefreshDelayMillis(), TimeUnit.MILLISECONDS);
		} catch (RuntimeException ex) {
			// A scheduler that refuses the task must not leave the bookkeeping believing a pass is
			// coming, or nothing would ever arm one again.
			_pending.notArmed();
			throw ex;
		}
	}

	/**
	 * One refresh pass: covers what was pending when it started, and arms another one when a change
	 * arrived while it worked.
	 */
	private void runPass() {
		try {
			refreshPending(_pending.start());
			duringPass.run();
		} finally {
			if (_pending.finish()) {
				arm();
			}
		}
	}

	/** Refreshes the windows of every account the batch names. */
	private void refreshPending(PendingRefreshes.Batch batch) {
		if (batch.isEmpty()) {
			return;
		}
		for (Map.Entry<TLSubSessionContext, WindowRef> entry : snapshot()) {
			WindowRef window = entry.getValue();
			if (!batch.all() && !isOwnedBy(window, batch.owners())) {
				continue;
			}
			refresh(entry.getKey(), window);
		}
	}

	/**
	 * Whether the given window shows one of the given accounts.
	 *
	 * <p>
	 * The null check is not defensive: a window nobody is logged into is registered with no
	 * account, and the batch is an immutable set, which answers {@code contains(null)} with a
	 * {@link NullPointerException} rather than with <code>false</code> - which would end the pass
	 * and lose the whole batch, windows further down the register included.
	 * </p>
	 */
	private static boolean isOwnedBy(WindowRef window, Set<Person> owners) {
		return window.account() != null && owners.contains(window.account());
	}

	/** A stable copy of the register, so the refresh does not iterate it while a render adds to it. */
	private List<Map.Entry<TLSubSessionContext, WindowRef>> snapshot() {
		synchronized (_windows) {
			return new ArrayList<>(_windows.entrySet());
		}
	}

	/**
	 * Pushes the pending model changes into one window, in its own sub-session and within its
	 * interaction. Best-effort: a window that cannot be reached is skipped, never logged as a failure
	 * of the run that caused the refresh.
	 */
	private void refresh(TLSubSessionContext subSession, WindowRef window) {
		ReactWindowRegistry registry = window.registry().get();
		if (registry == null) {
			// The login this window belonged to is gone; nothing will ever reach it again.
			_windows.remove(subSession);
			return;
		}
		try {
			// A window that stays busy is skipped: the browser command holding it synthesizes the
			// same events itself when it finishes, so this refresh has nothing left to do.
			_interaction.run(registry, subSession, getConfig().getLockTimeoutMillis(),
				() -> registry.synthesizeModelEvents(window.windowName()));
		} catch (RuntimeException ex) {
			Logger.warn("Could not refresh chat window '" + window.windowName() + "': "
				+ ex.getMessage(), ex, AgentViewRefreshService.class);
		}
	}

	/**
	 * One registered window: its registry key and the registry it lives in.
	 *
	 * @param windowName
	 *        The window's key in its {@link ReactWindowRegistry}.
	 * @param account
	 *        The account the window is logged in as, read where a context was bound - see
	 *        {@link #registerWindow(TLSubSessionContext, String, ReactWindowRegistry)}.
	 * @param registry
	 *        The registry, held weakly - it belongs to the {@code HttpSession}, not to us.
	 */
	private record WindowRef(String windowName, Person account,
			WeakReference<ReactWindowRegistry> registry) {
		// A register entry; see the field comment.
	}

	/**
	 * {@link TypedRuntimeModule} for {@link AgentViewRefreshService}.
	 */
	public static final class Module extends TypedRuntimeModule<AgentViewRefreshService> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<AgentViewRefreshService> getImplementation() {
			return AgentViewRefreshService.class;
		}
	}
}
