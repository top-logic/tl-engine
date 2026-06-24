/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager.rule;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.LocationImpl;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.boundsec.manager.I18NConstants;
import com.top_logic.element.boundsec.manager.RoleRulesImporter;
import com.top_logic.element.boundsec.manager.rule.PathNavigation;
import com.top_logic.element.boundsec.manager.rule.config.RoleRulesConfig;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * Test the {@link RoleRulesImporter}.
 *
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class TestRoleRulesImporter extends BasicTestCase {

    private static final String ROLE_TEST_ROLE = "testRole";

	private final static String ROLE_RULES_VALID = "/WEB-INF/xml/roleRules/ValidRoleRules.xml";

	private final static String ROLE_RULES_INVALID_META_ELEMENT =
		"/WEB-INF/xml/roleRules/InvalidMetaElementRoleRules.xml";

	private final static String ROLE_RULES_INVALID_PATH_META_ELEMENT =
		"/WEB-INF/xml/roleRules/InvalidPathMetaElementRoleRules.xml";

	private final static String ROLE_RULES_INVALID_ATTRIBUTE = "/WEB-INF/xml/roleRules/InvalidAttributeRoleRules.xml";

	private final static String ROLE_RULES_INVALID_ROLE = "/WEB-INF/xml/roleRules/InvalidRoleRoleRules.xml";

    @Override
	protected void setUp() throws Exception {
        super.setUp();

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(ResKey.forTest("Create test role"))) {
			BoundedRole.createBoundedRole(ROLE_TEST_ROLE);
			tx.commit();
		}
    }

    @Override
	protected void tearDown() throws Exception {
		final BoundedRole testRole = BoundedRole.getRoleByName(ROLE_TEST_ROLE);
		try (Transaction tx = testRole.getKnowledgeBase().beginTransaction(ResKey.forTest("Delete test role"))) {
			testRole.tDelete();
			tx.commit();
		}
        super.tearDown();
    }

    public void testInvalidMetaElementRules() throws Exception {
		this.multiProblemTest(
                ROLE_RULES_INVALID_META_ELEMENT,
			I18NConstants.UNKNOWN_META_ELEMENT.fill("unknown")
        );
    }

    public void testInvalidRoleRules() throws Exception {
		this.multiProblemTest(
                ROLE_RULES_INVALID_ROLE,
			I18NConstants.ROLE_RULES_PROBLEM_UNKNOWN_ROLE.fill("unknown",
				BoundedRole.getAll()
					.stream()
					.map(x -> x.getName())
					.collect(Collectors.joining(", ")))
        );
    }

    public void testInvalidPathMetaElementRules() throws Exception {
		this.multiProblemTest(ROLE_RULES_INVALID_PATH_META_ELEMENT,
			com.top_logic.basic.config.I18NConstants.ERROR_INSTANTIATION_FAILED__CLASS_LOCATION
				.fill(PathNavigation.class.getName(),
					LocationImpl.location("file://" + ROLE_RULES_INVALID_PATH_META_ELEMENT, 14, 44))
        );
    }

    public void testInvalidAttributeRules() throws Exception {
		this.multiProblemTest(ROLE_RULES_INVALID_ATTRIBUTE,
			com.top_logic.basic.config.I18NConstants.ERROR_INSTANTIATION_FAILED__CLASS_LOCATION
				.fill(PathNavigation.class.getName(),
					LocationImpl.location("file://" + ROLE_RULES_INVALID_ATTRIBUTE, 17, 7)));
	}

    /**
     * Compare problems found when parsing anInput with expectedProblems.
     */
	public void multiProblemTest(String anInput, ResKey... expectedProblems) throws Exception {
		RoleRulesConfig roleRulesConfig = getRoleRulesConfig(anInput);
		RoleRulesImporter importer = RoleRulesImporter.loadRules(roleRulesConfig);
		List<ResKey> theProblems = importer.getProblems();
		assertEquals(Arrays.asList(expectedProblems) + " != " + theProblems, expectedProblems.length,
			theProblems.size());
		for (int i = 0; i < expectedProblems.length; i++) {
			assertEquals(expectedProblems[i], theProblems.get(i));
		}
	}

	public void testVaildRules() throws Exception {
		RoleRulesConfig roleRules = getRoleRulesConfig(ROLE_RULES_VALID);
		RoleRulesImporter importer = RoleRulesImporter.loadRules(roleRules);
		assertTrue(importer.getProblems().isEmpty());

	}

	private RoleRulesConfig getRoleRulesConfig(String resource) throws ConfigurationException, IOException {
		AssertProtocol log = new AssertProtocol(getClass().getName());
		BinaryData file = FileManager.getInstance().getData(resource);
		Map<String, ConfigurationDescriptor> globalDescriptors = Collections.emptyMap();
		ConfigurationItem configItem = ConfigurationReader.readContent(log, globalDescriptors, file);
		assertTrue(configItem instanceof RoleRulesConfig);
		return (RoleRulesConfig) configItem;
	}

	/** Return the suite of tests to perform. */
    public static Test suite() {
        TestSuite suite = new TestSuite(TestRoleRulesImporter.class);

        return ElementWebTestSetup.createElementWebTestSetup(suite);
    }

    /**
     * Main function for direct testing.
     */
    public static void main(String[] args) throws IOException {
        Logger.configureStdout(); // Set to "INFO" to see failed commits
        junit.textui.TestRunner.run (suite ());
    }

}

