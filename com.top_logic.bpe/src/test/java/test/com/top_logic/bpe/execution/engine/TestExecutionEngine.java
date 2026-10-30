/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.bpe.execution.engine;

import java.util.Collection;

import junit.framework.Test;

import test.com.top_logic.basic.AssertNoErrorLogListener;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.util.sched.model.TaskTestUtil;

import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.bpe.execution.engine.BPETimeoutTask;
import com.top_logic.bpe.execution.engine.ExecutionEngine;
import com.top_logic.bpe.execution.model.ExecutionState;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.bpe.execution.model.TlBpeExecutionFactory;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;
import com.top_logic.util.sched.task.impl.TaskImpl;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * Test for {@link ExecutionEngine} as a service bound to the lifetime of the knowledge base.
 */
@SuppressWarnings("javadoc")
public class TestExecutionEngine extends BasicTestCase {

	private static final String PROCESS_EXECUTION = "tl.bpe.execution:ProcessExecution";


	public void testActiveExecutionsAfterKnowledgeBaseRestart() throws Exception {
		ExecutionEngine engine = ExecutionEngine.getInstance();

		ProcessExecution running = createExecution(ExecutionState.runningClassifier());
		ProcessExecution finished = createExecution(ExecutionState.finishedClassifier());

		Collection<ProcessExecution> before = engine.getAllExecutions();
		assertTrue(before.contains(running));
		assertFalse(before.contains(finished));

		ModuleUtil.INSTANCE.restart(PersistencyLayer.Module.INSTANCE, null);

		ExecutionEngine restartedEngine = ExecutionEngine.getInstance();
		assertNotSame("The engine must restart with the knowledge base.", engine, restartedEngine);
		assertFalse("The engine of the stopped knowledge base must be shut down.", engine.isStarted());
		assertTrue(restartedEngine.isStarted());

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLObject reloadedRunning = WrapperFactory.getWrapper(kb.resolveObjectKey(running.tId()));
		TLObject reloadedFinished = WrapperFactory.getWrapper(kb.resolveObjectKey(finished.tId()));
		assertNotNull(reloadedRunning);
		assertSame(TLModelUtil.findType(ModelService.getApplicationModel(), PROCESS_EXECUTION),
			reloadedRunning.tType());

		Collection<ProcessExecution> after = restartedEngine.getAllExecutions();
		assertTrue(after.contains(reloadedRunning));
		assertFalse(after.contains(reloadedFinished));
	}

	public void testTimeoutTaskWithActiveEngine() {
		createExecution(ExecutionState.runningClassifier());

		assertEquals(ResultType.SUCCESS, runTimeoutTask());
	}

	public void testTimeoutTaskWithStoppedEngine() throws Exception {
		ExecutionEngine engine = ExecutionEngine.getInstance();
		ProcessExecution running = createExecution(ExecutionState.runningClassifier());

		ModuleUtil.INSTANCE.shutDown(ExecutionEngine.Module.INSTANCE);
		try {
			assertFalse(ExecutionEngine.Module.INSTANCE.isActive());

			AssertNoErrorLogListener listener = new AssertNoErrorLogListener(false);
			try {
				assertEquals(ResultType.SUCCESS, runTimeoutTask());
				listener.assertNoErrorLogged("Timeout task run with a stopped engine must not log an error:");
			} finally {
				listener.deactivate();
			}

			assertTrue("An engine obtained before the shutdown completes its run.",
				engine.getAllExecutions().contains(running));
		} finally {
			ModuleUtil.INSTANCE.startUp(ExecutionEngine.Module.INSTANCE);
		}
	}

	private static ResultType runTimeoutTask() {
		TaskImpl<?> task = TaskTestUtil.initTaskLog(
			TaskTestUtil.createInstance(TaskTestUtil.createTaskImplConfig("BPETimeoutTask", BPETimeoutTask.class)));
		task.run();
		return task.getLog().getLastResult().getResultType();
	}

	private static ProcessExecution createExecution(TLClassifier state) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			ProcessExecution result = TlBpeExecutionFactory.getInstance().createProcessExecution();
			result.setExecutionState(state);
			tx.commit();
			return result;
		}
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(TestExecutionEngine.class, ExecutionEngine.Module.INSTANCE));
	}

}
