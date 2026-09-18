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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

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
import com.top_logic.layout.react.window.ReactWindowRegistry;
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
 * the affected account, synthesizes the events in that window's sub-session and <em>under the
 * window's request lock</em>. The lock is what makes this safe beside a browser command: a React
 * control's state map is a plain {@code HashMap}, so pushing state from two threads at once would
 * corrupt it. (The heartbeat path in the engine skips the lock; this deliberately does not copy
 * that.)
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
		 * How long a refresh waits for a window's request lock, in milliseconds.
		 *
		 * <p>
		 * The lock is held while that window runs a browser command, and such a command synthesizes
		 * the model events itself when it finishes - so giving up costs nothing but a little
		 * latency, whereas waiting would tie up a scheduler thread behind a slow command.
		 * </p>
		 *
		 * @see #LOCK_TIMEOUT_MILLIS
		 */
		@Name(LOCK_TIMEOUT_MILLIS)
		@IntDefault(500)
		int getLockTimeoutMillis();

		/**
		 * How long a write into a window waits for that window's request lock, in milliseconds.
		 *
		 * <p>
		 * Longer than {@link #getLockTimeoutMillis()}, and for the opposite reason: a refresh that
		 * gives up loses nothing, because the command holding the lock synthesizes the same events
		 * when it finishes. A write has no such second chance - nobody else is going to put the
		 * agent's value on the channel - so giving up means telling the model that the page could
		 * not be reached.
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

	/** Accounts whose data changed since the last refresh. */
	private final Set<Person> _pending = ConcurrentHashMap.newKeySet();

	/** Whether a change of no particular account is pending, which refreshes every window. */
	private final AtomicBoolean _pendingAll = new AtomicBoolean();

	/** The scheduled refresh, or <code>null</code> when none is due. */
	private volatile ScheduledFuture<?> _scheduled;

	private static volatile AgentViewRefreshService _instance;

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
		_instance = this;
		AgentExecutionStore.getInstance().addChangeListener(this);
	}

	@Override
	protected void shutDown() {
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
		service._windows.put(subSession, new WindowRef(windowName, new WeakReference<>(registry)));
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
	 * Runs {@code action} in the browser window of {@code subSession}, under that window's request
	 * lock, with the window's pending model changes applied first.
	 *
	 * <p>
	 * This is the write counterpart of the refresh: same window register, same lock discipline, but
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
	 *        What to do in the window. Runs on the calling thread.
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
		return ThreadContextManager.inContext(subSession,
			(ComputationEx2<WindowAccess, RuntimeException, RuntimeException>) () -> {
				ReentrantLock lock = registry.getRequestLock();
				boolean locked;
				try {
					locked = lock.tryLock(service.getConfig().getWriteTimeoutMillis(), TimeUnit.MILLISECONDS);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
					return WindowAccess.BUSY;
				}
				if (!locked) {
					return WindowAccess.BUSY;
				}
				try {
					registry.synthesizeModelEvents(window.windowName());
					action.run();
					return WindowAccess.DONE;
				} finally {
					lock.unlock();
				}
			});
	}

	@Override
	public void onAgentDataChanged(Person owner) {
		if (owner == null) {
			_pendingAll.set(true);
		} else {
			_pending.add(owner);
		}
		schedule();
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
			if (owner != null && !owner.equals(subSession.getPerson())) {
				continue;
			}
			TaskProgressTracker tracker = subSession.get(ChatSessionRegistry.CHAT_TRACKER);
			if (tracker == null) {
				continue;
			}
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

	/** Schedules the refresh unless one is already due, which is what coalesces a burst of writes. */
	private synchronized void schedule() {
		ScheduledFuture<?> scheduled = _scheduled;
		if (scheduled != null && !scheduled.isDone()) {
			return;
		}
		if (!SchedulerService.Module.INSTANCE.isActive()) {
			return;
		}
		_scheduled = SchedulerService.getInstance().schedule(this::refreshPending,
			getConfig().getRefreshDelayMillis(), TimeUnit.MILLISECONDS);
	}

	/** Refreshes the windows of every account that changed since the last run. */
	private void refreshPending() {
		boolean all = _pendingAll.getAndSet(false);
		Set<Person> owners = Set.copyOf(_pending);
		_pending.removeAll(owners);
		if (!all && owners.isEmpty()) {
			return;
		}
		for (Map.Entry<TLSubSessionContext, WindowRef> entry : snapshot()) {
			TLSubSessionContext subSession = entry.getKey();
			if (!all && !owners.contains(subSession.getPerson())) {
				continue;
			}
			refresh(subSession, entry.getValue());
		}
	}

	/** A stable copy of the register, so the refresh does not iterate it while a render adds to it. */
	private List<Map.Entry<TLSubSessionContext, WindowRef>> snapshot() {
		synchronized (_windows) {
			return new ArrayList<>(_windows.entrySet());
		}
	}

	/**
	 * Pushes the pending model changes into one window, in its own sub-session and under its request
	 * lock. Best-effort: a window that cannot be reached is skipped, never logged as a failure of
	 * the run that caused the refresh.
	 */
	private void refresh(TLSubSessionContext subSession, WindowRef window) {
		ReactWindowRegistry registry = window.registry().get();
		if (registry == null) {
			return;
		}
		try {
			ThreadContextManager.inContext(subSession, () -> {
				ReentrantLock lock = registry.getRequestLock();
				boolean locked;
				try {
					locked = lock.tryLock(getConfig().getLockTimeoutMillis(), TimeUnit.MILLISECONDS);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
					return;
				}
				if (!locked) {
					// A browser command holds the lock, and that command synthesizes the events
					// itself when it finishes - so this refresh has nothing left to do.
					return;
				}
				try {
					registry.synthesizeModelEvents(window.windowName());
				} finally {
					lock.unlock();
				}
			});
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
	 * @param registry
	 *        The registry, held weakly - it belongs to the {@code HttpSession}, not to us.
	 */
	private record WindowRef(String windowName, WeakReference<ReactWindowRegistry> registry) {
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
