/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.Computation;

/**
 * {@link Computation} of a {@link List} that can be asynchronously started.
 * 
 * <p>
 * {@link #start() Starting} the computation yields a future implementing the {@link List}
 * interface. Accessing any of the {@link List} methods waits until the computation is finished,
 * like the call {@link Future#get()} does.
 * </p>
 * 
 * <p>
 * The actual computation hapens in a separate thread but using the same context as the thread
 * creating the computation.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ListComputation<T> implements Callable<List<? extends T>>, Computation<List<? extends T>> {

	private final SubSessionContext _subSession;

	/**
	 * Creates a {@link ListComputation}.
	 * 
	 * <p>
	 * Note: The computation must be {@link #start() started} after construction.
	 * </p>
	 */
	public ListComputation() {
		_subSession = ThreadContextManager.getSubSession();
	}

	/**
	 * Starts the computation.
	 * 
	 * <p>
	 * Note: This method must not be invoked directly from the constructor to avoid accessing
	 * uninitialized state from within the started thread.
	 * </p>
	 * 
	 * @return The {@link List} future.
	 */
	public List<T> start() {
		_subSession.lock();
		return new ProxyList<>(SchedulerService.getInstance().submit(this));
	}

	@Override
	public final List<? extends T> call() {
		List<? extends T> result = ThreadContextManager.inContext(_subSession, this);
		_subSession.unlock();
		return result;
	}

	private static final class ProxyList<T> extends LazyListUnmodifyable<T> {

		private final Future<List<? extends T>> _future;

		public ProxyList(Future<List<? extends T>> future) {
			_future = future;
		}

		@Override
		protected final List<? extends T> initInstance() {
			try {
				return _future.get();
			} catch (InterruptedException ex) {
				throw new RuntimeException(ex);
			} catch (ExecutionException ex) {
				throw toRuntime(ex);
			}
		}

		private RuntimeException toRuntime(ExecutionException ex) throws Error, UnreachableAssertion {
			Throwable cause = ex.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			}
			if (cause instanceof Error) {
				throw (Error) cause;
			}
			throw new UnreachableAssertion(ex);
		}
	}

}
