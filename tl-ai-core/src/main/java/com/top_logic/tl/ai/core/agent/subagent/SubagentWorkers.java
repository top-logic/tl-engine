/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.tl.ai.tool.api.AICategory;

/**
 * Runs the subagents that agents start in the background.
 *
 * <p>
 * Background subagents are started only while this service runs. Stopping it - when the application
 * shuts down - cancels every subagent and waits a few seconds for the running ones to end, before the
 * services they use stop.
 * </p>
 *
 * @implNote A background subagent works on a thread of its {@link SubagentJobRegistry}, and the
 *           registries are kept for the process in {@link SubagentRegistries}, not by a service:
 *           saving the agent runtime's configuration must not stop work in progress. Their threads
 *           still need the services a run uses - at the least the {@link ThreadContextManager} every
 *           run is executed in - so they must end before those services stop. A registry
 *           {@link #track(SubagentJobRegistry) reports to the running service} when it is given a
 *           job. Stopping the service {@link SubagentJobRegistry#shutdown() shuts down} every
 *           registry it was told about - running jobs are cancelled through their tokens, queued
 *           ones end cancelled without running - and waits at most {@link #STOP_TIMEOUT} for the
 *           running ones to end. Then it forgets all registries of {@link SubagentRegistries}, so
 *           that a restart starts with fresh ones.
 */
@ServiceDependencies({
	ThreadContextManager.Module.class
})
@ServiceCategory.Of(AICategory.class)
public class SubagentWorkers extends ConfiguredManagedClass<SubagentWorkers.Config<?>> {

	/** How long stopping the service waits, in total, for running subagents to end. */
	public static final Duration STOP_TIMEOUT = Duration.ofSeconds(10);

	/** Configuration of {@link SubagentWorkers}. */
	public interface Config<I extends SubagentWorkers> extends ConfiguredManagedClass.Config<I> {
		// Nothing to configure.
	}

	private static volatile SubagentWorkers _instance;

	/**
	 * The registries that were given a job while this service ran; guarded by itself.
	 *
	 * <p>
	 * Weakly referenced: a registry that {@link SubagentRegistries} forgot and nobody else holds has no
	 * job left to stop - a running job keeps its registry reachable from its thread - and must not be
	 * kept in memory by this set until the service stops.
	 * </p>
	 */
	private final Set<SubagentJobRegistry> _registries = Collections.newSetFromMap(new WeakHashMap<>());

	/** Whether this service has begun to stop; guarded by {@link #_registries}. */
	private boolean _stopped;

	/**
	 * Creates a {@link SubagentWorkers} from configuration.
	 *
	 * @param context
	 *        For instantiation of dependent configured objects.
	 * @param config
	 *        The configuration.
	 */
	public SubagentWorkers(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/** The running instance, or <code>null</code> if the service is not started. */
	public static SubagentWorkers getInstance() {
		return _instance;
	}

	@Override
	protected void startUp() {
		super.startUp();
		_instance = this;
	}

	@Override
	protected void shutDown() {
		List<SubagentJobRegistry> registries;
		synchronized (_registries) {
			_stopped = true;
			registries = new ArrayList<>(_registries);
			_registries.clear();
		}
		int stillRunning = shutdown(registries, STOP_TIMEOUT.toMillis());
		if (stillRunning > 0) {
			Logger.warn(stillRunning + " subagent(s) still running " + STOP_TIMEOUT.toSeconds()
				+ " seconds after they were cancelled; the services they use stop nevertheless.",
				SubagentWorkers.class);
		}
		SubagentRegistries.clear();
		_instance = null;
		super.shutDown();
	}

	/**
	 * Tells the running service about a registry that is given a job, so that stopping the service
	 * stops the registry's jobs.
	 *
	 * <p>
	 * A registry given a job once the service has begun to stop is shut down at once: its jobs end
	 * cancelled without running. Without a service - a unit test that drives a registry by itself -
	 * nothing happens.
	 * </p>
	 */
	static void track(SubagentJobRegistry registry) {
		SubagentWorkers service = _instance;
		if (service != null) {
			service.add(registry);
		}
	}

	private void add(SubagentJobRegistry registry) {
		synchronized (_registries) {
			if (!_stopped) {
				_registries.add(registry);
				return;
			}
		}
		registry.shutdown();
	}

	/**
	 * Shuts down the given registries and waits for their running jobs to end.
	 *
	 * @param registries
	 *        The registries to shut down.
	 * @param timeoutMillis
	 *        How long to wait, in total, for the running jobs to end.
	 * @return How many jobs were still running when the wait ended.
	 */
	static int shutdown(Collection<SubagentJobRegistry> registries, long timeoutMillis) {
		for (SubagentJobRegistry registry : registries) {
			registry.shutdown();
		}
		long deadline = System.currentTimeMillis() + timeoutMillis;
		int stillRunning = 0;
		for (SubagentJobRegistry registry : registries) {
			if (!registry.awaitTermination(Math.max(0, deadline - System.currentTimeMillis()))) {
				stillRunning += Math.max(1, registry.startedCount());
			}
		}
		return stillRunning;
	}

	/**
	 * {@link TypedRuntimeModule} for {@link SubagentWorkers}.
	 */
	public static final class Module extends TypedRuntimeModule<SubagentWorkers> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton.
		}

		@Override
		public Class<SubagentWorkers> getImplementation() {
			return SubagentWorkers.class;
		}
	}
}
