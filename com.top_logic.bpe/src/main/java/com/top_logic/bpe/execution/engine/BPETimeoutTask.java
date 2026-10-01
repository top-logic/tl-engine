/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.engine;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.util.sched.task.impl.TaskImpl;

/**
 * Task processing the timer events of the running processes.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class BPETimeoutTask extends TaskImpl {

	public BPETimeoutTask(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void run() {
		runWithResultProtocol(this::processTimeouts);

		super.run();
	}

	private void processTimeouts() {
		KnowledgeBase theKB = PersistencyLayer.getKnowledgeBase();
		try (Transaction t = theKB.beginTransaction(I18NConstants.PROCESSED_TIMER_WORKFLOW_TASKS)) {
			ExecutionEngine.getInstance().updateAll();
			t.commit();
		}
	}

	@Override
	public boolean isNodeLocal() {
		/* Runs every few seconds: A cluster lock would cost several commits per run. Concurrent
		 * runs on different nodes are harmless, since both update the same process data and
		 * the commit of all but one of them fails with a conflict. */
		return true;
	}

}
