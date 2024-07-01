/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sched;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.Suspendable;

/**
 * Service providing a singleton {@link ScheduledExecutorService}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SchedulerService extends ConfiguredManagedClass<SchedulerService.Config>
		implements ScheduledExecutorService, Suspendable {

	/**
	 * Configuration options for {@link SchedulerService}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface Config extends ConfiguredManagedClass.Config<SchedulerService> {

		/**
		 * The number of threads to use.
		 * 
		 * @see ScheduledThreadPoolExecutor#getCorePoolSize()
		 */
		int getCorePoolSize();

	}

	private ScheduledExecutorService _executor;

	private volatile boolean _suspended = false;

	/**
	 * Creates a configured {@link SchedulerService}.
	 */
	public SchedulerService(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();

		ThreadFactory threadFactory = DaemonThreadFactory.daemonThreadFactory();
		int poolSize = getConfig().getCorePoolSize();
		_executor = Executors.newScheduledThreadPool(poolSize, threadFactory);
	}

	@Override
	protected void shutDown() {
		_executor.shutdown();

		super.shutDown();
	}

	/**
	 * Must not be called on a service.
	 */
	@Deprecated
	@Override
	public void shutdown() {
		throw noLifeCycleMethods();
	}

	/**
	 * Must not be called on a service.
	 */
	@Deprecated
	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		throw noLifeCycleMethods();
	}

	/**
	 * Must not be called on a service.
	 */
	@Deprecated
	@Override
	public List<Runnable> shutdownNow() {
		throw noLifeCycleMethods();
	}

	private UnsupportedOperationException noLifeCycleMethods() {
		return new UnsupportedOperationException("Must not call executor life-cycle methods on a service.");
	}

	@Override
	public boolean isShutdown() {
		return _executor.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return _executor.isTerminated();
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return _executor.submit(task);
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return _executor.submit(task, result);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return _executor.submit(task);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return _executor.invokeAll(tasks);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		return _executor.invokeAll(tasks, timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return _executor.invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return _executor.invokeAny(tasks, timeout, unit);
	}

	@Override
	public void execute(Runnable command) {
		_executor.execute(command);
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		return _executor.schedule(command, delay, unit);
	}

	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
		return _executor.schedule(callable, delay, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return _executor.scheduleAtFixedRate(wrap(command), initialDelay, period, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		return _executor.scheduleWithFixedDelay(wrap(command), initialDelay, delay, unit);
	}

	private Runnable wrap(final Runnable command) {
		return new Runnable() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void run() {
				if (isSuspended()) {
					wrapperLogSuspended(command);
					return;
				}
				try {
					command.run();
				} catch (Throwable ex) {
					wrapperLogThrowable(command, ex);
				}
			}

		};
	}

	private void wrapperLogSuspended(final Object command) {
		logDebug("Not executing '" + StringServices.getObjectDescription(command) + "' as the "
			+ SchedulerService.class.getSimpleName() + " is suspended.");
	}

	private void wrapperLogThrowable(final Object command, Throwable ex) {
		logError("Execution of '" + StringServices.getObjectDescription(command) + "' failed.", ex);
	}

	@Override
	public void suspend() {
		_suspended = true;
		logInfo("Suspending");
	}

	@Override
	public void resume() {
		_suspended = false;
		logInfo("Resuming");
	}

	@Override
	public boolean isSuspended() {
		return _suspended;
	}

	private void logDebug(String message) {
		if (Logger.isDebugEnabled(SchedulerService.class)) {
			Logger.debug(message, SchedulerService.class);
		}
	}

	private void logInfo(String message) {
		Logger.info(message, SchedulerService.class);
	}

	private void logError(String message, Throwable throwable) {
		Logger.error(message + " Cause: " + throwable.getMessage(), throwable, SchedulerService.class);
	}

	/**
	 * The {@link SchedulerService} singleton.
	 */
	public static SchedulerService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module of {@link SchedulerService}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class Module extends TypedRuntimeModule<SchedulerService> {

		/**
		 * Singleton {@link SchedulerService.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<SchedulerService> getImplementation() {
			return SchedulerService.class;
		}

	}

}
