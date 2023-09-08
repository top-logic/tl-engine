/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.journal;

import junit.framework.Test;

import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.TestFactoryProxy;
import test.com.top_logic.basic.ThreadContextSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.basic.module.TestModuleUtil;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.knowledge.journal.JournalManager;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.WebFolderFactory;

/**
 * Setup a test environment for journal tests
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
class JournalTestSetup extends ThreadContextSetup {
	
	private JournalManager storedJournalManager;
    
	JournalTestSetup(Test aTest) {
		super(aTest);
    }
    
    /**
     * Set up empty tables for the  {@link JournalManager}
     */
    @Override
    protected void doSetUp() throws Exception {
    	// store current journalManager to reinstall after test and reset instance
        storedJournalManager = TestModuleUtil.installNewInstance(JournalManager.Module.INSTANCE, TestJournal.createJournalManager());
    }        
    
    @Override
    protected void doTearDown() throws Exception {
    	// reinstall stored JournalManager
    	JournalManager testJournalManager = TestModuleUtil.installNewInstance(JournalManager.Module.INSTANCE, storedJournalManager);

    	if (testJournalManager != null) {
    		testJournalManager.terminate();
    	}
    }
    
	static Test suite(Class<? extends Test> testClass, TestFactory factory) {
		return KBSetup.getKBTest(testClass, new TestFactoryProxy(factory) {
			@Override
			public Test createSuite(Class<? extends Test> testCase, String suiteName) {
				Test innerTest = super.createSuite(testCase, suiteName);
				innerTest = new JournalTestSetup(innerTest);
				innerTest =
					ServiceTestSetup.createSetup(innerTest, JournalManager.Module.INSTANCE,
						PersistencyLayer.Module.INSTANCE, WebFolderFactory.Module.INSTANCE);
				return innerTest;
			}
		});
	}
    
}
