/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.schedule;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.util.sched.task.Task;

/**
 * A {@link AbstractSchedulingAlgorithm} for: "Always run that {@link Task}."
 * <p>
 * Always returns a clone of the given 'now'. <br/>
 * Useful for example for tests and experiments. Is not located in the test package, as it is
 * sometimes useful for experiments that use the running application.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class AlwaysSchedule extends AbstractSchedulingAlgorithm<AlwaysSchedule.Config> {

	/** {@link TypedConfiguration} of {@link AlwaysSchedule}. */
	public interface Config extends AbstractSchedulingAlgorithm.Config<AlwaysSchedule> {
		// Nothing needed but the type parameter
	}

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link AlwaysSchedule}.
	 * 
	 * @param context
	 *        For instantiation of dependent configured objects.
	 * @param config
	 *        The configuration of this {@link AlwaysSchedule}.
	 */
	@CalledByReflection
	public AlwaysSchedule(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public long nextSchedule(long notBefore, long lastSchedule) {
		return notBefore;
	}

}
