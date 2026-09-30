/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.boundsec.wrap;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.TestUtils;

import com.top_logic.base.services.InitialGroupManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * Test case for the default group configured in {@link InitialGroupManager}.
 * 
 * @see Group#isDefaultGroup()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDefaultGroup extends BasicTestCase {

	/**
	 * Name of the group that the configuration of this test declares as default group.
	 */
	private static final String DEFAULT_GROUP_NAME = "testDefaultGroup";

	/**
	 * Name of a group that the configuration of this test creates without declaring it as default
	 * group.
	 */
	private static final String OTHER_GROUP_NAME = "testOtherGroup";

	/**
	 * The group named by the configuration is the default group.
	 */
	public void testConfiguredGroupIsDefaultGroup() {
		Group defaultGroup = Group.getGroupByName(DEFAULT_GROUP_NAME);
		assertNotNull("The configured group is created during startup.", defaultGroup);
		assertTrue(defaultGroup.isDefaultGroup());
		assertEquals(defaultGroup, InitialGroupManager.getInstance().getDefaultGroup());
	}

	/**
	 * A group that the configuration does not name as default group is no default group.
	 */
	public void testOtherGroupIsNoDefaultGroup() {
		Group otherGroup = Group.getGroupByName(OTHER_GROUP_NAME);
		assertNotNull("The configured group is created during startup.", otherGroup);
		assertFalse(otherGroup.isDefaultGroup());
	}

	/**
	 * A group created while the application is running is no default group.
	 */
	public void testCreatedGroupIsNoDefaultGroup() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Transaction tx = kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);
		try {
			Group group = Group.createGroup(getName());
			assertFalse(group.isDefaultGroup());
		} finally {
			tx.rollback();
		}
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		Test test = PersonManagerSetup.createPersonManagerSetup(new TestSuite(TestDefaultGroup.class));
		String customConfig = CustomPropertiesDecorator.createFileName(TestDefaultGroup.class);
		return TLTestSetup.createTLTestSetup(TestUtils.doNotMerge(new CustomPropertiesSetup(test, customConfig, true)));
	}

}
