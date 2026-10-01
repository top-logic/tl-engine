/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.schedule;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.util.sched.Scheduler;

/**
 * {@link SchedulingAlgorithm} that runs a task once after each start of the task scheduler.
 * 
 * <p>
 * The task runs once on each cluster node after the application has fully started, and again when
 * the task scheduler service is restarted. For a task that runs only on one node in the cluster, a
 * run on any other node after the start of this node's task scheduler counts as the run for this
 * start.
 * </p>
 * 
 * @implNote The start of the task scheduler is {@link Scheduler#getDispatchStart()}.
 */
@InApp
public class OnStartup implements SchedulingAlgorithm {

	/**
	 * Configuration options for {@link OnStartup}.
	 */
	@TagName("on-startup")
	public interface Config<I extends OnStartup> extends PolymorphicConfiguration<I> {
		// Marker only.
	}

	/**
	 * Creates a {@link OnStartup} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public OnStartup(InstantiationContext context, Config<?> config) {
		super();
	}

	@Override
	public long nextSchedule(long notBefore, long lastSchedule) {
		if (lastSchedule == NO_SCHEDULE || lastSchedule < dispatchStart()) {
			return notBefore;
		}
		return NO_SCHEDULE;
	}

	private static long dispatchStart() {
		Scheduler scheduler = Scheduler.getSchedulerInstance();
		if (scheduler == null) {
			return NO_SCHEDULE;
		}
		return scheduler.getDispatchStart();
	}

	@Override
	public void fillFormGroup(FormGroup group) {
		// No contents.
	}

}
