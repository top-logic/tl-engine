/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.dataImport;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.util.sched.TestingScheduler;
import test.com.top_logic.util.sched.model.TaskTestUtil;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tool.dataImport.AbstractDataImporter;
import com.top_logic.tool.dataImport.AutomaticDataImportTask;
import com.top_logic.tool.dataImport.AutomaticDataImportTask.Config;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.log.TaskLog;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * Test for {@link AutomaticDataImportTask}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestAutomaticDataImportTask extends BasicTestCase {

	public static class TestingDataImporter extends AbstractDataImporter {

		@Override
		protected void doPrepareImport() {
			// empty
		}

		@Override
		protected void doParseImport() {
			// empty
		}

		@Override
		protected void doCommitImport() {
			// empty
		}

		@Override
		protected void doCleanUpImporter() {
			// empty
		}

	}

	private TestingScheduler _scheduler;

	@Override
	protected void tearDown() throws Exception {
		if (_scheduler != null) {
			_scheduler.stopAndJoin(20000);
		}
		super.tearDown();
	}

	private Config<?> newTaskConfig(String taskName) {
		Config<?> config = TypedConfiguration.newConfigItem(AutomaticDataImportTask.Config.class);
		config.setName(taskName);
		TaskTestUtil.setSchedulingAlgorithm(config, TaskTestUtil.newAlwaysSchedule());
		return config;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testLogSuccessful() throws ConfigurationException, InterruptedException {
		Config<?> taskConfig = newTaskConfig("testLogSuccessful");
		taskConfig.setImporter((PolymorphicConfiguration) TypedConfiguration
			.createConfigItemForImplementationClass(TestingDataImporter.class));
		Task task = TaskTestUtil.createInstance(taskConfig);
		_scheduler = TestingScheduler.newStartedScheduler("testLogSuccessful");
		_scheduler.addTask(task);
		long stop = System.currentTimeMillis() + 5000;
		ResultType resultType;
		while (true) {
			if (stop < System.currentTimeMillis()) {
				fail("Task did not finished in time.");
			}
			Thread.sleep(_scheduler.getPollingInterval());
			TaskLog log = task.getLog();
			if (log == null) {
				// Not started yet
				continue;
			}
			TaskResult lastResult = log.getLastResult();
			if (lastResult == null) {
				// Not finished yet
				continue;
			}
			resultType = lastResult.getResultType();
			break;
		}
		assertEquals(ResultType.SUCCESS, resultType);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestAutomaticDataImportTask}.
	 */
	public static Test suite() {
		return TestingScheduler.wrapSchedulerDependenciesSetup(TestAutomaticDataImportTask.class);
	}

}

