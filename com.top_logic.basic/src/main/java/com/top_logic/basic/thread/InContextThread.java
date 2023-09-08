/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.thread;

/**
 * {@link Thread} that executes {@link #inContext()} in parallel in the {@link ThreadContext}
 * returned from {@link #getContext()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InContextThread extends Thread implements InContext {

	private final ThreadContext _context;

	/**
	 * Creates a {@link InContextThread}.
	 * 
	 * @param context
	 *        The {@link ThreadContext} to execute in.
	 * 
	 * @see Thread#Thread()
	 */
	public InContextThread(ThreadContext context) {
		super();
		_context = context;
		_context.lock();
	}

	/**
	 * Creates a {@link InContextThread}.
	 * 
	 * @param context
	 *        The {@link ThreadContext} to execute in.
	 * 
	 * @see Thread#Thread(Runnable, String)
	 */
	public InContextThread(ThreadContext context, Runnable target, String name) {
		super(target, name);
		_context = context;
		_context.lock();
	}

	/**
	 * Creates a {@link InContextThread}.
	 * 
	 * @param context
	 *        The {@link ThreadContext} to execute in.
	 * 
	 * @see Thread#Thread(Runnable)
	 */
	public InContextThread(ThreadContext context, Runnable target) {
		super(target);
		_context = context;
		_context.lock();
	}

	/**
	 * Creates a {@link InContextThread}.
	 * 
	 * @param context
	 *        The {@link ThreadContext} to execute in.
	 * 
	 * @see Thread#Thread(String)
	 */
	public InContextThread(ThreadContext context, String name) {
		super(name);
		_context = context;
		_context.lock();
	}

	/**
	 * The {@link ThreadContext} to execute in.
	 */
	protected final ThreadContext getContext() {
		return _context;
	}

	@Override
	public final void run() {
		ThreadContext context = getContext();
		try {
			context.inContext(this);
		} finally {
			context.unlock();
		}
	}

	@Override
	public void inContext() {
		super.run();
	}

}