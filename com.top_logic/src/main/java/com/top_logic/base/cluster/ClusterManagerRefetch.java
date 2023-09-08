/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.cluster;

import com.top_logic.basic.sched.SchedulerServiceHandle;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.basic.util.SystemContextRunnable;

/**
 * A {@link Runnable} that calls {@link ClusterManager#refetch()} once.
 * <p>
 * <em>Has to be run in
 * {@link ThreadContext#inSystemContext(Class, com.top_logic.basic.util.ComputationEx2) system
 * context} </em>, for example by wrapping it with a {@link SystemContextRunnable}.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class ClusterManagerRefetch implements Runnable {

	private final ClusterManager _clusterManager;

	/**
	 * Creates a {@link ClusterManagerRefetch} for the given {@link ClusterManager}.
	 * 
	 * @param clusterManager
	 *        Must not be <code>null</code>. The {@link ClusterManager} to refetch.
	 * @throws NullPointerException
	 *         If the {@link ClusterManager} is null.
	 * 
	 * @see #createPeriodic(ClusterManager)
	 * @see #createOnce(ClusterManager)
	 * @see #createOnceRaw(ClusterManager)
	 */
	private ClusterManagerRefetch(ClusterManager clusterManager) {
		if (clusterManager == null) {
			throw new NullPointerException("ClusterManager is not allowed to be null.");
		}
		_clusterManager = clusterManager;
	}

	/**
	 * Creates a periodically running {@link ClusterManagerRefetch} for the given
	 * {@link ClusterManager}.
	 * <p>
	 * Call {@link SchedulerServiceHandle#start(long, java.util.concurrent.TimeUnit)} to start the periodic
	 * refetch and {@link SchedulerServiceHandle#stop(long, java.util.concurrent.TimeUnit)} to stop it. <br/>
	 * The {@link ClusterManagerRefetch} will be wrapped in a {@link SystemContextRunnable}.
	 * </p>
	 * 
	 * @param clusterManager
	 *        The {@link ClusterManager} to refetch. Must not be <code>null</code>.
	 * @throws NullPointerException
	 *         If the {@link ClusterManager} is null.
	 */
	public static SchedulerServiceHandle createPeriodic(ClusterManager clusterManager) throws NullPointerException {
		return new SchedulerServiceHandle(createOnce(clusterManager));
	}

	/**
	 * Creates a {@link ClusterManagerRefetch} for the given {@link ClusterManager}, that will do
	 * one refetch on every call of {@link ClusterManagerRefetch#run()}.
	 * <p>
	 * The {@link ClusterManagerRefetch} will be wrapped in a {@link SystemContextRunnable}.
	 * </p>
	 * 
	 * @param clusterManager
	 *        The {@link ClusterManager} to refetch. Must not be <code>null</code>.
	 * @throws NullPointerException
	 *         If the {@link ClusterManager} is null.
	 */
	public static Runnable createOnce(ClusterManager clusterManager) throws NullPointerException {
		return new SystemContextRunnable<>(createOnceRaw(clusterManager));
	}

	/**
	 * Creates a {@link ClusterManagerRefetch} for the given {@link ClusterManager}, but without
	 * {@link SystemContextRunnable} wrapped around it.
	 * <p>
	 * <em>The {@link ClusterManagerRefetch} has to be run in
	 * {@link ThreadContext#inSystemContext(Class, com.top_logic.basic.util.ComputationEx2) system
	 * context}</em>, for example by wrapping it with a {@link SystemContextRunnable}. Use
	 * {@link #createOnce(ClusterManager)} instead, unless there is a good reason for taking care of
	 * the {@link ThreadContext} yourself.
	 * </p>
	 * 
	 * @param clusterManager
	 *        The {@link ClusterManager} to refetch. Must not be <code>null</code>.
	 * @throws NullPointerException
	 *         If the {@link ClusterManager} is null.
	 */
	public static ClusterManagerRefetch createOnceRaw(ClusterManager clusterManager) throws NullPointerException {
		return new ClusterManagerRefetch(clusterManager);
	}

	@Override
	public void run() {
		_clusterManager.refetch();
	}

	@Override
	public String toString() {
		return NameBuilder.buildName(this, _clusterManager.toString());
	}

}
