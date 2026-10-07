/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.providers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.ComponentTestUtils;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;
import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.base.services.simpleajax.RequestLockFactory;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.ScriptAbort;
import com.top_logic.model.search.providers.DropSecurity;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.SecurityObjectProviderManager;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;
import com.top_logic.tool.execution.service.CommandApprovalService;
import com.top_logic.util.error.TopLogicException;

/**
 * Base class for tests of drop operations secured by {@link DropSecurity}.
 *
 * <p>
 * The drop operations are configured in a test component that is read from the layout file
 * {@code <TestClass>_layout.xml} next to the concrete test class. The layout must define a security
 * layout with name {@code sl} with the test component as single child, which checks the security on
 * its model. The role {@link #ROLE} grants the {@link SimpleBoundCommandGroup#WRITE write} group in
 * that component. The user {@link #_user} has this role on {@link #_allowed} and {@link #_vetoed},
 * but not on {@link #_denied}.
 * </p>
 *
 * <p>
 * Drop scripts of the tests throw a {@link ScriptAbort}, when they are executed, see
 * {@link #assertScriptExecuted(Runnable)} and {@link #assertRefused(Runnable)}.
 * </p>
 *
 * @param <C>
 *        Type of the test component.
 */
@SuppressWarnings("javadoc")
public abstract class AbstractDropSecurityTest<C extends BoundComponent> extends AbstractSearchExpressionTest {

	/** Role that grants the write group in the test component. */
	protected static final String ROLE = "DropSecurityTest.Writer";

	/** Name passed to {@link #newTarget(String)} for {@link #_vetoed}. */
	protected static final String VETOED_NAME = "vetoed";

	private KnowledgeBase _kb;

	private final Map<ObjectKey, KnowledgeItem> _toDelete = new HashMap<>();

	private final UpdateListener _creationListener = new UpdateListener() {
		@Override
		public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
			_toDelete.putAll(event.getCreatedObjects());
			_toDelete.keySet().removeAll(event.getDeletedObjectKeys());
		}
	};

	/** The acting user. */
	protected Person _user;

	/** Object on which {@link #_user} has the role {@link #ROLE}. */
	protected TLObject _allowed;

	/** Object on which {@link #_user} has no role. */
	protected TLObject _denied;

	/**
	 * Object on which {@link #_user} has the role {@link #ROLE}, created with name
	 * {@link #VETOED_NAME}.
	 */
	protected TLObject _vetoed;

	/** The test component defining the drop operations. */
	protected C _component;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		SecurityComponentCache.disableCache();

		_kb = PersistencyLayer.getKnowledgeBase();
		_kb.addUpdateListener(_creationListener);

		File layout = ComponentTestUtils.getLayoutFile(getClass(), "layout.xml");
		ComponentName securityLayoutName = ComponentTestUtils.newComponentName(layout, "sl");

		try (Transaction tx = _kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			PersBoundComp.createInstance(_kb, securityLayoutName);

			_user = TestPerson.createPerson("dropSecUser");
			_allowed = newTarget("allowed");
			_denied = newTarget("denied");
			_vetoed = newTarget(VETOED_NAME);
			tx.commit();
		}

		LayoutContainer securityLayout = (LayoutContainer) ComponentTestUtils.createComponent(layout);
		@SuppressWarnings("unchecked")
		C component = (C) securityLayout.getChildList().get(0);
		_component = component;
		assertNotNull(_component.getPersBoundComp());

		try (Transaction tx = _kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			BoundedRole role = BoundedRole.createBoundedRole(ROLE);
			_component.getPersBoundComp().addAccess(SimpleBoundCommandGroup.WRITE, role);
			BoundedRole.assignRole(_allowed, _user, role);
			BoundedRole.assignRole(_vetoed, _user, role);
			tx.commit();
		}
		AccessManager.getInstance().reload();

		becomeUser(_user);
	}

	@Override
	protected void tearDown() throws Exception {
		becomeUser(PersonManager.getManager().getRoot());
		_kb.removeUpdateListener(_creationListener);
		try (Transaction tx = _kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			KBUtils.deleteAllKI(_toDelete.values().iterator());
			tx.commit();
		}
		_toDelete.clear();
		AccessManager.getInstance().reload();
		SecurityComponentCache.setupCache();
		_component = null;
		_kb = null;
		super.tearDown();
	}

	/**
	 * Creates a drop target object with the given name within a running transaction.
	 */
	protected abstract TLObject newTarget(String name);

	/**
	 * A technical tree node with the given business object.
	 */
	protected static TLTreeNode<?> node(Object businessObject) {
		return new DefaultMutableTLTreeModel(businessObject).getRoot();
	}

	/**
	 * Asserts that the given drop executes the drop script.
	 */
	protected static void assertScriptExecuted(Runnable drop) {
		try {
			drop.run();
		} catch (RuntimeException ex) {
			assertTrue("Drop script not executed: " + ex, hasCause(ex, ScriptAbort.class));
			return;
		}
		fail("Drop script not executed.");
	}

	/**
	 * Asserts that the given drop is refused without executing the drop script.
	 */
	protected static void assertRefused(Runnable drop) {
		try {
			drop.run();
		} catch (TopLogicException ex) {
			assertFalse("Drop script executed for refused drop.", hasCause(ex, ScriptAbort.class));
			assertEquals(
				com.top_logic.model.search.providers.I18NConstants.ERROR_DROP_NOT_ALLOWED__REASON.getKey(),
				ex.getErrorKey().getKey());
			return;
		}
		fail("Drop not refused.");
	}

	private static boolean hasCause(Throwable ex, Class<? extends Throwable> type) {
		for (Throwable current = ex; current != null; current = current.getCause()) {
			if (type.isInstance(current)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates the suite for the given test class.
	 *
	 * @param testClass
	 *        The test class.
	 * @param configFile
	 *        Name of the configuration file next to the test class that is loaded additionally
	 *        (the typed configuration is read from the corresponding {@code .config.xml} file).
	 */
	protected static Test suite(Class<? extends AbstractDropSecurityTest<?>> testClass, String configFile) {
		Test kbTest = KBSetup.getSingleKBTest(testClass,
			ServiceTestSetup.createStarterFactoryForModules(getModules(
				AccessManager.Module.INSTANCE,
				TLSecurityDeviceManager.Module.INSTANCE,
				PersonManager.Module.INSTANCE,
				LayoutStorage.Module.INSTANCE,
				SecurityObjectProviderManager.Module.INSTANCE,
				BoundHelper.Module.INSTANCE,
				SecurityComponentCache.Module.INSTANCE,
				RequestLockFactory.Module.INSTANCE,
				CommandHandlerFactory.Module.INSTANCE,
				InitialRolesManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE,
				CommandApprovalService.Module.INSTANCE)));
		String configPath = CustomPropertiesDecorator.createFileName(testClass, configFile);
		return TLTestSetup.createTLTestSetup(TestUtils.doNotMerge(new CustomPropertiesSetup(kbTest, configPath, true)));
	}

}
