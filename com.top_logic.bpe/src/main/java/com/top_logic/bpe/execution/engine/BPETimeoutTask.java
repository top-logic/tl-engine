/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.engine;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.thread.InContext;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.util.TLContext;
import com.top_logic.util.sched.task.impl.TaskImpl;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class BPETimeoutTask extends TaskImpl {

	public BPETimeoutTask(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void run() {
		TLContext.inSystemContext(this.getClass(), new InContext() {
			@Override
			public void inContext() {
				KnowledgeBase theKB = PersistencyLayer.getKnowledgeBase();
				try (Transaction t = theKB.beginTransaction()) {
					ExecutionEngine.getInstance().updateAll();
					t.commit();
				} catch (Exception e) {
					Logger.error(
						"Unable to execute timeout-task.", e,
						BPETimeoutTask.class);
				}
			}
		});

		super.run();
	}
}
