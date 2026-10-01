/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched.model;

import java.util.Date;
import java.util.Properties;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.util.sched.task.impl.TaskImpl;

/**
 * Minimal concrete {@link TaskImpl} for tests of the scheduling behavior.
 * 
 * <p>
 * The task does nothing but updating its schedule and runs on every cluster node.
 * </p>
 */
@SuppressWarnings("deprecation")
public class TestingTask extends TaskImpl<TestingTask.Config<?>> {

	/**
	 * Configuration options for {@link TestingTask}.
	 */
	public interface Config<I extends TestingTask> extends TaskImpl.Config<I> {

		@Override
		@ClassDefault(TestingTask.class)
		Class<? extends I> getImplementationClass();

	}

	/**
	 * Creates a {@link TestingTask} from configuration.
	 */
	@CalledByReflection
	public TestingTask(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * Creates a {@link TestingTask} running once a day, week, or month.
	 */
	public TestingTask(String name, int daytype, int daymask, int hour, int minute) {
		super(name, daytype, daymask, hour, minute);
	}

	/**
	 * Creates a {@link TestingTask} running at the given date.
	 */
	public TestingTask(String name, Date when, int hour, int minute) {
		super(name, when, hour, minute);
	}

	/**
	 * Creates a {@link TestingTask} running periodically.
	 */
	public TestingTask(String name, int daytype, int daymask, int startHour, int startMinute, long interval,
			int stopHour, int stopMinute) {
		super(name, daytype, daymask, startHour, startMinute, interval, stopHour, stopMinute);
	}

	/**
	 * Creates a {@link TestingTask} from {@link Properties}.
	 */
	public TestingTask(Properties properties) {
		super(properties);
	}

	@Override
	public boolean isNodeLocal() {
		return true;
	}

}
