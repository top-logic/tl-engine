/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.monitor.thread;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.ReadOnlyAccessor;

/**
 * Thread information to be used by {@link ThreadListModelBuilder}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ThreadData {

	/** The observed thread. */
	private final Thread _thread;

	/** The last cached stack of the thread. */
	private StackTraceElement[] _stack;

	/** The cached {@link Thread#getState()}. */
	private State _state;

	/**
	 * Creates a {@link ThreadData}.
	 */
	public ThreadData(Thread thread, StackTraceElement[] stack) {
		assert thread != null : "Observed thread may not be null";
		_thread = thread;
		_state = thread.getState();

		setStackTrace(stack);
	}

	@Override
	public String toString() {
		return "ThreadInfo(" + _thread + ")";
	}

	@Override
	public int hashCode() {
		return CollectionUtil.hashCodeLong(_thread.getId());
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}

		if (other instanceof ThreadData) {
			return getThread() == ((ThreadData) other).getThread();
		}

		return false;
	}

	/**
	 * The observed thread.
	 */
	public Thread getThread() {
		return _thread;
	}

	/**
	 * The cached {@link Thread#getState()}.
	 */
	public State getState() {
		return _thread.getState();
	}

	/**
	 * The cached stack trace.
	 */
	public synchronized StackTraceElement[] getStackTrace() {
		return _stack;
	}

	/**
	 * The {@link Thread#getId()} of the {@link #getThread()}.
	 */
	public Long getID() {
		return getThread().getId();
	}

	/**
	 * The {@link Thread#getName()} of the {@link #getThread()}.
	 */
	public String getName() {
		return getThread().getName();
	}

	/** @see #getStackTrace() */
	public synchronized void setStackTrace(StackTraceElement[] stack) {
		_stack = stack;
	}

	/**
	 * Updates the internal state of this thread.
	 * 
	 * @return <code>true</code> if the thread state has changed.
	 */
	public boolean update() {
		return update(getThread().getStackTrace());
	}

	/**
	 * Updates the cached stack.
	 */
	public boolean update(StackTraceElement[] stack) {
		State oldState = _state;

		_state = getThread().getState();
		setStackTrace(stack);

		return oldState != _state;
	}

	/**
	 * Creates a list with information of all running threads.
	 */
	public static Collection<ThreadData> createThreadInfos() {
		return ThreadData.updateThreadInfos(new ArrayList<>());
	}

	/**
	 * Updates the given thread information list.
	 * 
	 * @param currentThreads
	 *        The list of thread informations to be updated.
	 */
	public static Collection<ThreadData> updateThreadInfos(List<ThreadData> currentThreads) {
		Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();

		int writePos = 0;

		// Update existing, drop dead.
		for (int readPos = 0, cnt = currentThreads.size(); readPos < cnt; readPos++) {
			ThreadData info = currentThreads.get(readPos);

			StackTraceElement[] newStack = allStackTraces.remove(info.getThread());
			if (newStack == null) {
				// Thread is dead, drop.
				continue;
			} else {
				info.update(newStack);
				currentThreads.set(writePos++, info);
			}
		}

		// Free space in list.
		while (writePos < currentThreads.size()) {
			currentThreads.remove(currentThreads.size() - 1);
		}
		
		// Add new threads.
		for (Entry<Thread, StackTraceElement[]> entry : allStackTraces.entrySet()) {
			currentThreads.add(new ThreadData(entry.getKey(), entry.getValue()));
		}

		return currentThreads;
	}

	/**
	 * {@link Accessor} for {@link ThreadData#getThread()}
	 */
	public static class ThreadColumn extends ReadOnlyAccessor<ThreadData> {
		/**
		 * Singleton {@link ThreadColumn} instance.
		 */
		public static final ThreadColumn INSTANCE = new ThreadColumn();

		@Override
		public Object getValue(ThreadData info, String property) {
			return info.getThread();
		}
	}

	/**
	 * {@link Accessor} for {@link ThreadData#getName()}
	 */
	public static class ThreadName extends ReadOnlyAccessor<ThreadData> {
		/**
		 * Singleton {@link ThreadColumn} instance.
		 */
		public static final ThreadName INSTANCE = new ThreadName();

		@Override
		public Object getValue(ThreadData info, String property) {
			return info.getName();
		}
	}

	/**
	 * {@link Accessor} for {@link ThreadData#getID()}
	 */
	public static class ThreadId extends ReadOnlyAccessor<ThreadData> {
		/**
		 * Singleton {@link ThreadColumn} instance.
		 */
		public static final ThreadId INSTANCE = new ThreadId();

		@Override
		public Object getValue(ThreadData info, String property) {
			return info.getID();
		}
	}

	/**
	 * {@link Accessor} for {@link ThreadData#getStackTrace()}
	 */
	public static class ThreadStack extends ReadOnlyAccessor<ThreadData> {
		/**
		 * Singleton {@link ThreadColumn} instance.
		 */
		public static final ThreadStack INSTANCE = new ThreadStack();

		@Override
		public Object getValue(ThreadData info, String property) {
			return info.getStackTrace();
		}
	}

	/**
	 * {@link Accessor} for {@link ThreadData#getState()}
	 */
	public static class ThreadState extends ReadOnlyAccessor<ThreadData> {
		/**
		 * Singleton {@link ThreadColumn} instance.
		 */
		public static final ThreadState INSTANCE = new ThreadState();

		@Override
		public Object getValue(ThreadData info, String property) {
			return info.getState();
		}
	}

	/**
	 * {@link Accessor} for {@link Thread#getPriority()}
	 */
	public static class ThreadPriority extends ReadOnlyAccessor<ThreadData> {
		/**
		 * Singleton {@link ThreadColumn} instance.
		 */
		public static final ThreadPriority INSTANCE = new ThreadPriority();

		@Override
		public Object getValue(ThreadData info, String property) {
			return info.getThread().getPriority();
		}
	}

	/**
	 * The {@link Thread} kind.
	 * 
	 * @see Thread#isDaemon()
	 */
	public enum Kind {
		/**
		 * A regular non-daemon thread.
		 */
		REGULAR,

		/**
		 * A daemon thread.
		 */
		DAEMON;
	}

	/**
	 * {@link Accessor} for the {@link Kind} of the thread.
	 */
	public static class ThreadKind extends ReadOnlyAccessor<ThreadData> {
		/**
		 * Singleton {@link ThreadColumn} instance.
		 */
		public static final ThreadKind INSTANCE = new ThreadKind();

		@Override
		public Object getValue(ThreadData info, String property) {
			return info.getThread().isDaemon() ? Kind.DAEMON : Kind.REGULAR;
		}
	}

	/**
	 * {@link Accessor} for {@link Thread#getThreadGroup()}
	 */
	public static class ThreadGroup extends ReadOnlyAccessor<ThreadData> {
		/**
		 * Singleton {@link ThreadColumn} instance.
		 */
		public static final ThreadGroup INSTANCE = new ThreadGroup();

		@Override
		public Object getValue(ThreadData info, String property) {
			return info.getThread().getThreadGroup();
		}
	}

}
