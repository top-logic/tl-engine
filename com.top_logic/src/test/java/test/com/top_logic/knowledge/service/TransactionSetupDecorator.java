/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service;

import test.com.top_logic.LocalTestSetup;
import test.com.top_logic.basic.DecoratedTestSetup;
import test.com.top_logic.basic.TestSetupDecorator;

import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;

/**
 * {@link LocalTestSetup} that wraps its inner {@link DecoratedTestSetup} with a transaction in
 * {@link PersistencyLayer#getDefaultKnowledgeBase()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TransactionSetupDecorator implements TestSetupDecorator {
	
	/**
	 * Singleton {@link TransactionSetupDecorator} instance.
	 */
	public static final TransactionSetupDecorator INSTANCE = new TransactionSetupDecorator();

	private TransactionSetupDecorator() {
		// Singleton constructor.
	}
	
	@Override
	public void setup(SetupAction innerSetup) throws Exception {
		Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction();
		innerSetup.setUpDecorated();
		tx.commit();
	}

	@Override
	public void tearDown(SetupAction innerSetup) throws Exception {
		Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction();
		innerSetup.tearDownDecorated();
		tx.commit();
	}

}
