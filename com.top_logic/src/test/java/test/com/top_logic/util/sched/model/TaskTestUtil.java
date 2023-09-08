/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched.model;

import junit.framework.TestCase;

import test.com.top_logic.basic.ReflectionUtils;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.composite.CompositeTask;
import com.top_logic.util.sched.task.composite.CompositeTaskImpl;
import com.top_logic.util.sched.task.impl.TaskImpl;
import com.top_logic.util.sched.task.log.TransientTaskLog;
import com.top_logic.util.sched.task.schedule.AlwaysSchedule;
import com.top_logic.util.sched.task.schedule.SchedulingAlgorithm;

/**
 * Utils for {@link TestCase}s that need {@link Task}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TaskTestUtil {

	/**
	 * Initialize the field {@link TaskImpl#_log} per reflection for tests that don't need a
	 * {@link Scheduler}.
	 * <p>
	 * The correct solution would be a mock {@link Scheduler}, but that is not yet possible, as
	 * there is no {@link Scheduler} interface, yet.
	 * </p>
	 */
	/* The raw-type "T extends Task" instead of "T extends Task" is a workaround for Eclipse 4.8. If
	 * it is correctly typed, Eclipse 4.8 shows a compiler error on every call of this method. */
	@SuppressWarnings("rawtypes")
	public static <T extends Task> T initTaskLog(T task) {
		if (!(task instanceof TaskImpl)) {
			return task;
		}
		initTaskLogInternal((TaskImpl<?>) task);

		if (task instanceof CompositeTask) {
			CompositeTask compositeTask = (CompositeTask) task;
			for (Task child : compositeTask.getChildren()) {
				initTaskLog(child);
			}
		}
		return task;
	}

	private static void initTaskLogInternal(TaskImpl<?> task) {
		ReflectionUtils.setValue(task, "_log", new TransientTaskLog(task));
	}

	public static TaskImpl<?> createTaskImpl(String taskName, Class<? extends TaskImpl> implementationClass,
			PolymorphicConfiguration<? extends SchedulingAlgorithm> schedule) {

		TaskImpl.Config<?> config = createTaskImplConfig(taskName, implementationClass);
		setSchedulingAlgorithm(config, schedule);
		return createInstance(config);
	}

	public static TaskImpl.Config<?> createTaskImplConfig(String taskName,
			Class<? extends TaskImpl> implementationClass) {
		TaskImpl.Config<?> config = TypedConfiguration.newConfigItem(TaskImpl.Config.class);
		config.setName(taskName);
		return config;
	}

	public static Task.Config<?> createTaskImplConfig(String taskName) {
		TaskImpl.Config<?> config = TypedConfiguration.newConfigItem(TaskImpl.Config.class);
		config.setName(taskName);
		return config;
	}

	public static CompositeTaskImpl.Config<?> createCompositeTaskImplConfig(String taskName) {
		CompositeTaskImpl.Config<?> config = TypedConfiguration.newConfigItem(CompositeTaskImpl.Config.class);
		config.setName(taskName);
		return config;
	}

	public static void setSchedulingAlgorithm(Task.Config config,
			PolymorphicConfiguration<? extends SchedulingAlgorithm> schedule) {
		if (schedule == null) {
			return;
		}
		config.getSchedules().add(schedule);
	}

	public static <T> T createInstance(PolymorphicConfiguration<T> config) {
		return getInstantiationContext().getInstance(config);
	}

	public static InstantiationContext getInstantiationContext() {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
	}

	public static AlwaysSchedule.Config newAlwaysSchedule() {
		return TypedConfiguration.newConfigItem(AlwaysSchedule.Config.class);
	}

}
