/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.top_logic.basic.Logger;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.scripting.ReactWindowReplay;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Delivers values to a {@link ViewChannel} of a window from any thread.
 *
 * <p>
 * A {@link ViewChannel} belongs to the window that displays it: writing it runs its listeners,
 * updates the controls bound to it and sends their updates to the browser, all of which must be
 * serialized against the requests the window serves. A thread that does not serve the window - a
 * background job, a listener of an external system, a timer - therefore does not write the channel
 * itself but hands the value to a {@link ChannelPublisher}, which writes it on its behalf.
 * </p>
 *
 * <h2>Threading</h2>
 *
 * <p>
 * {@link #publish(Object)} may be called from any thread. It never blocks the caller beyond a short
 * internal lock and never runs a channel listener on the calling thread: it records the value and
 * hands its delivery to the {@link SchedulerService}. The value is then written on a thread of the
 * scheduler, in the sub-session that was current when the publisher was created, and under the
 * {@link ReactWindowRegistry#beginInteraction() interaction} of the window (see
 * {@link ReactWindowReplay}). Writing the channel is thereby serialized against the requests of the
 * same session exactly like a command is.
 * </p>
 *
 * <p>
 * Values are delivered one at a time and in the order they were published.
 * </p>
 *
 * <h2>Throttling and conflation</h2>
 *
 * <p>
 * A channel holds state, not events: what counts is the value it ends up with, not every value it
 * passed through. With a positive update interval, the publisher delivers at most one value per
 * interval. A value published while the interval since the last delivery is still running is held
 * back and delivered once it has passed; a value published while an earlier one is still held back
 * replaces it. The last value of a burst is thus always delivered, and an older value is never
 * delivered after a newer one. With an update interval of zero, every published value is delivered,
 * as soon as the scheduler gets to it.
 * </p>
 *
 * <p>
 * {@link #publishNow(Object)} publishes a value that is delivered without waiting for the update
 * interval - the value that concludes what the channel shows, for example. It is ordered like any
 * other value: it replaces a value that is still held back, so it is not overwritten afterwards by
 * a value published before it.
 * </p>
 *
 * <h2>Windows and the headless case</h2>
 *
 * <p>
 * Without a {@link ReactContext}, or with a context that has no
 * {@link ReactContext#getWindowRegistry() window registry} - a view that is not displayed, as in a
 * test - the value is written directly on the scheduler thread, in the captured sub-session. When
 * the window is gone by the time a value is delivered (the page was closed or reloaded), the value
 * is dropped, unless the publisher was created to apply its values without a window as well.
 * </p>
 *
 * <h2>Failures and closing</h2>
 *
 * <p>
 * A failure raised while a value is applied is logged and does not stop the publisher: later
 * values are delivered as usual.
 * </p>
 *
 * <p>
 * {@link #close()} ends the publisher: values held back are dropped, values published afterwards
 * are ignored, and a delivery the scheduler runs afterwards does nothing. A delivery that is
 * running while the publisher is closed completes.
 * </p>
 */
public class ChannelPublisher implements ChannelSink {

	private final ReactContext _context;

	private final long _updateInterval;

	private final Consumer<Object> _apply;

	private final boolean _applyWithoutWindow;

	private final SubSessionContext _subSession;

	/** Guards the delivery state below. */
	private final Object _lock = new Object();

	/**
	 * The values published and not yet delivered, oldest first.
	 *
	 * <p>
	 * With a positive update interval, this holds at most one value.
	 * </p>
	 */
	private final Deque<Object> _pending = new ArrayDeque<>();

	/** Whether the value held back is to be delivered without waiting for the update interval. */
	private boolean _urgent;

	/** Whether a value is being delivered right now. */
	private boolean _delivering;

	/** When the last delivery started, in milliseconds. */
	private long _lastDelivery;

	/** The delivery last handed to the scheduler, if it may not have started yet. */
	private ScheduledFuture<?> _scheduled;

	private boolean _closed;

	/**
	 * Creates a {@link ChannelPublisher} writing the published values to the given channel.
	 *
	 * <p>
	 * Called on a thread serving a request of the window: the current sub-session is the one the
	 * values are delivered in.
	 * </p>
	 *
	 * @param context
	 *        The context of the window the channel belongs to, {@code null} for a view that is not
	 *        displayed.
	 * @param target
	 *        The channel to write.
	 * @param updateInterval
	 *        The shortest time in milliseconds between two deliveries; zero to deliver every value.
	 */
	public ChannelPublisher(ReactContext context, ViewChannel target, long updateInterval) {
		this(context, updateInterval, target::set, false);
	}

	/**
	 * Creates a {@link ChannelPublisher} handing the published values to the given function.
	 *
	 * <p>
	 * Called on a thread serving a request of the window: the current sub-session is the one the
	 * values are delivered in.
	 * </p>
	 *
	 * @param context
	 *        The context of the window the values are delivered to, {@code null} for a view that is
	 *        not displayed.
	 * @param updateInterval
	 *        The shortest time in milliseconds between two deliveries; zero to deliver every value.
	 * @param apply
	 *        Applies a delivered value, typically by writing it to a channel. Called under the
	 *        interaction of the window, so that it may do whatever a command may do in the window.
	 * @param applyWithoutWindow
	 *        Whether a value is still applied when the window is gone by the time it is delivered,
	 *        so that the work depending on it is not left waiting. Otherwise, such a value is
	 *        dropped.
	 */
	public ChannelPublisher(ReactContext context, long updateInterval, Consumer<Object> apply,
			boolean applyWithoutWindow) {
		_context = context;
		_updateInterval = Math.max(0, updateInterval);
		_apply = apply;
		_applyWithoutWindow = applyWithoutWindow;
		_subSession = ThreadContextManager.getSubSession();
	}

	/**
	 * Publishes the given value to be delivered once the update interval since the last delivery
	 * has passed.
	 *
	 * <p>
	 * May be called from any thread; does not block and does not apply the value on the calling
	 * thread.
	 * </p>
	 *
	 * @param value
	 *        The value the channel is to hold.
	 */
	@Override
	public void publish(Object value) {
		enqueue(value, false);
	}

	/**
	 * Publishes the given value to be delivered as soon as possible, without waiting for the update
	 * interval.
	 *
	 * <p>
	 * May be called from any thread; does not block and does not apply the value on the calling
	 * thread. A value published earlier that is still held back is replaced by this one; a value
	 * that is being delivered right now completes its delivery before this one is delivered.
	 * </p>
	 *
	 * @param value
	 *        The value the channel is to hold.
	 */
	public void publishNow(Object value) {
		enqueue(value, true);
	}

	/**
	 * Ends this publisher; nothing is delivered afterwards.
	 *
	 * <p>
	 * May be called from any thread, any number of times, also from within the delivery of a
	 * value.
	 * </p>
	 */
	public void close() {
		synchronized (_lock) {
			_closed = true;
			_pending.clear();
			_urgent = false;
			if (_scheduled != null) {
				_scheduled.cancel(false);
				_scheduled = null;
			}
		}
	}

	private void enqueue(Object value, boolean urgent) {
		synchronized (_lock) {
			if (_closed) {
				return;
			}
			if (_updateInterval > 0) {
				_pending.clear();
			}
			_pending.add(value);
			_urgent |= urgent;
			if (!_delivering) {
				// Otherwise, the running delivery takes the value up once it is done.
				schedule(System.currentTimeMillis());
			}
		}
	}

	/**
	 * Makes sure a delivery is scheduled for the time the next value is due.
	 *
	 * <p>
	 * Called while holding {@link #_lock}, with values pending and no delivery running.
	 * </p>
	 */
	private void schedule(long now) {
		long delay = delay(now);
		if (_scheduled != null && !_scheduled.isDone()) {
			if (delay > 0 || _scheduled.getDelay(TimeUnit.MILLISECONDS) <= 0) {
				// The scheduled delivery comes in time.
				return;
			}
			_scheduled.cancel(false);
		}
		_scheduled = SchedulerService.getInstance().schedule(this::drain, delay, TimeUnit.MILLISECONDS);
	}

	/**
	 * The time in milliseconds until the value held back is due.
	 *
	 * <p>
	 * Called while holding {@link #_lock}.
	 * </p>
	 */
	private long delay(long now) {
		if (_urgent) {
			return 0;
		}
		return Math.max(0, _lastDelivery + _updateInterval - now);
	}

	/**
	 * Delivers the pending values that are due, one after the other.
	 *
	 * <p>
	 * Runs on a scheduler thread. Several runs may be scheduled at a time; a run that finds another
	 * one delivering, nothing pending, or nothing due yet leaves the work to that one or to a later
	 * run.
	 * </p>
	 */
	private void drain() {
		while (true) {
			Object value;
			synchronized (_lock) {
				// This run has started, so it no longer counts as the one to wait for.
				_scheduled = null;
				if (_delivering || _closed || _pending.isEmpty()) {
					return;
				}
				long now = System.currentTimeMillis();
				if (delay(now) > 0) {
					schedule(now);
					return;
				}
				value = _pending.poll();
				if (_pending.isEmpty()) {
					_urgent = false;
				}
				_delivering = true;
				_lastDelivery = now;
			}
			try {
				deliver(value);
			} catch (Throwable ex) {
				Logger.error("Failed to deliver a value to a view channel.", ex, ChannelPublisher.class);
			} finally {
				synchronized (_lock) {
					_delivering = false;
				}
			}
		}
	}

	/**
	 * Applies the given value in the captured sub-session and under the interaction of the window.
	 */
	private void deliver(Object value) {
		if (_subSession == null) {
			applyInWindow(value);
		} else {
			ThreadContextManager.inContext(_subSession, () -> applyInWindow(value));
		}
	}

	private void applyInWindow(Object value) {
		ReactWindowRegistry registry = _context == null ? null : _context.getWindowRegistry();
		if (registry == null) {
			apply(value);
			return;
		}
		if (!ReactWindowReplay.inWindow(registry, _context.getWindowName(), () -> apply(value))) {
			// The window is gone.
			if (_applyWithoutWindow) {
				apply(value);
			}
		}
	}

	private void apply(Object value) {
		synchronized (_lock) {
			if (_closed) {
				return;
			}
		}
		_apply.accept(value);
	}

}
