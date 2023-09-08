/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.util;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.ThreadContextSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.boundsec.manager.rule.TestElementAccessImporter;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.module.ModuleSystem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.AbstractStartStopListener;

/**
 * Setup for live test-cases in the Element project,
 * 
 * @author    <a href=mailto:tsa@top-logic.com>tsa</a>
 */
public class ElementWebTestSetup {

    public static Test createElementWebTestSetup(Class<? extends Test> testClass) {
	    return createElementWebTestSetup(new TestSuite(testClass));
	}

	public static Test createElementWebTestSetup(Test wrapped) {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				reloadAccessManager(wrapped),
				ModuleSystem.Module.INSTANCE));
	}
    
    private static Test reloadAccessManager(Class<TestElementAccessImporter> test) {
		return reloadAccessManager(new TestSuite(test));
	}

    /**
	 * Required to work around a cyclic dependency {@link AccessManager} <-> {@link KnowledgeBase},
	 * see [88378] and {@link AbstractStartStopListener#initApplication}.
	 */
	private static Test reloadAccessManager(Test test) {
		return new ThreadContextSetup(test) {
			
			@Override
			protected void doSetUp() throws Exception {
				AccessManager.getInstance().reload();
			}
			
			@Override
			protected void doTearDown() throws Exception {
				// nothing to do here
			}
		};
	}

}
