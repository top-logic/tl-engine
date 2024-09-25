/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DecoratedTestSetup;
import test.com.top_logic.basic.ReflectionUtils;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.ThreadContextDecorator;
import test.com.top_logic.basic.ThreadContextSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.service.TransactionSetupDecorator;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.element.boundsec.ElementSecurityStorage;
import com.top_logic.element.boundsec.manager.ElementAccessManager;
import com.top_logic.element.boundsec.manager.ElementSecurityUpdateManager;
import com.top_logic.element.boundsec.manager.StorageAccessManager;
import com.top_logic.element.boundsec.manager.rule.config.RoleRulesConfig;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.singleton.ElementSingletonManager;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.element.structured.wrap.StructuredElementWrapper;
import com.top_logic.knowledge.security.SecurityStorage;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.manager.AccessManager.Config;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.TLContext;

/**
 * Test for the {@link ElementAccessManager}.
 *
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
@SuppressWarnings("javadoc")
public class TestElementAccessManager extends BasicTestCase {

    /** Number of time to repeat calls to getRoles for performance testing. */
    private static final int REPEAT_GET_ROLES = 10;

    /**
     * If true, the test asks for roles of deleted objects
     * (no one should have roles on deleted objects).
     */
    private static final boolean ASK_ROLES_FOR_REMOVED_OBJECTS = true;

    /**
     * If true, the test asks for roles of deleted persons
     * (deleted persons should not have any roles).
     */
    private static final boolean ASK_ROLES_FOR_REMOVED_PERSONS = true;

	/** If true, some information is written to System.out during the test. */
    private static boolean DO_SYS_OUT = false;


    /** Set to false when automatic security update works correctly. */
    private boolean RELOAD_SECURITY_STORAGE_AFTER_EACH_COMMIT = false;

	private KnowledgeBase _kb;

	public TestElementAccessManager(String name) {
        super(name);
    }

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_kb = PersistencyLayer.getKnowledgeBase();
	}

	public void testAllowedBySecurityParentClassicAM() throws ConfigurationException, Exception {
		testAllowedBySecurityParent(getInstantiationContext().getInstance(createEmptyConfig()));
	}

	public void testAllowedBySecurityParentElementAM() throws ConfigurationException, Exception {
		testAllowedBySecurityParent(getInstantiationContext().getInstance(createEmptyConfig()));
	}

	public void testAllowedBySecurityParentStorageAM() throws ConfigurationException, Exception {
		testAllowedBySecurityParent(getInstantiationContext().getInstance(createMinimalConfig()));
	}

	private void testAllowedBySecurityParent(AccessManager manager) throws Exception {
		manager.reload();
		RELOAD_SECURITY_STORAGE_AFTER_EACH_COMMIT = false;

		loadRules("/WEB-INF/init/InitialTestMERoleRules.xml");
		reloadStorage();

		Person personB = null;
		Person personC = null;
		StructuredElementWrapper projectOne = null;
		StructuredElementWrapper subProjectOne = null;
		try {
			Transaction tx = begin();
			personB = initPerson("BB");
			personC = initPerson("CC");
			commit(tx);
			tx = begin();

			StructuredElement projectRoot =
				projectRoot();

			projectOne = (StructuredElementWrapper) projectRoot.createChild("pr1", "Project");
			subProjectOne = (StructuredElementWrapper) projectOne.createChild("spr1", "Subproject");

			// inherits role from project
			SecurityChild pr1Child = SecurityChild.newSecurityChild("pr1Child", projectOne);
			SecurityChild spr1Child = SecurityChild.newSecurityChild("spr1Child", subProjectOne);

			BoundedRole memberRole = BoundedRole.getRoleByName("projElement.Mitarbeiter");
			BoundedRole responsibleRole = BoundedRole.getRoleByName("projElement.Verantwortlicher");

			BoundedRole.assignRole(subProjectOne, personC, responsibleRole);
			BoundedRole.assignRole(projectOne, personB, memberRole);
			BoundedRole.assignRole(subProjectOne, personB, memberRole);

			commit(tx);

			Collection<?> personBMember =
				manager.getAllowedBusinessObjects(personB, list(memberRole), list(pr1Child, spr1Child));
			assertEquals(set(pr1Child, spr1Child), toSet(personBMember));
			Collection<?> personCResponsible =
				manager.getAllowedBusinessObjects(personC, list(responsibleRole), list(pr1Child, spr1Child));
			assertEquals(set(spr1Child), toSet(personCResponsible));
		} finally {
			cleanUp(new AbstractWrapper[] { projectOne, subProjectOne }, new Person[] { personB, personC });
		}
    }

	public void testPerformanceClassicAM() throws ConfigurationException, Exception {
		testPerformance(getInstantiationContext().getInstance(createEmptyConfig()));
	}

	public void testPerformanceElementAM() throws ConfigurationException, Exception {
		testPerformance(getInstantiationContext().getInstance(createEmptyConfig()));
	}

	public void testPerformanceStorageAM() throws ConfigurationException, Exception {
		testPerformance(getInstantiationContext().getInstance(createMinimalConfig()));
	}

	public void testPerformance(AccessManager manager) throws Exception {
        RELOAD_SECURITY_STORAGE_AFTER_EACH_COMMIT = false;

		loadRules("/WEB-INF/init/InitialTestMERoleRules.xml");
        reloadStorage();

        BoundedRole theResponsibleRole = BoundedRole.getRoleByName("projElement.Verantwortlicher");
        BoundedRole theMemberRole      = BoundedRole.getRoleByName("projElement.Mitarbeiter");
        Person personA = null;
        Person personB = null;
        Person personC = null;
        Person personD = null;
        StructuredElementWrapper projectOne = null;
        StructuredElementWrapper subProjectOne = null;
        try {
			Transaction tx = begin();
            personA = initPerson("AA");
            personB = initPerson("BB");
            personC = initPerson("CC");
            personD = initPerson("DD");

			StructuredElement theRoot = projectRoot();

            projectOne    = (StructuredElementWrapper) theRoot   .createChild("p1",  "Project");
            subProjectOne = (StructuredElementWrapper) projectOne.createChild("sp1", "Subproject");

            subProjectOne.setValue("AbhaengigVon",     Collections.singleton(projectOne));
            projectOne   .setValue("Verantwortlicher", personA);
            subProjectOne.setValue("Verantwortlicher", personC);
            projectOne   .setValue("Mitarbeiter",      Collections.singleton(personB));
            subProjectOne.setValue("Mitarbeiter",      Collections.singleton(personD));

			commit(tx);

            // Test performance

            startTime();

			manager.reload();
            for (int i=0; i<REPEAT_GET_ROLES; i++) {
				manager.getRoles(personA, projectOne);
				manager.getRoles(personB, projectOne);
				manager.getRoles(personC, projectOne);
				manager.getRoles(personD, projectOne);
				manager.getRoles(personA, subProjectOne);
				manager.getRoles(personB, subProjectOne);
				manager.getRoles(personC, subProjectOne);
				manager.getRoles(personD, subProjectOne);
            }
			logTime("" + REPEAT_GET_ROLES + " getRoles in manager " + manager);

            sysOut("");

			Collection<BoundedRole> theRoles = Arrays.asList(new BoundedRole[] { theMemberRole, theResponsibleRole });

            for (int i=0; i<REPEAT_GET_ROLES; i++) {
				manager.hasRole(personA, projectOne, theRoles);
				manager.hasRole(personB, projectOne, theRoles);
				manager.hasRole(personC, projectOne, theRoles);
				manager.hasRole(personD, projectOne, theRoles);
				manager.hasRole(personA, subProjectOne, theRoles);
				manager.hasRole(personB, subProjectOne, theRoles);
				manager.hasRole(personC, subProjectOne, theRoles);
				manager.hasRole(personD, subProjectOne, theRoles);
				assertFalse(manager.hasRole(personD, subProjectOne, Collections.<BoundedRole> emptySet()));
				assertFalse(manager.hasRole(personD, subProjectOne, null));

            }
			logTime("" + REPEAT_GET_ROLES + " hasRoles in manager " + manager);

            sysOut("\n");

        }
        finally {
            cleanUp(new AbstractWrapper[] {projectOne, subProjectOne},
				new Person[] { personA, personB, personC, personD });
        }

    }

	private StructuredElement projectRoot() {
		return rootForStructure("projElement");
	}

	private StructuredElement rootForStructure(String structure) {
		return ((StructuredElementFactory) DynamicModelService.getFactoryFor(structure)).getRoot();
	}

	private Config createMinimalConfig() {
		ElementSecurityStorage.Config storage = newConfig(ElementSecurityStorage.Config.class);
		ElementSecurityUpdateManager.Config updateManager = newConfig(ElementSecurityUpdateManager.Config.class);
		StorageAccessManager.Config accessManager = newConfig(StorageAccessManager.Config.class);
		setProperty(accessManager, StorageAccessManager.Config.STORAGE, storage);
		setProperty(accessManager, StorageAccessManager.Config.UPDATE_MANAGER, updateManager);
		return accessManager;
	}

	private void setProperty(StorageAccessManager.Config config, String propertyName, Object value) {
		config.update(config.descriptor().getProperty(propertyName), value);
	}

	private AccessManager.Config createEmptyConfig() {
		return newConfig(AccessManager.Config.class);
	}

	private <T extends ConfigurationItem> T newConfig(Class<T> config) {
		return TypedConfiguration.newConfigItem(config);
	}

	private InstantiationContext getInstantiationContext() {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
	}

    public void testSimple() throws Exception {
        RELOAD_SECURITY_STORAGE_AFTER_EACH_COMMIT = false;

        AccessManager theAM = AccessManager.getInstance();
		loadRules("/WEB-INF/init/InitialTestMERoleRules.xml");
        reloadStorage();

        Person personA = null;
        Person personB = null;
        Person personC = null;
        Person personD = null;
        StructuredElementWrapper projectOne = null;
        StructuredElementWrapper subProjectOne = null;
        try {
			Transaction tx1 = begin();
            personA = initPerson("AA");
            personB = initPerson("BB");
            personC = initPerson("CC");
            personD = initPerson("DD");

			StructuredElement theRoot = projectRoot();

            projectOne    = (StructuredElementWrapper) theRoot   .createChild("pr1",  "Project");
            subProjectOne = (StructuredElementWrapper) projectOne.createChild("spr1", "Subproject");

            subProjectOne.setValue("AbhaengigVon",     Collections.singleton(projectOne));
            projectOne   .setValue("Verantwortlicher", personA);
            subProjectOne.setValue("Verantwortlicher", personC);
            projectOne   .setValue("Mitarbeiter",      Collections.singleton(personB));
            subProjectOne.setValue("Mitarbeiter",      Collections.singleton(personD));

			commit(tx1);

            assertRoles(theAM, personA, projectOne,    new String[] {"projElement.Verantwortlicher", "projElement.TeilprojektVerantwortlicher"});
            assertRoles(theAM, personB, projectOne,    new String[] {"projElement.Mitarbeiter"});
            assertRoles(theAM, personC, projectOne,    new String[] {"projElement.Mitarbeiter", "projElement.TeilprojektVerantwortlicher"});
            assertRoles(theAM, personD, projectOne,    new String[] {});
            assertRoles(theAM, personA, subProjectOne, new String[] {"projElement.Verantwortlicher"});
            assertRoles(theAM, personB, subProjectOne, new String[] {"projElement.Mitarbeiter"});
            assertRoles(theAM, personC, subProjectOne, new String[] {"projElement.Mitarbeiter", "projElement.Verantwortlicher"});
            assertRoles(theAM, personD, subProjectOne, new String[] {"projElement.Mitarbeiter"});

			Transaction tx2 = begin();
            subProjectOne.setValue("AbhaengigVon",     null);
            projectOne   .setValue("Verantwortlicher", null);
            subProjectOne.setValue("Verantwortlicher", personC);
            projectOne   .setValue("Mitarbeiter",      null);
            subProjectOne.setValue("Mitarbeiter",      Collections.singleton(personD));

			commit(tx2);

            assertRoles(theAM, personA, projectOne,    new String[] {});
            assertRoles(theAM, personB, projectOne,    new String[] {});
            assertRoles(theAM, personC, projectOne,    new String[] {"projElement.TeilprojektVerantwortlicher"});
            assertRoles(theAM, personD, projectOne,    new String[] {});
            assertRoles(theAM, personA, subProjectOne, new String[] {});
            assertRoles(theAM, personB, subProjectOne, new String[] {});
            assertRoles(theAM, personC, subProjectOne, new String[] {"projElement.Verantwortlicher"});
            assertRoles(theAM, personD, subProjectOne, new String[] {"projElement.Mitarbeiter"});
        }
        finally {
            cleanUp(new AbstractWrapper[] {projectOne, subProjectOne},
				new Person[] { personA, personB, personC, personD });
        }

    }



    public void testBoundSecurity() throws Exception {
        RELOAD_SECURITY_STORAGE_AFTER_EACH_COMMIT = false;

        Person personA = null;
        Person personB = null;
        Person personC = null;
        BoundedRole allRole  = null;
        BoundedRole someRole = null;
        BoundedRole readOnly = null;
        BoundedRole theResponsibleRole  = BoundedRole.getRoleByName("projElement.Verantwortlicher");

        AccessManager theAM = AccessManager.getInstance();
		loadRules("/WEB-INF/init/InitialTestMERoleRules.xml");
        reloadStorage();

        /** The root bound object */
        StructuredElementWrapper projectOne = null;
        StructuredElementWrapper subProjectOne = null;

        try {
			Transaction tx1 = begin();
            // create three different persons
            personA = initPerson("AA");
            personB = initPerson("BB");
            personC = initPerson("CC");

			commit(tx1);
			TLContext context = TLContext.getContext();
            context.setCurrentPerson(personA);

			StructuredElement theRoot = projectRoot();

			Transaction tx2 = begin();
            // create some bound objects
            projectOne    = (StructuredElementWrapper) theRoot   .createChild("p1",  "Project");
            subProjectOne = (StructuredElementWrapper) projectOne.createChild("sp1", "Subproject");

//            SimpleBoundObject  prjO    = new SimpleBoundObject("Project");
//            SimpleBoundObject  subPrjO = new SimpleBoundObject("SubProject");
//            SimpleBoundObject  taskO   = new SimpleBoundObject("Task");

            // initially, no roles are set
            assertTrue("Expected no initial roles for personA.", theAM.getRoles(personA, projectOne).isEmpty());
            assertTrue("Expected no initial roles for personB.", theAM.getRoles(personB, projectOne).isEmpty());
            assertTrue("Expected no initial roles for personC.", theAM.getRoles(personC, projectOne).isEmpty());

            assertTrue("Expected no initial roles for personA.", theAM.getRoles(personA, subProjectOne).isEmpty());
            assertTrue("Expected no initial roles for personB.", theAM.getRoles(personB, subProjectOne).isEmpty());
            assertTrue("Expected no initial roles for personC.", theAM.getRoles(personC, subProjectOne).isEmpty());


            // create some roles
            allRole  = BoundedRole.createBoundedRole("All");
            someRole = BoundedRole.createBoundedRole("Some");
            readOnly = BoundedRole.createBoundedRole("ReadOnly");
            //special  = BoundedRole.createBoundedRole("Special");

            // Add some person roles to bound objects
            BoundedRole.assignRole(projectOne, personA, allRole);
//            projectOne.addRoleForPerson(personA, special);
            BoundedRole.assignRole(projectOne, personB, someRole);
            BoundedRole.assignRole(projectOne, personC, readOnly);
//            prjO .addRoleForPerson(personB, special);

//            // Build hierarchy of bound objects
//            rootObject.addChild(prjO);
//            prjO.addChild(subPrjO);
//            prjO.addChild(taskO);

			commit(tx2);

            // roles are set after commit
            assertRoles(theAM, personA, projectOne, new String[] {"All"});
            assertRoles(theAM, personB, projectOne, new String[] {"Some"});
            assertRoles(theAM, personC, projectOne, new String[] {"ReadOnly"});

            assertRoles(theAM, personA, subProjectOne, new String[] {});
            assertRoles(theAM, personB, subProjectOne, new String[] {});
            assertRoles(theAM, personC, subProjectOne, new String[] {});


			Transaction tx3 = begin();
            // remove some person roles from bound objects
            BoundedRole.removeRoleForPerson(projectOne, personA, allRole);
			commit(tx3);

            // personA lost roles
            assertRoles(theAM, personA, projectOne, new String[] {});
            assertRoles(theAM, personB, projectOne, new String[] {"Some"});
            assertRoles(theAM, personC, projectOne, new String[] {"ReadOnly"});

            assertRoles(theAM, personA, subProjectOne, new String[] {});
            assertRoles(theAM, personB, subProjectOne, new String[] {});
            assertRoles(theAM, personC, subProjectOne, new String[] {});

            assertFalse("Expected no Verantwortlicher for personA bevor set.", theAM.getRoles(personA, projectOne).contains(theResponsibleRole));

			Transaction tx4 = begin();
            projectOne.setValue("Verantwortlicher", personA);
			commit(tx4);

            assertTrue("Expected Verantwortlicher for personA after set.", theAM.getRoles(personA, projectOne).contains(theResponsibleRole));

			Transaction tx5 = begin();
            projectOne.setValue("Verantwortlicher", null);
			commit(tx5);

            assertFalse("Expected Verantwortlicher for personA after unset.", theAM.getRoles(personA, projectOne).contains(theResponsibleRole));

        }
        finally {
            cleanUp(new AbstractWrapper[] {projectOne, subProjectOne, allRole, someRole, readOnly/*, special*/},
				new Person[] { personA, personB, personC });
        }
    }

	private void createRoles(String[] someRoleName, String aStructureName) {
		Transaction tx = begin();
		TLModule scope = TLModelUtil.findModule(aStructureName);
        for (int theI = 0; theI < someRoleName.length; theI++) {
            String theRoleName = someRoleName[theI];
            BoundedRole       theBR   = BoundedRole.createBoundedRole(aStructureName + "." + theRoleName);
			theBR.bind(scope);
        }
		tx.commit();
    }

	private void deleteRoles(String[] someRoleName) {
		Transaction tx = begin();
        for (int theI = 0; theI < someRoleName.length; theI++) {
            String theRoleName = someRoleName[theI];
            BoundedRole   theBR = BoundedRole.getRoleByName(theRoleName);
            theBR.unbind();
            theBR.tDelete();
        }
		tx.commit();

    }

    public void testSourceMEMO() throws Exception {

        Person        personA = null;
        Person        personB = null;
        Person        personC = null;
        StructuredElementWrapper theProd        = null;
        StructuredElementWrapper theProdVersion = null;
        StructuredElementWrapper theProdType    = null;

        createRoles(new String[] { "testRoleVersionME", "testRoleVersionMO", "testRoleTypeME", "testRoleTypeMO"} , "prodElement");

        BoundedRole testRoleVersionME = BoundedRole.getRoleByName("prodElement.testRoleVersionME");
        BoundedRole testRoleVersionMO = BoundedRole.getRoleByName("prodElement.testRoleVersionMO");
        BoundedRole testRoleTypeME    = BoundedRole.getRoleByName("prodElement.testRoleTypeME");
        BoundedRole testRoleTypeMO    = BoundedRole.getRoleByName("prodElement.testRoleTypeMO");

        try {

            RELOAD_SECURITY_STORAGE_AFTER_EACH_COMMIT = false;

			Transaction tx1 = begin();
            // create three different persons
            personA = initPerson("AAX");
            personB = initPerson("BBX");
            personC = initPerson("CCX");

			commit(tx1);

            AccessManager theAM = AccessManager.getInstance();
			loadRules("/WEB-INF/init/InitialTestMEMORoleRules.xml");
            reloadStorage();
			TLContext context = TLContext.getContext();
            context.setCurrentPerson(personA);

			StructuredElement theRoot = rootForStructure("prodElement");

			Transaction tx2 = begin();
            theProd        = (StructuredElementWrapper) theRoot.createChild("prod",        "Product");
            theProdVersion = (StructuredElementWrapper) theProd.createChild("prodVersion", "ProductVersion");
            theProdType    = (StructuredElementWrapper) theProd.createChild("prodType",    "ProductType");

            assertTrue("Expected no initial roles for personA on product.", theAM.getRoles(personA, theProd)       .isEmpty());
            assertTrue("Expected no initial roles for personB on version.", theAM.getRoles(personA, theProdVersion).isEmpty());
            assertTrue("Expected no initial roles for personC on type.",    theAM.getRoles(personA, theProdType)   .isEmpty());

            theProdVersion.setValue("Verantwortlicher", personA);
			commit(tx2);

            assertRoles(theAM, personA, theProdVersion, new Object[] { testRoleVersionME, testRoleVersionMO, testRoleTypeME, testRoleTypeMO });
            assertRoles(theAM, personA, theProd,        new Object[] { testRoleVersionME, testRoleVersionMO });


			Transaction tx3 = begin();
            theProdVersion.setValue("Verantwortlicher", null);
            theProdType   .setValue("Verantwortlicher", personA);

			commit(tx3);

            assertRoles(theAM, personA, theProdType,    new Object[] { testRoleVersionME, testRoleVersionMO, testRoleTypeME, testRoleTypeMO });
            assertRoles(theAM, personA, theProd,        new Object[] { testRoleTypeME, testRoleTypeMO });


			Transaction tx4 = begin();
            theProdType   .setValue("Verantwortlicher", null);

			commit(tx4);

            assertRoles(theAM, personA, theProd,        new Object[] { });

        } finally {
            deleteRoles(new String[] { testRoleVersionME.getName(), testRoleVersionMO.getName(), testRoleTypeME.getName(), testRoleTypeMO.getName() });
            cleanUp(new AbstractWrapper[] { theProd, theProdVersion, theProdType },
				new Person[] { personA, personB, personC });
        }
    }

    public void testMEStructureA() throws Exception {
        RELOAD_SECURITY_STORAGE_AFTER_EACH_COMMIT = false;
        sysOut("\n\ntestMEStructureA:");
        doTestME();
    }

    public void testMEStructureB() throws Exception {
        RELOAD_SECURITY_STORAGE_AFTER_EACH_COMMIT = true;
        sysOut("\n\ntestMEStructureB:");
        doTestME();
    }

    private void doTestME() throws Exception {
        sysOut("Testing MetaElementRules with following parameters:");
        sysOut("RELOAD_SECURITY_STORAGE_AFTER_EACH_COMMIT = " + RELOAD_SECURITY_STORAGE_AFTER_EACH_COMMIT);
        sysOut("");
		loadRules("/WEB-INF/init/InitialTestMERoleRules.xml");
        doTestMEStructure();
    }

    private void doTestMEStructure() throws Exception {

        // Create Elements:
        AccessManager theAM = AccessManager.getInstance();
        reloadStorage();

        Person personAphrodite = null;
        Person personAthene = null;
        Person personHades = null;
        Person personPoseidon = null;
        Person personZeus = null;

        StructuredElementWrapper theRoot = null;

        StructuredElementWrapper project1 = null;
        StructuredElementWrapper project2 = null;
        StructuredElementWrapper project3 = null;
        StructuredElementWrapper project4 = null;

        StructuredElementWrapper project1_1 = null;
        StructuredElementWrapper project1_2 = null;
        StructuredElementWrapper project1_3 = null;
        StructuredElementWrapper project1_4 = null;
        StructuredElementWrapper project1_5 = null;
        StructuredElementWrapper project1_6 = null;
        StructuredElementWrapper project1_7 = null;
        StructuredElementWrapper project1_8 = null;
        StructuredElementWrapper project1_9 = null;

        StructuredElementWrapper project1_3_1 = null;

        StructuredElementWrapper project2_1 = null;
        StructuredElementWrapper project2_2 = null;
        StructuredElementWrapper project2_3 = null;
        StructuredElementWrapper project2_4 = null;
        StructuredElementWrapper project2_5 = null;

        StructuredElementWrapper workPackage1_1 = null;
        StructuredElementWrapper workPackage1_2 = null;
        StructuredElementWrapper workPackage1_3 = null;

        StructuredElementWrapper workPackage1_3_1 = null;
        StructuredElementWrapper workPackage1_3_2 = null;
        StructuredElementWrapper workPackage1_3_3 = null;


        String chefAttr = "Verantwortlicher";
        String hanselAttr = "Mitarbeiter";
        String nutzerAttr = "Nutzer";
        String hauptnutzerAttr = "Hauptbenutzer";

        String chef                 = "projElement.Verantwortlicher";
        String hansel               = "projElement.Mitarbeiter";
        String teilchef             = "projElement.TeilprojektVerantwortlicher";
        String nutzer               = "projElement.Nutzer";
        String hauptnutzer          = "projElement.Hauptbenutzer";
        String beobachter           = "projElement.Beobachter";
        String projektbeobachter    = "projElement.Projektbeobachter";

        BoundedRole chefRole                = BoundedRole.getRoleByName(chef);
        BoundedRole beobachterRole          = BoundedRole.getRoleByName(beobachter);
        BoundedRole projektbeobachterRole   = BoundedRole.getRoleByName(projektbeobachter);
		Transaction tx1 = begin();
        BoundedRole feuerRole               = BoundedRole.createBoundedRole("Feuer");
        BoundedRole blitzRole               = BoundedRole.createBoundedRole("Blitz");
        BoundedRole wasserRole              = BoundedRole.createBoundedRole("Wasser");
		commit(tx1);
        String[] noRoles = ArrayUtil.EMPTY_STRING_ARRAY;

        try {

            // Create universe:

			Transaction tx2 = begin();
            personAphrodite = initPerson("Aphrodite");
            personAthene = initPerson("Athene");
            personHades = initPerson("Hades");
            personPoseidon = initPerson("Poseidon");
            personZeus = initPerson("Zeus");
            Person[] allPersons = new Person[] {personAphrodite, personAthene, personHades, personPoseidon, personZeus};

			theRoot = (StructuredElementWrapper) projectRoot();
            theRoot.setValue("Name", "Universum");
            project1 = (StructuredElementWrapper)theRoot.createChild("Milchstraße", "Project");
            project2 = (StructuredElementWrapper)theRoot.createChild("Pegasus", "Project");
            project3 = (StructuredElementWrapper)theRoot.createChild("Andromeda", "Project");
            project4 = (StructuredElementWrapper)theRoot.createChild("Centaurus", "Project");

            workPackage1_1 = (StructuredElementWrapper)project1.createChild("Sterne", "Workpackage");
            workPackage1_2 = (StructuredElementWrapper)project1.createChild("Planeten", "Workpackage");
            workPackage1_3 = (StructuredElementWrapper)project1.createChild("Schwarze Löcher", "Workpackage");

            project1_1 = (StructuredElementWrapper)project1.createChild("Merkur", "Subproject");
            project1_2 = (StructuredElementWrapper)project1.createChild("Venus", "Subproject");
            project1_3 = (StructuredElementWrapper)project1.createChild("Erde", "Subproject");
            project1_4 = (StructuredElementWrapper)project1.createChild("Mars", "Subproject");
            project1_5 = (StructuredElementWrapper)project1.createChild("Jupiter", "Subproject");
            project1_6 = (StructuredElementWrapper)project1.createChild("Saturn", "Subproject");
            project1_7 = (StructuredElementWrapper)project1.createChild("Uranos", "Subproject");
            project1_8 = (StructuredElementWrapper)project1.createChild("Neptun", "Subproject");
            project1_9 = (StructuredElementWrapper)project1.createChild("Pluto", "Subproject");

            project1_3_1 = (StructuredElementWrapper)project1_3.createChild("Mond", "Subproject");
            workPackage1_3_1 = (StructuredElementWrapper)project1_3.createChild("Oberfläche", "Workpackage");
            workPackage1_3_2 = (StructuredElementWrapper)project1_3.createChild("Klima", "Workpackage");
            workPackage1_3_3 = (StructuredElementWrapper)project1_3.createChild("Lebewesen", "Workpackage");

            StructuredElementWrapper[] allSubElements = new StructuredElementWrapper[] {
                    workPackage1_1, workPackage1_2, workPackage1_3,
                    project1_1, project1_2, project1_3, project1_4, project1_5,
                    project1_6, project1_7, project1_8, project1_9,
                    project1_3_1, workPackage1_3_1, workPackage1_3_2, workPackage1_3_3
            };
            StructuredElementWrapper[] projects = new StructuredElementWrapper[] {
                    project1, project2, project3, project4
            };
            StructuredElementWrapper[] allProjects = (StructuredElementWrapper[])
                    ArrayUtil.join(projects, allSubElements);
			StructuredElementWrapper[] allProjectsWithRoot = ArrayUtil.addElement(allProjects, theRoot);

			commit(tx2);


            // Assert no roles:
            assertRoles(theAM, allPersons, allProjectsWithRoot, noRoles);


            // Test singleton rule stuff:

			Transaction tx3 = begin();
			TLObject theSecurityRoot =
				ElementSingletonManager
					.getSingleton(ElementSingletonManager.SINGLETON_PREFIX_STRUCTURE_ROOT + "SecurityStructure");
			theSecurityRoot.tUpdateByName("name", "SecurityRoot");
			theSecurityRoot.tUpdateByName(nutzerAttr, Collections.singletonList(personAphrodite));
			theSecurityRoot.tUpdateByName(hauptnutzerAttr,
				CollectionUtil.toList(new Person[] { personAphrodite, personAthene }));
			commit(tx3);

            Person[] otherPersons = new Person[] {personHades, personPoseidon, personZeus};

            assertRoles(theAM, personAphrodite, project1,       new String[] {nutzer, hauptnutzer});
            assertRoles(theAM, personAphrodite, project1_3,     new String[] {nutzer});
            assertRoles(theAM, personAphrodite, project1_3_1,   new String[] {nutzer});
            assertRoles(theAM, personAphrodite, project1_5,     new String[] {nutzer});
            assertRoles(theAM, personAphrodite, project1_8,     new String[] {nutzer});
            assertRoles(theAM, personAphrodite, project1_9,     new String[] {nutzer});
            assertRoles(theAM, personAphrodite, workPackage1_2, new String[] {nutzer});
            assertRoles(theAM, personAphrodite, project2,       new String[] {nutzer, hauptnutzer});
            assertRoles(theAM, personAphrodite, theRoot,        noRoles);

            assertRoles(theAM, personAthene,    project1,       new String[] {hauptnutzer});
            assertRoles(theAM, personAthene,    project1_3,     new String[] {});
            assertRoles(theAM, personAthene,    project1_3_1,   new String[] {});
            assertRoles(theAM, personAthene,    project1_5,     new String[] {});
            assertRoles(theAM, personAthene,    project1_8,     new String[] {});
            assertRoles(theAM, personAthene,    project1_9,     new String[] {});
            assertRoles(theAM, personAthene,    workPackage1_2, new String[] {});
            assertRoles(theAM, personAthene,    project2,       new String[] {hauptnutzer});
            assertRoles(theAM, personAthene,    theRoot,        noRoles);

            assertRoles(theAM, otherPersons,    project1,       noRoles);
            assertRoles(theAM, otherPersons,    project1_3,     noRoles);
            assertRoles(theAM, otherPersons,    project1_3_1,   noRoles);
            assertRoles(theAM, otherPersons,    project1_5,     noRoles);
            assertRoles(theAM, otherPersons,    project1_8,     noRoles);
            assertRoles(theAM, otherPersons,    project1_9,     noRoles);
            assertRoles(theAM, otherPersons,    workPackage1_2, noRoles);
            assertRoles(theAM, otherPersons,    project2,       noRoles);
            assertRoles(theAM, otherPersons,    theRoot,        noRoles);

			Transaction tx4 = begin();
			theSecurityRoot.tUpdateByName(nutzerAttr, null);
			theSecurityRoot.tUpdateByName(hauptnutzerAttr, null);
			commit(tx4);

            // Assert no roles:
            assertRoles(theAM, allPersons, allProjectsWithRoot, noRoles);


            // Test direct role associations:
			Transaction tx5 = begin();
            BoundedRole.assignRole(project1, personAphrodite, chefRole);
            BoundedRole.assignRole(project1, personHades, feuerRole);
            BoundedRole.assignRole(project1_1, personZeus, blitzRole);
			commit(tx5);

            assertRoles(theAM, personHades,     project1,       new String[] {feuerRole.getName()});
            assertRoles(theAM, personHades,     project1_1,     new String[] {});
            assertRoles(theAM, personHades,     project1_3,     new String[] {});
            assertRoles(theAM, personHades,     project1_3_1,   new String[] {});
            assertRoles(theAM, personHades,     project1_4,     new String[] {});
            assertRoles(theAM, personHades,     project2,       new String[] {});
            assertRoles(theAM, personHades,     theRoot,        new String[] {});

            assertRoles(theAM, personZeus,      project1,       new String[] {});
            assertRoles(theAM, personZeus,      project1_1,     new String[] {blitzRole.getName()});
            assertRoles(theAM, personZeus,      project1_3,     new String[] {});
            assertRoles(theAM, personZeus,      project1_3_1,   new String[] {});
            assertRoles(theAM, personZeus,      project1_4,     new String[] {});
            assertRoles(theAM, personZeus,      project2,       new String[] {});
            assertRoles(theAM, personZeus,      theRoot,        new String[] {});

            assertRoles(theAM, personAphrodite, project1,       new String[] {chef, teilchef});
            assertRoles(theAM, personAphrodite, project1_1,     new String[] {chef});
            assertRoles(theAM, personAphrodite, project1_3,     new String[] {chef, teilchef});
            assertRoles(theAM, personAphrodite, project1_3_1,   new String[] {chef});
            assertRoles(theAM, personAphrodite, project1_4,     new String[] {chef});
            assertRoles(theAM, personAphrodite, theRoot,        new String[] {teilchef});

            otherPersons = new Person[] {personAthene, personPoseidon};
            assertRoles(theAM, otherPersons,    allProjectsWithRoot,    noRoles);


			Transaction tx6 = begin();
            // Add more direct roles:
            BoundedRole.assignRole(project1_3, personAphrodite, chefRole);
			commit(tx6);

            assertRoles(theAM, personAphrodite, project1,       new String[] {chef, teilchef});
            assertRoles(theAM, personAphrodite, project1_1,     new String[] {chef});
            assertRoles(theAM, personAphrodite, project1_3,     new String[] {chef, teilchef});
            assertRoles(theAM, personAphrodite, project1_3_1,   new String[] {chef});
            assertRoles(theAM, personAphrodite, project1_4,     new String[] {chef});
            assertRoles(theAM, personAphrodite, theRoot,        new String[] {teilchef});

            assertRoles(theAM, personHades,     project1,       new String[] {feuerRole.getName()});
            assertRoles(theAM, personHades,     project1_1,     new String[] {});
            assertRoles(theAM, personZeus,      project1,       new String[] {});
            assertRoles(theAM, personZeus,      project1_1,     new String[] {blitzRole.getName()});


			Transaction tx7 = begin();
            // Remove direct roles:
            BoundedRole.removeRoleForPerson(project1, personAphrodite, chefRole);
            BoundedRole.removeRoleForPerson(project1_1, personZeus, blitzRole);
			commit(tx7);

            assertRoles(theAM, personAphrodite, project1,       new String[] {teilchef});
            assertRoles(theAM, personAphrodite, project1_1,     new String[] {});
            assertRoles(theAM, personAphrodite, project1_3,     new String[] {chef, teilchef});
            assertRoles(theAM, personAphrodite, project1_3_1,   new String[] {chef});
            assertRoles(theAM, personAphrodite, project1_4,     new String[] {});
            assertRoles(theAM, personAphrodite, project2,       new String[] {});
            assertRoles(theAM, personAphrodite, theRoot,        new String[] {teilchef});

            assertRoles(theAM, personHades,     project1,       new String[] {feuerRole.getName()});
            assertRoles(theAM, personHades,     project1_1,     new String[] {});
            assertRoles(theAM, personZeus,      allProjectsWithRoot,    noRoles);


			Transaction tx8 = begin();
            // Remove all direct roles:
            BoundedRole.removeRoleForPerson(project1, personHades, feuerRole);
            BoundedRole.removeRoleForPerson(project1_3, personAphrodite, chefRole);
			commit(tx8);


            // Assert no roles:
            assertRoles(theAM, allPersons, allProjectsWithRoot, noRoles);


			Transaction tx9 = begin();
            // Test no path element rules:
            BoundedRole.assignRole(theSecurityRoot, personAphrodite, beobachterRole);
            BoundedRole.assignRole(theSecurityRoot, personAthene, wasserRole);
			commit(tx9);

            try {
                assertRoles(theAM, personAphrodite, allSubElements,         new String[] {nutzer});
                assertRoles(theAM, personAphrodite, projects,               noRoles);
                assertRoles(theAM, personAphrodite, theRoot,                noRoles);
                assertRoles(theAM, personAthene,    allProjectsWithRoot,    noRoles);
                assertRoles(theAM, otherPersons,    allProjectsWithRoot,    noRoles);
            }
            finally {
				Transaction tx10 = begin();
                BoundedRole.removeRoleForPerson(theSecurityRoot, personAphrodite, beobachterRole);
                BoundedRole.removeRoleForPerson(theSecurityRoot, personAthene, wasserRole);
				commit(tx10);
            }


            // Assert no roles:
            assertRoles(theAM, allPersons, allProjectsWithRoot, noRoles);


            // Allocate responsibles:

			Transaction tx11 = begin();
            project1.setValue(chefAttr, personZeus);
            project2.setValue(chefAttr, personZeus);
            project3.setValue(chefAttr, personHades);
            project1.setValue(hanselAttr, Collections.singleton(personPoseidon));
            project2.setValue(hanselAttr, Collections.singleton(personPoseidon));
            project3.setValue(hanselAttr, Collections.singleton(personHades));

            project1_1.setValue(chefAttr, personPoseidon);
            project1_2.setValue(chefAttr, personAthene);
            project1_3.setValue(chefAttr, personZeus);
            project1_4.setValue(chefAttr, personAthene);
            project1_5.setValue(chefAttr, personZeus);
            project1_6.setValue(chefAttr, personAthene);
            project1_7.setValue(chefAttr, personPoseidon);
            project1_8.setValue(chefAttr, personPoseidon);
            project1_3.setValue(hanselAttr, CollectionUtil.toList(new Person[] {personAthene, personHades, personPoseidon}));
            project1_3_1.setValue(chefAttr, personHades);

			commit(tx11);


            // Assert direct and indirect roles:

            assertRoles(theAM, personZeus,      project2,       new String[] {chef});
            assertRoles(theAM, personZeus,      project3,       new String[] {});
            assertRoles(theAM, personZeus,      project4,       new String[] {});
            assertRoles(theAM, personPoseidon,  project2,       new String[] {hansel});
            assertRoles(theAM, personHades,     project3,       new String[] {chef, hansel});

            assertRoles(theAM, personAthene,    project1,       new String[] {teilchef});
            assertRoles(theAM, personAphrodite, project1,       new String[] {});
            assertRoles(theAM, personHades,     project1,       new String[] {teilchef});
            assertRoles(theAM, personPoseidon,  project1,       new String[] {hansel, teilchef});
            assertRoles(theAM, personZeus,      project1,       new String[] {chef, teilchef});

            assertRoles(theAM, personPoseidon,  project1_1,     new String[] {chef, hansel});
            assertRoles(theAM, personAthene,    project1_2,     new String[] {chef});
            assertRoles(theAM, personZeus,      project1_3,     new String[] {chef, teilchef});
            assertRoles(theAM, personAthene,    project1_4,     new String[] {chef});
            assertRoles(theAM, personZeus,      project1_5,     new String[] {chef});
            assertRoles(theAM, personAthene,    project1_6,     new String[] {chef});
            assertRoles(theAM, personPoseidon,  project1_7,     new String[] {chef, hansel});
            assertRoles(theAM, personPoseidon,  project1_8,     new String[] {chef, hansel});
            assertRoles(theAM, personZeus,      project1_9,     new String[] {chef});
            assertRoles(theAM, personAthene,    project1_7,     new String[] {});
            assertRoles(theAM, personAthene,    project1_9,     new String[] {});
            assertRoles(theAM, personAphrodite, project1_3,     new String[] {});
            assertRoles(theAM, personAthene,    project1_3,     new String[] {hansel});
            assertRoles(theAM, personHades,     project1_3_1,   new String[] {chef, hansel});
            assertRoles(theAM, personHades,     project1_3,     new String[] {hansel, teilchef});
            assertRoles(theAM, personPoseidon,  project1_3,     new String[] {hansel});
            assertRoles(theAM, personPoseidon,  project1_3_1,   new String[] {hansel});

            assertRoles(theAM, personAphrodite, theRoot,        new String[] {});
            assertRoles(theAM, personAthene,    theRoot,        new String[] {teilchef});
            assertRoles(theAM, personHades,     theRoot,        new String[] {teilchef});
            assertRoles(theAM, personPoseidon,  theRoot,        new String[] {teilchef});
            assertRoles(theAM, personZeus,      theRoot,        new String[] {teilchef});


			Transaction tx12 = begin();
            // Add direct role:
            BoundedRole.assignRole(theSecurityRoot, personZeus, projektbeobachterRole);
			commit(tx12);

            try {

            assertHasRole(theAM, personZeus, allProjects, projektbeobachter);


            // Create more:

				Transaction tx13 = begin();
            project2_1 = (StructuredElementWrapper)project2.createChild("Discordia", "Subproject");
            project2_2 = (StructuredElementWrapper)project2.createChild("Mira", "Subproject");
            project2_3 = (StructuredElementWrapper)project2.createChild("Orion", "Subproject");
            project2_4 = (StructuredElementWrapper)project2.createChild("Rhovid", "Subproject");
            project2_5 = (StructuredElementWrapper)project2.createChild("Ebydos", "Subproject");

            allProjectsWithRoot = (StructuredElementWrapper[]) ArrayUtil.join(allProjectsWithRoot,
                    new StructuredElementWrapper[] {
                    project2_1, project2_2, project2_3, project2_4, project2_5
            });

				commit(tx13);

            assertRoles(theAM, personZeus,      project2,       new String[] {chef, teilchef, projektbeobachter});
            assertRoles(theAM, personZeus,      project2_1,     new String[] {chef, projektbeobachter});
            assertRoles(theAM, personZeus,      project2_2,     new String[] {chef, projektbeobachter});
            assertRoles(theAM, personZeus,      project2_3,     new String[] {chef, projektbeobachter});
            assertRoles(theAM, personZeus,      project2_4,     new String[] {chef, projektbeobachter});
            assertRoles(theAM, personZeus,      project2_5,     new String[] {chef, projektbeobachter});

            assertRoles(theAM, personAthene,    project2,       new String[] {});
            assertRoles(theAM, personAthene,    project2_1,     new String[] {});
            assertRoles(theAM, personZeus,      project2,       new String[] {chef, teilchef, projektbeobachter});
            assertRoles(theAM, personAphrodite, theRoot,        new String[] {});
            assertRoles(theAM, personAthene,    theRoot,        new String[] {teilchef});
            assertRoles(theAM, personHades,     theRoot,        new String[] {teilchef});
            assertRoles(theAM, personPoseidon,  theRoot,        new String[] {teilchef});
            assertRoles(theAM, personZeus,      theRoot,        new String[] {teilchef});


            // Change responsibilities:

				Transaction tx14 = begin();
				project1_3_1.setValue(chefAttr, personAphrodite);
            project1_1.setValue(chefAttr, null);
            project1_7.setValue(chefAttr, personAthene);
            project2.setValue(hanselAttr, CollectionUtil.intoList(null));
            project2_1.setValue(hanselAttr, CollectionUtil.intoList(personAphrodite));
            project2_2.setValue(chefAttr, personAthene);
            project2_3.setValue(chefAttr, personHades);
            project2_4.setValue(hanselAttr, CollectionUtil.intoList(personHades));
            project2_5.setValue(chefAttr, personPoseidon);
            project2_5.setValue(chefAttr, null);

				commit(tx14);

            assertRoles(theAM, personAphrodite, project1,       new String[] {teilchef});
            assertRoles(theAM, personAphrodite, project1_3,     new String[] {teilchef});
            assertRoles(theAM, personAphrodite, project1_3_1,   new String[] {chef});
            assertRoles(theAM, personAphrodite, project2,       new String[] {});
            assertRoles(theAM, personAphrodite, project2_1,     new String[] {hansel});
            assertRoles(theAM, personAphrodite, project2_4,     new String[] {});

            assertRoles(theAM, personPoseidon,  project1,       new String[] {hansel, teilchef});
            assertRoles(theAM, personPoseidon,  project1_1,     new String[] {hansel});
            assertRoles(theAM, personPoseidon,  project1_7,     new String[] {hansel});
            assertRoles(theAM, personPoseidon,  project1_8,     new String[] {chef, hansel});
            assertRoles(theAM, personPoseidon,  project2,       new String[] {});
            assertRoles(theAM, personPoseidon,  project2_4,     new String[] {});
            assertRoles(theAM, personPoseidon,  project2_5,     new String[] {});

            assertRoles(theAM, personHades,     project1,       new String[] {});
            assertRoles(theAM, personHades,     project1_2,     new String[] {});
            assertRoles(theAM, personHades,     project1_3,     new String[] {hansel});
            assertRoles(theAM, personHades,     project1_3_1,   new String[] {hansel});
            assertRoles(theAM, personHades,     project2,       new String[] {teilchef});
            assertRoles(theAM, personHades,     project2_1,     new String[] {});
            assertRoles(theAM, personHades,     project2_2,     new String[] {});
            assertRoles(theAM, personHades,     project2_3,     new String[] {chef});
            assertRoles(theAM, personHades,     project2_4,     new String[] {hansel});

            assertRoles(theAM, personAthene,    project1,       new String[] {teilchef});
            assertRoles(theAM, personAthene,    project1_1,     new String[] {});
            assertRoles(theAM, personAthene,    project1_7,     new String[] {chef});
            assertRoles(theAM, personAthene,    project1_3,     new String[] {hansel});

            assertRoles(theAM, personZeus,      project1,       new String[] {chef, teilchef, projektbeobachter});
            assertRoles(theAM, personZeus,      project1_1,     new String[] {chef, projektbeobachter});
            assertRoles(theAM, personZeus,      project1_3,     new String[] {chef, teilchef, projektbeobachter});
            assertRoles(theAM, personZeus,      project1_3_1,   new String[] {chef, projektbeobachter});
            assertRoles(theAM, personZeus,      project2,       new String[] {chef, teilchef, projektbeobachter});
            assertRoles(theAM, personZeus,      project2_1,     new String[] {chef, projektbeobachter});
            assertRoles(theAM, personZeus,      project2_3,     new String[] {chef, projektbeobachter});
            assertRoles(theAM, personZeus,      project2_5,     new String[] {chef, projektbeobachter});

            assertRoles(theAM, personAphrodite, theRoot,        new String[] {teilchef});
            assertRoles(theAM, personAthene,    theRoot,        new String[] {teilchef});
            assertRoles(theAM, personHades,     theRoot,        new String[] {teilchef});
            assertRoles(theAM, personPoseidon,  theRoot,        new String[] {teilchef});
            assertRoles(theAM, personZeus,      theRoot,        new String[] {teilchef});


            // Change more:

				Transaction tx15 = begin();
            project1_8.setValue(chefAttr, null);
            project1.setValue(hanselAttr, Collections.EMPTY_LIST);
            project1_3.setValue(chefAttr, personAthene);

				commit(tx15);

            assertRoles(theAM, personPoseidon,  project1,       new String[] {});
            assertRoles(theAM, personPoseidon,  project1_1,     new String[] {});
            assertRoles(theAM, personPoseidon,  project1_7,     new String[] {});
            assertRoles(theAM, personPoseidon,  project1_8,     new String[] {});
            assertRoles(theAM, personPoseidon,  project1_3,     new String[] {hansel});
            assertRoles(theAM, personPoseidon,  project1_3_1,   new String[] {hansel});

            assertRoles(theAM, personAthene,    project1,       new String[] {teilchef});
            assertRoles(theAM, personAthene,    project1_3,     new String[] {chef, hansel, teilchef});
            assertRoles(theAM, personAthene,    project1_3_1,   new String[] {chef, hansel});
            assertRoles(theAM, personAphrodite, project1,       new String[] {teilchef});
            assertRoles(theAM, personAphrodite, project1_3,     new String[] {teilchef});
            assertRoles(theAM, personAphrodite, project1_3_1,   new String[] {chef});

            assertRoles(theAM, personAphrodite, theRoot,        new String[] {teilchef});
            assertRoles(theAM, personAthene,    theRoot,        new String[] {teilchef});
            assertRoles(theAM, personHades,     theRoot,        new String[] {teilchef});
            assertRoles(theAM, personPoseidon,  theRoot,        new String[] {});
            assertRoles(theAM, personZeus,      theRoot,        new String[] {teilchef});

            assertRoles(theAM, personPoseidon,  workPackage1_3_1,   new String[] {hansel});
            assertRoles(theAM, personPoseidon,  workPackage1_3_2,   new String[] {hansel});
            assertRoles(theAM, personPoseidon,  workPackage1_3_3,   new String[] {hansel});
            assertRoles(theAM, personHades,     workPackage1_3_1,   new String[] {hansel});
            assertRoles(theAM, personHades,     workPackage1_3_2,   new String[] {hansel});
            assertRoles(theAM, personHades,     workPackage1_3_3,   new String[] {hansel});


            // Change even more:

				Transaction tx16 = begin();
            project1_3.setValue(hanselAttr, null);

				commit(tx16);

            assertRoles(theAM, personPoseidon,  workPackage1_3_1,   new String[] {});
            assertRoles(theAM, personPoseidon,  workPackage1_3_2,   new String[] {});
            assertRoles(theAM, personPoseidon,  workPackage1_3_3,   new String[] {});
            assertRoles(theAM, personHades,     workPackage1_3_1,   new String[] {});
            assertRoles(theAM, personHades,     workPackage1_3_2,   new String[] {});
            assertRoles(theAM, personHades,     workPackage1_3_3,   new String[] {});

				Transaction tx17 = begin();
            workPackage1_3_1.setValue(chefAttr, personPoseidon);
            workPackage1_3_2.setValue(chefAttr, personHades);
            workPackage1_3_3.setValue(hanselAttr, CollectionUtil.intoList(personPoseidon));

				commit(tx17);

            assertRoles(theAM, personPoseidon,  project1,           new String[] {teilchef});
            assertRoles(theAM, personPoseidon,  project1_3,         new String[] {teilchef});
            assertRoles(theAM, personPoseidon,  workPackage1_3_1,   new String[] {chef});
            assertRoles(theAM, personPoseidon,  workPackage1_3_2,   new String[] {});
            assertRoles(theAM, personPoseidon,  workPackage1_3_3,   new String[] {hansel});
            assertRoles(theAM, personHades,     project1,           new String[] {teilchef});
            assertRoles(theAM, personHades,     project1_3,         new String[] {teilchef});
            assertRoles(theAM, personHades,     workPackage1_3_1,   new String[] {});
            assertRoles(theAM, personHades,     workPackage1_3_2,   new String[] {chef});
            assertRoles(theAM, personHades,     workPackage1_3_3,   new String[] {});


            // Destroy worlds:

				Transaction tx18 = begin();
            removeWrapper(project2_3);
            removeWrapper(project2_4);
            removeWrapper(project2_5);

				commit(tx18);

            if (ASK_ROLES_FOR_REMOVED_OBJECTS) {
                assertRoles(theAM, allPersons,      project2_3,     noRoles);
                assertRoles(theAM, allPersons,      project2_4,     noRoles);
                assertRoles(theAM, allPersons,      project2_5,     noRoles);
            }

            assertRoles(theAM, personAthene,    project2,       new String[] {teilchef});
            assertRoles(theAM, personAphrodite, project2,       new String[] {});
            assertRoles(theAM, personHades,     project2,       new String[] {});
            assertRoles(theAM, personPoseidon,  project2,       new String[] {});
            assertRoles(theAM, personZeus,      project2,       new String[] {chef, teilchef, projektbeobachter});

            assertRoles(theAM, personAphrodite, theRoot,        new String[] {teilchef});
            assertRoles(theAM, personAthene,    theRoot,        new String[] {teilchef});
            assertRoles(theAM, personHades,     theRoot,        new String[] {teilchef});
            assertRoles(theAM, personPoseidon,  theRoot,        new String[] {teilchef});
            assertRoles(theAM, personZeus,      theRoot,        new String[] {teilchef});


            // Destroy more:

				Transaction tx19 = begin();
				removeWrapper(project1_1);
            removeWrapper(project1_3);
            removeWrapper(project1_6);
            removeWrapper(workPackage1_3_2);
            removeWrapper(project2_1);
            removeWrapper(project2_2);

				commit(tx19);

            if (ASK_ROLES_FOR_REMOVED_OBJECTS) {
                assertRoles(theAM, allPersons, project1_1,       noRoles);
                assertRoles(theAM, allPersons, project1_3,       noRoles);
					assertRoles(theAM, allPersons, project1_3_1, noRoles);
                assertRoles(theAM, allPersons, project1_6,       noRoles);
                assertRoles(theAM, allPersons, workPackage1_3_2, noRoles);
                assertRoles(theAM, allPersons, project2_1,       noRoles);
                assertRoles(theAM, allPersons, project2_2,       noRoles);
            }

            assertRoles(theAM, personAphrodite, project1,       new String[] {});
            assertRoles(theAM, personAthene,    project1,       new String[] {teilchef});
            assertRoles(theAM, personHades,     project1,       new String[] {});
            assertRoles(theAM, personPoseidon,  project1,       new String[] {});
            assertRoles(theAM, personZeus,      project1,       new String[] {chef, teilchef, projektbeobachter});

            assertRoles(theAM, personAphrodite, project2,       new String[] {});
            assertRoles(theAM, personAthene,    project2,       new String[] {});
            assertRoles(theAM, personHades,     project2,       new String[] {});
            assertRoles(theAM, personPoseidon,  project2,       new String[] {});
            assertRoles(theAM, personZeus,      project2,       new String[] {chef, projektbeobachter});

            assertRoles(theAM, personAphrodite, theRoot,        new String[] {});
            assertRoles(theAM, personAthene,    theRoot,        new String[] {teilchef});
            assertRoles(theAM, personHades,     theRoot,        new String[] {teilchef});
            assertRoles(theAM, personPoseidon,  theRoot,        new String[] {});
            assertRoles(theAM, personZeus,      theRoot,        new String[] {teilchef});

            }
            finally {
				Transaction tx20 = begin();
                // Remove direct role:
                BoundedRole.removeRoleForPerson(theSecurityRoot, personZeus, projektbeobachterRole);
				commit(tx20);
            }

            assertNotHasRole(theAM, personZeus, allProjects, projektbeobachter);


            // Remove persons:

			Transaction tx21 = begin();
            removePerson(personAthene);
            removePerson(personZeus);

			commit(tx21);

            Person[] thePersons = new Person[] {personAthene, personZeus};
            personAthene = personZeus = null;

            if (ASK_ROLES_FOR_REMOVED_PERSONS) {
                assertRoles(theAM, thePersons, allProjectsWithRoot, noRoles);
            }

            assertRoles(theAM, personAphrodite, theRoot,    new String[] {});
            assertRoles(theAM, personHades,     theRoot,    new String[] {teilchef});
            assertRoles(theAM, personPoseidon,  theRoot,    new String[] {});

        }
        finally {
            AbstractWrapper[] theWrapper = new AbstractWrapper[] {
                    project1, project2, project3, project4,
                    workPackage1_1, workPackage1_2, workPackage1_3,
                    project1_1, project1_2, project1_3, project1_4, project1_5,
                    project1_6, project1_7, project1_8, project1_9,
                    project1_3_1, workPackage1_3_1, workPackage1_3_2, workPackage1_3_3,
                    project2_1, project2_2, project2_3, project2_4, project2_5,
                    feuerRole, blitzRole, wasserRole
            };
            cleanUp(theWrapper,
				new Person[] { personAphrodite, personAthene, personHades, personPoseidon, personZeus });
        }

    }

    public void testDirectHasRoleAssociations() throws Exception {
        RELOAD_SECURITY_STORAGE_AFTER_EACH_COMMIT = false;

        // Create Elements:
        AccessManager theAM = AccessManager.getInstance();
        SecurityStorage theSS = getSecurityStorage();
        loadRules(null);
        reloadStorage();

        Person personA = null;
        Person personB = null;
        StructuredElementWrapper root = null;
        Person[] thePersons = new Person[0];

        String chef = "projElement.Verantwortlicher";
        BoundedRole chefRole = BoundedRole.getRoleByName(chef);
        String[] noRoles = ArrayUtil.EMPTY_STRING_ARRAY;
		Object[] vector = null;

        try {

			Transaction tx1 = begin();
            // Create objects:
            personA = initPerson("personA");
            personB = initPerson("personB");
            thePersons = new Person[] {personA, personB};
			root = (StructuredElementWrapper) projectRoot();
			vector = new Object[] { null, root.getID(), chefRole.getID(), SecurityStorage.REASON_HAS_ROLE };

			commit(tx1);

            // Assert no roles:
            assertRoles(theAM, thePersons, root, noRoles);

			Transaction tx2 = begin();
            BoundedRole.assignRole(root, personA, chefRole);
			commit(tx2);

            assertRoles(theAM, personA, root, new String[] {chef});
            assertRoles(theAM, personB, root, noRoles);
            theSS.getGroups(vector);

            theSS.reload();

            assertRoles(theAM, personA, root, new String[] {chef});
            assertRoles(theAM, personB, root, noRoles);
            theSS.getGroups(vector);
        }
        finally {
            if (root != null && personA != null && chefRole != null) try {
					Transaction tx3 = begin();
                BoundedRole.removeRoleForPerson(root, personA, chefRole);
					commit(tx3);
            }
            catch (Exception e) {
                // ignore (hopefully never get at this point...)
            }
			cleanUp(new AbstractWrapper[0], new Person[] { personA, personB });
        }
    }



    public void testFailedCommit() throws Exception {
        RELOAD_SECURITY_STORAGE_AFTER_EACH_COMMIT = false;

        // Create Elements:
        AccessManager theAM = AccessManager.getInstance();
		loadRules("/WEB-INF/init/InitialTestMERoleRules.xml");
        reloadStorage();

        Person personA = null;
        Person personB = null;
        StructuredElementWrapper theRoot = null;
        StructuredElementWrapper project1 = null;
        StructuredElementWrapper project1_1 = null;
        StructuredElementWrapper project1_2 = null;
        Person[] thePersons = new Person[0];
        StructuredElementWrapper[] theProjects = new StructuredElementWrapper[0];

        String chefAttr = "Verantwortlicher";
        String hanselAttr = "Mitarbeiter";
        String chef = "projElement.Verantwortlicher";
        String hansel = "projElement.Mitarbeiter";
        String teilchef = "projElement.TeilprojektVerantwortlicher";
        String[] noRoles = ArrayUtil.EMPTY_STRING_ARRAY;

        try {

			Transaction tx1 = begin();

            // Create objects:
            personA = initPerson("personA");
            personB = initPerson("personB");
			theRoot = (StructuredElementWrapper) projectRoot();
            project1 = (StructuredElementWrapper)theRoot.createChild("Project 1", "Project");
            project1_1 = (StructuredElementWrapper)project1.createChild("Project 1_1", "Subproject");
            project1_2 = (StructuredElementWrapper)project1.createChild("Project 1_2", "Subproject");
            thePersons = new Person[] {personA, personB};
            theProjects = new StructuredElementWrapper[] {theRoot, project1, project1_1, project1_2};

			commit(tx1);

            // Assert no roles:
            assertRoles(theAM, thePersons, theProjects, noRoles);


            // Test normal behavior
            // ====================

            // Change values:
			Transaction tx2 = begin();
            project1.setValue(chefAttr, personA);
            project1.setValue(hanselAttr, CollectionUtil.intoList(personB));
            project1_2.setValue(chefAttr, personB);
			commit(tx2);

            // Assert normal behavior is correct:
            assertRoles(theAM, personA, theRoot,    new String[] {teilchef});
            assertRoles(theAM, personA, project1,   new String[] {chef, teilchef});
            assertRoles(theAM, personA, project1_1, new String[] {chef});
            assertRoles(theAM, personA, project1_2, new String[] {chef});
            assertRoles(theAM, personB, theRoot,    new String[] {teilchef});
            assertRoles(theAM, personB, project1,   new String[] {hansel, teilchef});
            assertRoles(theAM, personB, project1_1, new String[] {hansel});
            assertRoles(theAM, personB, project1_2, new String[] {chef, hansel});

            // Remove values:
			Transaction tx3 = begin();
            project1.setValue(chefAttr, null);
            project1.setValue(hanselAttr, null);
            project1_2.setValue(chefAttr, null);
			commit(tx3);

            // Assert no roles:
            assertRoles(theAM, thePersons, theProjects, noRoles);


            // Test failure behavior
            // =====================

			Transaction tx4 = begin();
            // Change values:
            project1.setValue(chefAttr, personA);
            project1.setValue(hanselAttr, CollectionUtil.intoList(personB));
            project1_2.setValue(chefAttr, personB);

            // Assert that roles aren't set before commit:
            // This works with StorageAccessManager only.
            // ElementAccessManager would return the new roles at this point!
            if (theAM instanceof StorageAccessManager) {
                assertRoles(theAM, thePersons, theProjects, noRoles);
            }
            else if (theAM.getClass().getName().equals(ElementAccessManager.class.getName())) {
                assertRoles(theAM, personA, theRoot,    new String[] {teilchef});
                assertRoles(theAM, personA, project1,   new String[] {chef, teilchef});
                assertRoles(theAM, personA, project1_1, new String[] {chef});
                assertRoles(theAM, personA, project1_2, new String[] {chef});
                assertRoles(theAM, personB, theRoot,    new String[] {teilchef});
                assertRoles(theAM, personB, project1,   new String[] {hansel, teilchef});
                assertRoles(theAM, personB, project1_1, new String[] {hansel});
                assertRoles(theAM, personB, project1_2, new String[] {chef, hansel});
            }

            // Causing the next commit to fail:
            project1_1.setValue("NonExistantAttribute", personA);
			commitFailed(tx4);

            // Assert no roles were given because of failed commit:
            assertRoles(theAM, thePersons, theProjects, noRoles);

            getSecurityStorage().reload();
            assertRoles(theAM, thePersons, theProjects, noRoles);

			Transaction tx5 = begin();
            // Change values:
            project1.setValue(chefAttr, personA);
            project1.setValue(hanselAttr, CollectionUtil.intoList(personB));
            project1_2.setValue(chefAttr, personB);
			commit(tx5);

            // Assert normal behavior is correct:
            assertRoles(theAM, personA, theRoot,    new String[] {teilchef});
            assertRoles(theAM, personA, project1,   new String[] {chef, teilchef});
            assertRoles(theAM, personA, project1_1, new String[] {chef});
            assertRoles(theAM, personA, project1_2, new String[] {chef});
            assertRoles(theAM, personB, theRoot,    new String[] {teilchef});
            assertRoles(theAM, personB, project1,   new String[] {hansel, teilchef});
            assertRoles(theAM, personB, project1_1, new String[] {hansel});
            assertRoles(theAM, personB, project1_2, new String[] {chef, hansel});

			Transaction tx6 = begin();
            // Remove values:
            project1.setValue(chefAttr, null);
            project1.setValue(hanselAttr, null);
            project1_2.setValue(chefAttr, null);

            // Assert that roles aren't removed before commit:
            // This works with StorageAccessManager only.
            // ElementAccessManager would return no roles at this point!
            if (theAM instanceof StorageAccessManager) {
                assertRoles(theAM, personA, theRoot,    new String[] {teilchef});
                assertRoles(theAM, personA, project1,   new String[] {chef, teilchef});
                assertRoles(theAM, personA, project1_1, new String[] {chef});
                assertRoles(theAM, personA, project1_2, new String[] {chef});
                assertRoles(theAM, personB, theRoot,    new String[] {teilchef});
                assertRoles(theAM, personB, project1,   new String[] {hansel, teilchef});
                assertRoles(theAM, personB, project1_1, new String[] {hansel});
                assertRoles(theAM, personB, project1_2, new String[] {chef, hansel});
            }
            else if (theAM.getClass().getName().equals(ElementAccessManager.class.getName())) {
                assertRoles(theAM, thePersons, theProjects, noRoles);
            }

            // Causing the next commit to fail:
            project1_1.setValue("NonExistantAttribute", personA);
			commitFailed(tx6);

            // Assert no roles were removed because of failed commit:
            assertRoles(theAM, personA, theRoot,    new String[] {teilchef});
            assertRoles(theAM, personA, project1,   new String[] {chef, teilchef});
            assertRoles(theAM, personA, project1_1, new String[] {chef});
            assertRoles(theAM, personA, project1_2, new String[] {chef});
            assertRoles(theAM, personB, theRoot,    new String[] {teilchef});
            assertRoles(theAM, personB, project1,   new String[] {hansel, teilchef});
            assertRoles(theAM, personB, project1_1, new String[] {hansel});
            assertRoles(theAM, personB, project1_2, new String[] {chef, hansel});

            getSecurityStorage().reload();
            assertRoles(theAM, personA, theRoot,    new String[] {teilchef});
            assertRoles(theAM, personA, project1,   new String[] {chef, teilchef});
            assertRoles(theAM, personA, project1_1, new String[] {chef});
            assertRoles(theAM, personA, project1_2, new String[] {chef});
            assertRoles(theAM, personB, theRoot,    new String[] {teilchef});
            assertRoles(theAM, personB, project1,   new String[] {hansel, teilchef});
            assertRoles(theAM, personB, project1_1, new String[] {hansel});
            assertRoles(theAM, personB, project1_2, new String[] {chef, hansel});

            // Test empty commit:
			Transaction tx7 = begin();
			commit(tx7);

            // Assert no roles were changed because of empty commit:
            assertRoles(theAM, personA, theRoot,    new String[] {teilchef});
            assertRoles(theAM, personA, project1,   new String[] {chef, teilchef});
            assertRoles(theAM, personA, project1_1, new String[] {chef});
            assertRoles(theAM, personA, project1_2, new String[] {chef});
            assertRoles(theAM, personB, theRoot,    new String[] {teilchef});
            assertRoles(theAM, personB, project1,   new String[] {hansel, teilchef});
            assertRoles(theAM, personB, project1_1, new String[] {hansel});
            assertRoles(theAM, personB, project1_2, new String[] {chef, hansel});
        }
        finally {
            cleanUp(new AbstractWrapper[] {project1, project1_1, project1_2},
				new Person[] { personA, personB });
        }
    }

	private Transaction begin() {
		return _kb.beginTransaction();
	}

	private void commit(Transaction tx) {
		try {
			tx.commit();
		} catch (KnowledgeBaseException ex) {
			throw fail(null, ex);
		}
		if (RELOAD_SECURITY_STORAGE_AFTER_EACH_COMMIT) {
			assertTrue("Failed to reload security storage after commit.", getSecurityStorage().reload());
		}
	}

	private void commitFailed(Transaction tx) {
		try {
			tx.commit();
			fail("Transaction commit successful. It is expected to fail");
		} catch (KnowledgeBaseException ex) {
			// expected failure
		}
	}

	private SecurityStorage reloadStorage() throws Exception {
        SecurityStorage theSS = getSecurityStorage();
        assertTrue("Failed to reload security storage.", theSS.reload());
        assertTrue("Security storage is not empty after reload.", theSS.isEmpty());
        return theSS;
    }


    private void assertRoles(AccessManager anAM, Person aPerson, StructuredElementWrapper aProject, Object[] roles) {
		Collection<BoundRole> theRoles = anAM.getRoles(aPerson, aProject);
		Collection<String> theRoleNames = new ArrayList<>(theRoles.size());
		CollectionUtil.map(theRoles.iterator(), theRoleNames, boundRoleToName());
		Collection<String> theExpectedNames = new ArrayList<>(roles.length);
		CollectionUtil.map(Arrays.asList(roles).iterator(), theExpectedNames, new Mapping<Object, String>() {
            @Override
			public String map(Object anInput) {
                return (anInput instanceof BoundedRole) ? ((BoundedRole) anInput).getName() : anInput.toString();
            }
        });
        assertTrue("Expected "+theExpectedNames+" but found "+theRoleNames+" for person "+(aPerson.tValid() ? aPerson.getName() : "[invalid person]")+" on object "+(aProject.tValid() ? aProject.getName() : "[invalid object]")+".", CollectionUtil.containsSame(theRoleNames, theExpectedNames));
    }

    private void assertRoles(AccessManager anAM, Person[] persons, StructuredElementWrapper aProject, Object[] roles) {
        for (int i = 0; i < persons.length; i++) {
            Person thePerson = persons[i];
            assertRoles(anAM, thePerson, aProject, roles);
        }
    }

    private void assertRoles(AccessManager anAM, Person aPerson, StructuredElementWrapper[] projects, Object[] roles) {
        for (int i = 0; i < projects.length; i++) {
            StructuredElementWrapper theProject = projects[i];
            assertRoles(anAM, aPerson, theProject, roles);
        }
    }

    private void assertRoles(AccessManager anAM, Person[] persons, StructuredElementWrapper[] projects, Object[] roles) {
        for (int i = 0; i < persons.length; i++) {
            Person thePerson = persons[i];
            for (int j = 0; j < projects.length; j++) {
                StructuredElementWrapper theProject = projects[j];
                assertRoles(anAM, thePerson, theProject, roles);
            }
        }
    }

    private void assertHasRole(AccessManager anAM, Person aPerson, StructuredElementWrapper aProject, String aRole) {
		Collection<BoundRole> theRoles = anAM.getRoles(aPerson, aProject);
		Collection<String> theRoleNames = new ArrayList<>(theRoles.size());
		CollectionUtil.map(theRoles.iterator(), theRoleNames, boundRoleToName());
		assertTrue(
			"Expected that person " + aPerson.getName() + " has the role '" + aRole + "' on object "
				+ aProject.getName() + ". But the person has only these roles: " + theRoleNames + ".",
			theRoleNames.contains(aRole));
	}

	private Mapping<BoundRole, String> boundRoleToName() {
		return new Mapping<>() {
            @Override
			public String map(BoundRole aInput) {
				return aInput.getName();
            }
		};
	}

    private void assertHasRole(AccessManager anAM, Person aPerson, StructuredElementWrapper[] projects, String aRole) {
        for (int i = 0; i < projects.length; i++) {
            StructuredElementWrapper theProject = projects[i];
            assertHasRole(anAM, aPerson, theProject, aRole);
        }
    }

    private void assertNotHasRole(AccessManager anAM, Person aPerson, StructuredElementWrapper aProject, String aRole) {
		Collection<BoundRole> theRoles = anAM.getRoles(aPerson, aProject);
		Collection<String> theRoleNames = new ArrayList<>(theRoles.size());
		CollectionUtil.map(theRoles.iterator(), theRoleNames, boundRoleToName());
		assertFalse("Expected that person " + aPerson.getName() + " has NOT the role '" + aRole + "' on object "
			+ aProject + ". But the person has these roles: " + theRoleNames + ".", theRoleNames.contains(aRole));
    }

    private void assertNotHasRole(AccessManager anAM, Person aPerson, StructuredElementWrapper[] projects, String aRole) {
        for (int i = 0; i < projects.length; i++) {
            StructuredElementWrapper theProject = projects[i];
            assertNotHasRole(anAM, aPerson, theProject, aRole);
        }
    }

    private Person initPerson(String aID) {
        String authenticationDeviceID = TLSecurityDeviceManager.getInstance().getDefaultAuthenticationDevice().getDeviceID();
        Person thePerson = Person.byName(aID);
        if (thePerson == null)
			thePerson = Person.create(PersistencyLayer.getKnowledgeBase(), aID, authenticationDeviceID);
        else fail("Person '" + aID + "' exists already.");
        return thePerson;
    }



    /**
     * Remove all parts (User, KnowledgeObject ...) of given Person.
     *
     * @param aPerson
     *            the person to remove
     * @throws DatabaseAccessException
     *             if removing of the person fails
     */
	private void removePerson(Person aPerson) {
        if (aPerson != null && aPerson.tValid()) {
			PersonManager r = PersonManager.getManager();
			aPerson.tDelete();
        }
    }

    /**
     * Remove all parts (KnowledgeObject ...) of given Wrapper.
     *
     * @param aWrapper
     *            the wrapper to remove
     */
    private void removeWrapper(AbstractWrapper aWrapper) {
        if (aWrapper != null && aWrapper.tValid()) {
            aWrapper.tDelete();
        }
    }



    /**
     * Loads the rules in the file with the given filename.
     *
     * @param aFileName
     *            the name of the file with the rules to load; if <code>null</code>, the
     *            RoleRules.xml will be deleted an no rules will be loaded.
     * @throws Exception
     *             if the given file could not be read
     */
    private static void loadRules(String aFileName) throws Exception {
		AccessManager accessManager = AccessManager.getInstance();
		Object amConf = ReflectionUtils.executeMethod(accessManager, "getConfig", new Class[0], new Object[0]);
		RoleRulesConfig currentRoleRuleConf = null;
		if (amConf instanceof ElementAccessManager.Config) {
			ElementAccessManager.Config eamConf = (ElementAccessManager.Config) amConf;
			currentRoleRuleConf = eamConf.getRoleRules();
			RoleRulesConfig roleRules;
			if (!StringServices.isEmpty(aFileName)) {
				BinaryData theFile = FileManager.getInstance().getData(aFileName);
				roleRules = (RoleRulesConfig) ConfigurationReader.readContent(new AssertProtocol(),
					Collections.<String, ConfigurationDescriptor> emptyMap(), theFile);
			} else {
				roleRules = TypedConfiguration.newConfigItem(RoleRulesConfig.class);
			}
			eamConf.setRoleRules(roleRules);
		}
		assertTrue("Failed to load rules in '" + aFileName + "'.", accessManager.reload());
		if (amConf instanceof ElementAccessManager.Config) {
			ElementAccessManager.Config eamConf = (ElementAccessManager.Config) amConf;
			eamConf.setRoleRules(currentRoleRuleConf);
		}
    }



    /**
     * Cleans up the test case and removes the given wrapper and persons.
     *
     * @param wrapper
     *            the wrapper to remove
     * @param persons
     *            the persons to remove
     * @throws Exception
     *             if removing of the person fails
     */
	private void cleanUp(AbstractWrapper[] wrapper, Person[] persons) throws Exception {
        SecurityStorage theSS = getSecurityStorage();
        theSS.setAutoUpdate(false);

		Transaction tx = begin();

//        TLContext.removeContext();
        for (int i = 0; i < wrapper.length; i++) {
            removeWrapper(wrapper[i]);
        }
        for (int i = 0; i < persons.length; i++) {
            removePerson(persons[i]);
        }
		commit(tx);

        theSS.setAutoUpdate(true);

        assertTrue("Failed to reload security storage after cleanup.", theSS.reload());
        assertTrue("Security storage is not empty after cleanup.", theSS.isEmpty());
    }
    
    private static final SecurityStorage getSecurityStorage() {
    	return ((StorageAccessManager) AccessManager.getInstance()).getSecurityStorage();
    }

	/**
	 * Return the suite of Tests to perform.
	 * 
	 * @return the test for this class
	 */
	public static Test suite() {
		return TestUtils.doNotMerge(KBSetup.getKBTest(TestElementAccessManager.class,
			ServiceTestSetup.createStarterFactoryForModules(
				DecoratedTestSetup.join(ThreadContextDecorator.INSTANCE, TransactionSetupDecorator.INSTANCE),

				new TestFactory() {

					@SuppressWarnings("unused")
					@Override
					public Test createSuite(Class<? extends TestCase> testCase, String suiteName) {
						TestSuite suite = new TestSuite(testCase);
						suite.setName(suiteName);
						Test innerTest = suite;
						if (false) {
							innerTest = new TestElementAccessManager("testSimple");
						}
						return new ThreadContextSetup(innerTest) {
							@Override
							protected void doSetUp() throws Exception {
								// Nothing to do.
							}

							@Override
							protected void doTearDown() throws Exception {
								((StorageAccessManager) AccessManager.getInstance()).getSecurityStorage().setAutoUpdate(false);
							}
						};
					}
				}, AccessManager.Module.INSTANCE,
				TLSecurityDeviceManager.Module.INSTANCE,
				PersonManager.Module.INSTANCE)));
	}

    /**
     * Prints a string to System.out if the DO_SYS_OUT flag is set.
     *
     * @param s
     *        the string to print
     */
    public static void sysOut(String s) {
        if (DO_SYS_OUT) {
            System.out.println(s);
        }
    }

    /**
     * Main function for direct testing.
     *
     * @param args command line arguments are ignored
     */
    public static void main(String[] args) {
        Logger.configureStdout();
//         KBSetup.CREATE_TABLES = false; // Speed up debugging
        SHOW_TIME  = true;
        DO_SYS_OUT = true;
        TestRunner.run(suite());
    }

}
