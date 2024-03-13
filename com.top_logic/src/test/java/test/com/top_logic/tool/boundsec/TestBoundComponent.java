/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.boundsec;

import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.ComponentTestUtils;
import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.CreateDefaultTestPersons;
import test.com.top_logic.mig.html.layout.TestLayoutComponent;

import com.top_logic.base.services.simpleajax.RequestLockFactory;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.SecurityObjectProviderManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;
import com.top_logic.util.TLContext;

/**
 * Test the {@link BoundComponent}.
 * 
 * @author    <a href="mailto:klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 */
public class TestBoundComponent extends BasicTestCase {

    /**
     * Constructor for TestBoundComponent.
     * 
     * @param aName (function-) name of test to execute.
     */
    public TestBoundComponent(String aName) {
        super(aName);
    }

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		SecurityComponentCache.disableCache();
	}

	@Override
	protected void tearDown() throws Exception {
		SecurityComponentCache.setupCache();
		super.tearDown();
	}

    /** 
     * Main and single testcase for now.
     */
    public void testMain() throws Exception {
		File layout = ComponentTestUtils.getLayoutFile(TestBoundComponent.class, "testMain.xml");
		ComponentName componentName = ComponentTestUtils.newComponentName(layout, "SchuhComponent");
        try {
            Person guest = Person.byName("guest_de");

            TLContext context = TLContext.getContext();
            context.setCurrentPerson(guest);
            
			checkPBC(componentName);

			TestedBoundComponent cmp = newTestedBoundComponent(componentName, layout);
            assertNotNull(cmp.getPersBoundComp());
            
			check(cmp, guest, componentName);
        }
        finally { // remove the PersBoundComp eventually created by checkPBC
			PersBoundComp schuView = SecurityComponentCache.getSecurityComponent(componentName);
            if (schuView != null)
                schuView.tDelete();
        }
    }

	private TestedBoundComponent newTestedBoundComponent(ComponentName componentName, File layout)
			throws ConfigurationException, IOException {
		LayoutComponent comp = ComponentTestUtils.createComponent(layout);
		assertTrue(comp instanceof TestedBoundComponent);
		assertEquals(componentName, comp.getName());
		return (TestedBoundComponent) comp;
	}
    
    /**
	 * Check the functions of the given BoundComponent.
	 */
	protected void check(TestedBoundComponent cmp, Person guest, ComponentName componentName) {
		assertEquals(componentName, cmp.getSecurityId());

		cmp.resetAllowCache();
		cmp.setModel(guest);
		assertNotNull("Must fail since PersBoundComp has no roles, yet", cmp.hideReason());

		ThreadContext.pushSuperUser(); // Super allows everything
		assertNull(cmp.hideReason());
		ThreadContext.popSuperUser();

		cmp.setModel(null);
		assertNull(cmp.hideReason()); // null model is allowed by default

		cmp.resetAllowCache();
		cmp.setModel(guest);
		assertNotNull(cmp.hideReason());

		assertSame(guest, cmp.getCurrentObject(SimpleBoundCommandGroup.READ, cmp.getModel()));

		BoundedRole role = BoundedRole.createBoundedRole("Schuhzubinder");

		CommandHandler cmd =
			cmp.getCommandById(TestLayoutComponent.ComponentWithIntrinsicCommand.INTRINSIC_COMMAND_NAME);
		cmp.resetAllowCache();
		assertNotNull(cmp.hideReason());

		// Allow "Schuhzubinders" to VIEW the BoundComponent
		cmp.getPersBoundComp().addAccess(cmd.getCommandGroup(), role);

		// Make guest a "Schuhzubinder" on itself.
		ThreadContext.pushSuperUser(); // Need Super to do this
		BoundedRole.assignRole(guest, guest, role);
		ThreadContext.popSuperUser();

		cmp.resetAllowCache();
		assertNull(cmp.hideReason());

		assertNotNull(cmp.getCommandGroups());

		assertTrue(isEmptyOrNull(cmp.getChildCheckers()));

		assertTrue(cmp.getPersBoundComp().removeAccess(cmd.getCommandGroup(), role));
	}

    /** 
     * Setup the PersBoundComp for the given securityId as needed.
     */
	private void checkPBC(ComponentName name) {
		PersBoundComp pbc = SecurityComponentCache.getSecurityComponent(name);
		if (pbc == null) {
			KnowledgeBase kBase = PersistencyLayer.getKnowledgeBase();
			try (Transaction tx = kBase.beginTransaction()) {
				pbc = PersBoundComp.createInstance(kBase, name);
				tx.commit();
			}
		}
    }

    /**
     * Inner class implementing the Abstract Methods.
     * 
     * @author    <a href="mailto:kha@top-logic.com">kha</a>
     */
	public static class TestedBoundComponent extends BoundComponent {

		/**
		 * Typed configuration interface definition for {@link TestedBoundComponent}.
		 * 
		 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
		 */
		public interface Config extends BoundComponent.Config {

			/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
			Lookup LOOKUP = MethodHandles.lookup();

			@Override
			default void modifyIntrinsicCommands(CommandRegistry registry) {
				BoundComponent.Config.super.modifyIntrinsicCommands(registry);
				registry.registerCommand(
					TestLayoutComponent.ComponentWithIntrinsicCommand.INTRINSIC_COMMAND_NAME);
			}

		}

		/**
		 * Creates a new {@link TestBoundComponent.TestedBoundComponent} from the given
		 * configuration.
		 * 
		 * @param context
		 *        {@link InstantiationContext} to instantiate sub configurations.
		 * @param config
		 *        Configuration for this {@link TestBoundComponent.TestedBoundComponent}.
		 */
		public TestedBoundComponent(InstantiationContext context, Config config) throws ConfigurationException {
			super(context, config);
		}

		@Override
		protected boolean supportsInternalModel(Object object) {
			return object == null || object instanceof Person;
		}

    }   

    /**
     * the suite of tests to execute.
     */
     public static Test suite () {
		return PersonManagerSetup.createPersonManagerSetup(TestBoundComponent.class, new TestFactory() {

			@Override
			public Test createSuite(Class<? extends TestCase> testCase, String suiteName) {
				TestSuite suite = new TestSuite(testCase);
				suite.setName(suiteName);
				Test test = new CreateDefaultTestPersons(suite);
				test = ServiceTestSetup.createSetup(test, LayoutStorage.Module.INSTANCE);
				test = ServiceTestSetup.createSetup(test, SecurityObjectProviderManager.Module.INSTANCE);
				test = ServiceTestSetup.createSetup(test, BoundHelper.Module.INSTANCE);
				test = ServiceTestSetup.createSetup(test, SecurityComponentCache.Module.INSTANCE);
				test = ServiceTestSetup.createSetup(test, RequestLockFactory.Module.INSTANCE);
				test = ServiceTestSetup.createSetup(test, CommandHandlerFactory.Module.INSTANCE);
				return test;
			}

         });
     }

     
}
