/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched.task.composite.cache;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.util.sched.model.TaskTestUtil;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.Task.Config;
import com.top_logic.util.sched.task.composite.CompositeTask;
import com.top_logic.util.sched.task.composite.CompositeTaskImpl;
import com.top_logic.util.sched.task.composite.cache.CompositeTaskResultCache;
import com.top_logic.util.sched.task.log.TransientTaskLog;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;
import com.top_logic.util.sched.task.result.TransientTaskResult;

/**
 * {@link TestCase} for {@link CompositeTaskResultCache}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestCompositeTaskResultCache extends TestCase {

	@SuppressWarnings("unused")
	public void testNoCompositeTaskNoResults() {
		Task task = createTask(TaskTestUtil.createTaskImplConfig("ParentTask"));
		// Check whether the instantiation throws any exception
		new CompositeTaskResultCache(task);
		assertEquals(0, task.getLog().getResults().size());
	}

	@SuppressWarnings("unused")
	public void testNoChildTaskNoResults() {
		Task task = createTask(TaskTestUtil.createCompositeTaskImplConfig("ParentTask"));
		// Check whether the instantiation throws any exception
		new CompositeTaskResultCache(task);
		assertEquals(0, task.getLog().getResults().size());
	}

	@SuppressWarnings("unused")
	public void testSingleChildTaskNoResults() {
		Task task = createTask(createTaskConfig(1));
		// Check whether the instantiation throws any exception
		new CompositeTaskResultCache(task);
		assertEquals(0, task.getLog().getResults().size());
	}

	@SuppressWarnings("unused")
	public void testThreeChildTasksNoResults() {
		Task task = createTask(createTaskConfig(3));
		// Check whether the instantiation throws any exception
		new CompositeTaskResultCache(task);
		assertEquals(0, task.getLog().getResults().size());
	}

	@SuppressWarnings("unused")
	public void testComplexTaskTreeNoResults() {
		Task task = createComplexExampleTask();
		// Check whether the instantiation throws any exception
		new CompositeTaskResultCache(task);
		assertEquals(0, task.getLog().getResults().size());
	}

	public void testComplexTaskSingleResult() {
		CompositeTask task = createComplexExampleTask();
		addResultRecursively(task, 0);
		CompositeTaskResultCache cache = new CompositeTaskResultCache(task);
		assertEquals(1, task.getLog().getResults().size());
		TaskResult singletonResult = task.getLog().getLastResult();
		assertOneResultPerChild(cache, singletonResult);
	}

	public void testComplexTaskThreeResults() {
		CompositeTask task = createComplexExampleTask();
		long time = 0;
		time = addResultRecursively(task, time);
		time = addResultRecursively(task, time);
		time = addResultRecursively(task, time);
		CompositeTaskResultCache cache = new CompositeTaskResultCache(task);
		assertEquals(3, task.getLog().getResults().size());
		assertOneResultPerChild(cache, task);
	}

	public void testMissingChildResults() {
		CompositeTask task = (CompositeTask) createTask(createTaskConfig(1));
		long time = 0;
		time = addResultRecursively(task, time);
		addResult((TransientTaskLog) task.getLog(), new Date(time + 1), new Date(time + 2));
		time += 3;
		time = addResultRecursively(task, time);
		CompositeTaskResultCache cache = new CompositeTaskResultCache(task);
		assertEquals(3, task.getLog().getResults().size());
		Iterator<? extends TaskResult> resultIterator = task.getLog().getResults().iterator();
		assertOneResultPerChild(cache, resultIterator.next());
		assertTrue(cache.getChildResults(resultIterator.next()).isEmpty());
		assertOneResultPerChild(cache, resultIterator.next());
	}

	public void testOrphanChildResults() {
		CompositeTask task = (CompositeTask) createTask(createTaskConfig(1));
		long time = 0;
		time = addResultRecursively(task, time);
		List<Task> children = task.getChildren();
		Task arbitraryChild = children.get(0);
		addResult((TransientTaskLog) arbitraryChild.getLog(), new Date(time + 1), new Date(time + 2));
		time += 3;
		time = addResultRecursively(task, time);
		CompositeTaskResultCache cache = new CompositeTaskResultCache(task);
		assertEquals(2, task.getLog().getResults().size());
		assertOneResultPerChild(cache, task);
	}

	public void testRunningParent() {
		CompositeTask task = (CompositeTask) createTask(createTaskConfig(1));
		long time = 0;
		addResult((TransientTaskLog) task.getLog(), new Date(time + 1), null);
		time += 3;
		List<Task> children = task.getChildren();
		Task arbitraryChild = children.get(0);
		addResult((TransientTaskLog) arbitraryChild.getLog(), new Date(time + 1), new Date(time + 2));
		CompositeTaskResultCache cache = new CompositeTaskResultCache(task);
		assertEquals(1, task.getLog().getResults().size());
		assertOneResultPerChild(cache, task);
	}

	public void testRunningParentRunningChild() {
		CompositeTask task = (CompositeTask) createTask(createTaskConfig(1));
		long time = 0;
		addResult((TransientTaskLog) task.getLog(), new Date(time + 1), null);
		time += 3;
		List<Task> children = task.getChildren();
		Task arbitraryChild = children.get(0);
		addResult((TransientTaskLog) arbitraryChild.getLog(), new Date(time + 1), null);
		CompositeTaskResultCache cache = new CompositeTaskResultCache(task);
		assertEquals(1, task.getLog().getResults().size());
		assertOneResultPerChild(cache, task);
	}

	private void assertOneResultPerChild(CompositeTaskResultCache cache, Task task) {
		Iterator<? extends TaskResult> iterator = task.getLog().getResults().iterator();
		while (iterator.hasNext()) {
			TaskResult result = iterator.next();
			assertOneResultPerChild(cache, result);
		}
	}

	private void assertOneResultPerChild(CompositeTaskResultCache cache, TaskResult singletonResult) {
		CompositeTask task = (CompositeTask) singletonResult.getTask();
		Collection<TaskResult> childResults = cache.getChildResults(singletonResult);
		Set<Task> tasksOfResults = new HashSet<>();
		for (TaskResult childResult : childResults) {
			tasksOfResults.add(childResult.getTask());
		}
		assertEquals(new HashSet<>(task.getChildren()), tasksOfResults);
	}

	/**
	 * Works only for {@link Task}s with a {@link TransientTaskLog}.
	 */
	private long addResultRecursively(Task task, long startTime) {
		long time = startTime;
		if (task instanceof CompositeTask) {
			CompositeTask compositeTask = (CompositeTask) task;
			for (Task child : compositeTask.getChildren()) {
				time = addResultRecursively(child, time + 1);
			}
		}
		long endTime = time;
		addResult((TransientTaskLog) task.getLog(), new Date(startTime), new Date(endTime));
		return time + 1;
	}

	private void addResult(TransientTaskLog log, Date startTime, Date endTime) {
		/* Ugly but unavoidable without compromising non-test code quality, as faking the time is
		 * necessary here. But that is exactly what the API should prevent by design. */
		try {
			Class<?>[] signature = { TaskResult.class };
			Method methodAddResult = log.getClass().getDeclaredMethod("addResultInternal", signature);
			methodAddResult.setAccessible(true);
			TransientTaskResult taskResult = new TransientTaskResult(
				log.getTask(), ResultType.SUCCESS, ResKey.forTest("ignored"), startTime, endTime);
			methodAddResult.invoke(log, taskResult);
			Field fieldLastResult = log.getClass().getDeclaredField("_lastResult");
			fieldLastResult.setAccessible(true);
			fieldLastResult.set(log, taskResult);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		} catch (InvocationTargetException ex) {
			throw new RuntimeException(ex);
		} catch (NoSuchFieldException ex) {
			throw new RuntimeException(ex);
		}
	}

	private CompositeTask createComplexExampleTask() {
		Config<?> childConfig1 = createTaskConfig(3);
		Config<?> childConfig2 = TaskTestUtil.createTaskImplConfig("ignored");
		Config<?> childConfig3 = createTaskConfig(2);
		Config<?> parentConfig = createTaskConfig(childConfig1, childConfig2, childConfig3);
		return (CompositeTask) createTask(parentConfig);
	}

	private Task.Config<?> createTaskConfig(int children) {
		Config<?>[] childConfigs = new Task.Config[children];
		for (int i = 0; i < children; i++) {
			childConfigs[i] = TaskTestUtil.createTaskImplConfig("ignored");
		}
		return createTaskConfig(childConfigs);
	}

	private Task.Config<?> createTaskConfig(Task.Config<?>... children) {
		CompositeTaskImpl.Config<?> parentConfig = TaskTestUtil.createCompositeTaskImplConfig("ignored");
		parentConfig.getTasks().addAll(Arrays.asList(children));
		return parentConfig;
	}

	private Task createTask(Config<?> config) {
		Task task = TaskTestUtil.createInstance(config);
		return TaskTestUtil.initTaskLog(task);
	}

	public static Test suite() {
		return ServiceTestSetup.createSetup(TestCompositeTaskResultCache.class, TypeIndex.Module.INSTANCE);
	}

}
