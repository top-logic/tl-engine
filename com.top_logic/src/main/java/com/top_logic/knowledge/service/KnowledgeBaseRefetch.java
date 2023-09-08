/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.Logger;
import com.top_logic.basic.sched.SchedulerServiceHandle;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.basic.util.SystemContextRunnable;

/**
 * A {@link Runnable} that calls {@link KnowledgeBase#refetch()} once.
 * <p>
 * <em>Has to be run in
 * {@link ThreadContext#inSystemContext(Class, com.top_logic.basic.util.ComputationEx2) system
 * context} </em>, for example by wrapping it with a {@link SystemContextRunnable}.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class KnowledgeBaseRefetch implements Runnable {

	private final KnowledgeBase _knowledgeBase;

	/**
	 * Creates a {@link KnowledgeBaseRefetch} for the given {@link KnowledgeBase}.
	 * 
	 * @param knowledgeBase
	 *        Must not be <code>null</code>. The {@link KnowledgeBase} to refetch.
	 * @throws NullPointerException
	 *         If the {@link KnowledgeBase} is null.
	 * 
	 * @see #createPeriodic(KnowledgeBase)
	 * @see #createOnce(KnowledgeBase)
	 * @see #createOnceRaw(KnowledgeBase)
	 */
	private KnowledgeBaseRefetch(KnowledgeBase knowledgeBase) throws NullPointerException {
		if (knowledgeBase == null) {
			throw new NullPointerException("KnowledgeBase is not allowed to be null.");
		}
		_knowledgeBase = knowledgeBase;
	}

	/**
	 * Creates a periodically running {@link KnowledgeBaseRefetch} for the given
	 * {@link KnowledgeBase}.
	 * <p>
	 * Call {@link SchedulerServiceHandle#start(long, java.util.concurrent.TimeUnit)} to start the periodic
	 * refetch and {@link SchedulerServiceHandle#stop(long, java.util.concurrent.TimeUnit)} to stop it. <br/>
	 * The {@link KnowledgeBaseRefetch} will be wrapped in a {@link SystemContextRunnable}.
	 * </p>
	 * 
	 * @param knowledgeBase
	 *        The {@link KnowledgeBase} to refetch. Must not be <code>null</code>.
	 * @throws NullPointerException
	 *         If the {@link KnowledgeBase} is null.
	 */
	public static SchedulerServiceHandle createPeriodic(KnowledgeBase knowledgeBase) throws NullPointerException {
		return new SchedulerServiceHandle(createOnce(knowledgeBase));
	}

	/**
	 * Creates a {@link KnowledgeBaseRefetch} for the given {@link KnowledgeBase}, that will do one
	 * refetch on every call of {@link KnowledgeBaseRefetch#run()}.
	 * <p>
	 * The {@link KnowledgeBaseRefetch} will be wrapped in a {@link SystemContextRunnable}.
	 * </p>
	 * 
	 * @param knowledgeBase
	 *        The {@link KnowledgeBase} to refetch. Must not be <code>null</code>.
	 * @throws NullPointerException
	 *         If the {@link KnowledgeBase} is null.
	 */
	public static Runnable createOnce(KnowledgeBase knowledgeBase) throws NullPointerException {
		return new SystemContextRunnable<>(createOnceRaw(knowledgeBase));
	}

	/**
	 * Creates a {@link KnowledgeBaseRefetch} for the given {@link KnowledgeBase}, but without
	 * {@link SystemContextRunnable} wrapped around it.
	 * <p>
	 * <em>The {@link KnowledgeBaseRefetch} has to be run in
	 * {@link ThreadContext#inSystemContext(Class, com.top_logic.basic.util.ComputationEx2) system
	 * context}</em>, for example by wrapping it with a {@link SystemContextRunnable}. Use
	 * {@link #createOnce(KnowledgeBase)} instead, unless there is a good reason for taking care of
	 * the {@link ThreadContext} yourself.
	 * </p>
	 * 
	 * @param knowledgeBase
	 *        The {@link KnowledgeBase} to refetch. Must not be <code>null</code>.
	 * @throws NullPointerException
	 *         If the {@link KnowledgeBase} is null.
	 */
	public static KnowledgeBaseRefetch createOnceRaw(KnowledgeBase knowledgeBase) throws NullPointerException {
		return new KnowledgeBaseRefetch(knowledgeBase);
	}

	@Override
	public void run() {
		try {
			int updateCount = _knowledgeBase.refetch();
			if (updateCount > 0) {
				Logger.info("KnowledgeBase refetched " + updateCount + " objects.", KnowledgeBaseRefetch.class);
			}
		} catch (RefetchTimeout error) {
			Logger.error("KnowledgeBaseRefetch canceled after timeout. Cause: " + error.getMessage(), error,
				KnowledgeBaseRefetch.class);
		}
	}

	@Override
	public String toString() {
		return NameBuilder.buildName(this, _knowledgeBase.toString());
	}

}
