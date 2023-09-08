/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic;

import junit.extensions.TestSetup;
import junit.framework.Test;

import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.TestFactoryProxy;
import test.com.top_logic.basic.ThreadContextDecorator;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.KBSetup.KBType;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * {@link TestSetup} that initializes the {@link PersonManager} with a test
 * instance.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PersonManagerSetup {

	private PersonManagerSetup() {
		// just static methods
	}
	
	/**
	 * Creates a Test which can use the {@link PersonManager#getManager()}. The
	 * underlying {@link KnowledgeBase} is the default kb from {@link KBSetup}.
	 * 
	 * @param wrappedTest
	 *        test which wants to use a {@link PersonManager}
	 */
	public static Test createPersonManagerSetup(Test wrappedTest) {
		return KBSetup.getSingleKBTest(createPurePersonManagerSetup(wrappedTest));
	}
	
	/**
	 * Creates a Test which can use the {@link PersonManager#getManager()}. The underlying
	 * {@link KnowledgeBase} is the specified kb in {@link KBSetup}.
	 * 
	 * @param wrappedTest
	 *        test which wants to use a {@link PersonManager}
	 * @param kb
	 *        the underlying {@link KnowledgeBase} {@link KBType type}
	 */
	public static Test createPersonManagerSetup(Test wrappedTest, KBType kb) {
		return KBSetup.getKBTest(createPurePersonManagerSetup(wrappedTest),kb);
	}
	
	/**
	 * Creates Tests for each {@link KnowledgeBase} which can use the
	 * {@link PersonManager#getManager()}
	 * 
	 * @param testClass
	 *        class under test
	 * @param f
	 *        factory to create test
	 */
	public static Test createPersonManagerSetup(Class<? extends Test> testClass, TestFactory f) {
		return KBSetup.getKBTest(testClass, new TestFactoryProxy(f) {
			
			@Override
			public Test createSuite(Class<? extends Test> testCase, String suiteName) {
				return createPurePersonManagerSetup(super.createSuite(testCase, suiteName));
			}
		});
	}

	public static Test createPurePersonManagerSetup(Test innerTest) {
		return ServiceTestSetup.withThreadContext(ServiceTestSetup.createSetup(ThreadContextDecorator.INSTANCE,
			innerTest, PersonManager.Module.INSTANCE));
	}

}