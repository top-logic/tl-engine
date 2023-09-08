/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched.model;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.util.sched.Scheduler;

/**
 * A {@link BarrierTestTask} which "always" wants to run.
 * <p>
 * "Always" means: As often as possible. Whenever the {@link Scheduler} ask "when" it returns "now".
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class AlwaysBarrierTestTask extends BarrierTestTask {

	/** Called by the typed configuration for creating a {@link AlwaysBarrierTestTask}. */
	@CalledByReflection
	public AlwaysBarrierTestTask(InstantiationContext context, BarrierTestTaskConfig config) {
		super(context, config);
	}

	@Override
	public long calcNextShed(long notBefore) {
		// Causes the Scheduler to "always" run it.
		nextShed = notBefore;
		return nextShed;
	}

}
