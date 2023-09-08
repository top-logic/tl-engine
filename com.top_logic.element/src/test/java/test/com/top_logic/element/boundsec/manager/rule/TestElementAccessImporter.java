/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager.rule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.boundsec.manager.ElementAccessImporter;
import com.top_logic.element.boundsec.manager.ElementAccessManager;
import com.top_logic.element.boundsec.manager.RoleRulesImporter;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.element.boundsec.manager.rule.RoleProvider;
import com.top_logic.element.boundsec.manager.rule.RoleRule;
import com.top_logic.element.boundsec.manager.rule.config.RoleRulesConfig;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * Test the {@link ElementAccessImporter}.
 *
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class TestElementAccessImporter extends BasicTestCase {

    private static final String ROLE_TEST_ROLE = "testRole";
    private static final String STRUCTURE_PROJECT_ELEMENT = "projElement";
    private static final String META_ELEMENT_PROJECT_ALL = "projElement.All";

	private static final String META_ELEMENT_PROJECT_PART = "projElement.Part";

	private final static String ROLE_RULES_VALID = "/WEB-INF/xml/roleRules/ValidRoleRules.xml";

	private final static String ROLE_RULES_INVALID_META_ELEMENT =
		"/WEB-INF/xml/roleRules/InvalidMetaElementRoleRules.xml";

	private final static String ROLE_RULES_INVALID_PATH_META_ELEMENT =
		"/WEB-INF/xml/roleRules/InvalidPathMetaElementRoleRules.xml";

	private final static String ROLE_RULES_INVALID_PATH_META_ELEMENT_2 =
		"/WEB-INF/xml/roleRules/InvalidPathMetaElement2RoleRules.xml";

	private final static String ROLE_RULES_INVALID_ATTRIBUTE = "/WEB-INF/xml/roleRules/InvalidAttributeRoleRules.xml";

	private final static String ROLE_RULES_INVALID_ROLE = "/WEB-INF/xml/roleRules/InvalidRoleRoleRules.xml";

    /**
     * TODO TSA set up ...
     */
    @Override
	protected void setUp() throws Exception {
        super.setUp();

		TLModule scope = TLModelUtil.findModule(STRUCTURE_PROJECT_ELEMENT);
		BoundedRole role = BoundedRole.createBoundedRole(ROLE_TEST_ROLE);
		role.bind(scope);
        assertTrue(role.getKnowledgeBase().commit());
    }

    /**
     * TODO TSA tear down up ...
     */
    @Override
	protected void tearDown() throws Exception {
        final BoundedRole   theBR = BoundedRole.getRoleByName(ROLE_TEST_ROLE);
        final KnowledgeBase theKB = theBR.getKnowledgeBase();
        theBR.unbind();
        theBR.tDelete();
        theKB.commit();
        super.tearDown();
    }


    public void testInvalidMetaElementRules() throws Exception {
        this.singleProblemTest(
                ROLE_RULES_INVALID_META_ELEMENT,
                "admin.security.import.roleRules.problem.unknownMetaElement/sunknown"
        );
    }

    public void testInvalidRoleRules() throws Exception {
        this.singleProblemTest(
                ROLE_RULES_INVALID_ROLE,
                "admin.security.import.roleRules.problem.unknownRole/sunknown"
        );
    }

    public void testInvalidPathMetaElementRules() throws Exception {
        this.multiProblemTest(
                ROLE_RULES_INVALID_PATH_META_ELEMENT,
                new String[] {
                        "admin.security.import.roleRules.problem.unknownMetaElement/sunknown"
                       // ,"admin.security.import.roleRules.problem.invalidPath/sprojElement.All/stestRole"
                }
        );
    }

    public void testInvalidPathMetaElement2Rules() throws Exception {
        this.singleProblemTest(
                ROLE_RULES_INVALID_PATH_META_ELEMENT_2,
			"admin.security.import.roleRules.problem.illegalMetaElement/sprojElement&#58;ROOT&#58;projElement.Project/sprojElement.All"
        );
    }

    public void testInvalidAttributeRules() throws Exception {
        this.multiProblemTest(
                ROLE_RULES_INVALID_ATTRIBUTE,
                new String[] {
				"admin.security.import.roleRules.problem.unknownAttribute/sprojElement&#58;projElement.ProjectRoot/sMitarbeiter"
                       // ,"admin.security.import.roleRules.problem.invalidPath/sprojElement.All/stestRole"
                }
        );
    }

    /**
     * Compare problems found when parsing anInput with expectedProblems.
     */
    public void multiProblemTest(String anInput, String[] expectedProblems) throws Exception {
		RoleRulesConfig roleRulesConfig = getRoleRulesConfig(anInput);
		RoleRulesImporter importer = RoleRulesImporter.loadRules(elementAccessManager(), roleRulesConfig);
		List<ResKey> theProblems = importer.getProblems();
        assertEquals(Arrays.asList(expectedProblems) + " != " + theProblems, expectedProblems.length, theProblems.size());
        for (int i = 0; i < expectedProblems.length; i++) {
			assertEquals(expectedProblems[i], ResKey.encode(theProblems.get(i)));
        }
    }

    public void singleProblemTest(String anInput, String aProblem) throws Exception {
        this.multiProblemTest(anInput, new String[] { aProblem } );
    }

    public void testVaildRules() throws Exception {
		RoleRulesConfig roleRules = getRoleRulesConfig(ROLE_RULES_VALID);
		RoleRulesImporter importer = RoleRulesImporter.loadRules(elementAccessManager(), roleRules);
		assertTrue(importer.getProblems().isEmpty());

		final Map<Object, Collection<RoleProvider>> theRulesMap = importer.getRules();
		assertEquals(2, theRulesMap.size());

		List<RuleDesc> allRuleDescs = new ArrayList<>();
		allRuleDescs.add(
			(new RuleDesc(META_ELEMENT_PROJECT_ALL, "testRole", true, "roleRule.minimal"))
				.extendPath(new PathElementDesc(META_ELEMENT_PROJECT_ALL, "Verantwortlicher", false))
			);
		allRuleDescs.add(
			(new RuleDesc(META_ELEMENT_PROJECT_ALL, "testRole", true, "roleRule.simple"))
				.extendPath(new PathElementDesc(META_ELEMENT_PROJECT_ALL, "Mitarbeiter", false))
			);
		Collections.sort(allRuleDescs, RoleRuleDescKeyComparator.INSTANCE);
		check(theRulesMap.get(this.getMetaElement(META_ELEMENT_PROJECT_ALL)), allRuleDescs);

		List<RuleDesc> partRuleDescs = new ArrayList<>();
		partRuleDescs.add(
			(new RuleDesc(META_ELEMENT_PROJECT_PART, "testRole", true, "roleRule.forward"))
				.extendPath(new PathElementDesc(META_ELEMENT_PROJECT_ALL, "AbhaengigVon", false))
				.extendPath(new PathElementDesc(META_ELEMENT_PROJECT_ALL, "Mitarbeiter", false))
			);
		partRuleDescs.add(
			(new RuleDesc(META_ELEMENT_PROJECT_PART, "testRole", true, "roleRule.backward"))
				.extendPath(new PathElementDesc(META_ELEMENT_PROJECT_ALL, "AbhaengigVon", true))
				.extendPath(new PathElementDesc(META_ELEMENT_PROJECT_ALL, "Mitarbeiter", false))
			);
		Collections.sort(partRuleDescs, RoleRuleDescKeyComparator.INSTANCE);
		check(theRulesMap.get(this.getMetaElement(META_ELEMENT_PROJECT_PART)), partRuleDescs);
	}

	private RoleRulesConfig getRoleRulesConfig(String resource) throws ConfigurationException, IOException {
		AssertProtocol log = new AssertProtocol(getClass().getName());
		BinaryData file = FileManager.getInstance().getData(resource);
		Map<String, ConfigurationDescriptor> globalDescriptors = Collections.emptyMap();
		ConfigurationItem configItem = ConfigurationReader.readContent(log, globalDescriptors, file);
		assertTrue(configItem instanceof RoleRulesConfig);
		return (RoleRulesConfig) configItem;
	}

	private void check(Collection<RoleProvider> importedRules, List<RuleDesc> ruleDescs) {
		final ArrayList<RoleProvider> theSortedRules = new ArrayList<>(importedRules);
        Collections.sort(theSortedRules, RoleRuleKeyComparator.INSTANCE);

		assertEquals(ruleDescs.size(), importedRules.size());
		for (int i = 0; i < ruleDescs.size(); i++) {
			ruleDescs.get(i).check((RoleRule) theSortedRules.get(i));
        }
	}

    private TLClass getMetaElement(String aName) {
		for (Iterator<TLClass> theIt = MetaElementFactory.getInstance().getAllMetaElements().iterator(); theIt
			.hasNext();) {
			TLClass theME = theIt.next();
            if (theME.getName().equals(aName)) {
                return theME;
            }
        }
        return null;
    }

	private ElementAccessManager elementAccessManager() {
		return (ElementAccessManager) AccessManager.getInstance();
	}

    private static class RuleDesc {

        private String  metaElementName;
        private String  roleName;
        private String  resourceKey;
        private boolean inherit;

		private List<PathElementDesc> path;

        public RuleDesc(String aMetaElementName, String aRoleName, boolean aIsInherit, String aResourceKey) {
            super();
            this.metaElementName = aMetaElementName;
            this.roleName        = aRoleName;
            this.inherit         = aIsInherit;
            this.resourceKey     = aResourceKey;
			this.path = new ArrayList<>();
        }

        public void check(RoleRule aRule) {
            assertEquals(aRule.getMetaElement().getName(), this.metaElementName);
            assertEquals(aRule.getRole().getName(),                   this.roleName);
            assertEquals(aRule.isInherit(),                           this.inherit);
			assertEquals(ResKey.encode(aRule.getResourceKey()), this.resourceKey);
            List thePath = aRule.getPath();
            assertEquals(thePath.size(), this.path.size());
            for (int i = 0; i < thePath.size(); i++) {
				this.path.get(i).check((PathElement) thePath.get(i));
            }
        }

        public RuleDesc extendPath(PathElementDesc aPathElementDesc) {
            this.path.add(aPathElementDesc);
            return this;
        }
    }

    private static class PathElementDesc {
        private String metaElementName;
        private String attributeName;
        private boolean invers;

        public PathElementDesc(String aMetaElementName, String aAttributeName,
                boolean aInvers) {
            super();
            this.metaElementName = aMetaElementName;
            this.attributeName = aAttributeName;
            this.invers = aInvers;
        }

        public void check(PathElement aPathElement) {
            assertEquals(AttributeOperations.getMetaElement(aPathElement.getMetaAttribute()).getName(), this.metaElementName);
            assertEquals(aPathElement.getMetaAttribute().getName(), this.attributeName);
            assertEquals(aPathElement.isInverse(),                  this.invers);
        }
    }

	private static class RoleRuleKeyComparator implements Comparator<RoleProvider> {
        public static final RoleRuleKeyComparator INSTANCE = new RoleRuleKeyComparator();

        @Override
		public int compare(RoleProvider aObject1, RoleProvider aObject2) {
			return ((RoleRule) aObject1).getResourceKey().getKey().compareTo(((RoleRule) aObject2).getResourceKey().getKey());
        }
    }

	private static class RoleRuleDescKeyComparator implements Comparator<RuleDesc> {
        public static final RoleRuleDescKeyComparator INSTANCE = new RoleRuleDescKeyComparator();

        @Override
		public int compare(RuleDesc aObject1, RuleDesc aObject2) {
			return aObject1.resourceKey.compareTo(aObject2.resourceKey);
        }
    }

    /**  Return the suite of tests to perform. */
    public static Test suite() {
        TestSuite suite = new TestSuite(TestElementAccessImporter.class);

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

