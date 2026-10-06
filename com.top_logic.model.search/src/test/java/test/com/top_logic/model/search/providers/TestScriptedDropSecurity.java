/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.providers;

import java.io.File;
import java.util.HashMap;
import java.util.List;
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
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.model.DynamicModelService;
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
import com.top_logic.layout.table.dnd.BusinessObjectTableDrop;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.ScriptAbort;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.providers.DropSecurity;
import com.top_logic.model.search.providers.TreeDropTargetByExpression;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelUtil;
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
 * Test of the security check of drop targets configured with TL-Script, see {@link DropSecurity}.
 *
 * <p>
 * The drop targets are configured in a test component (see the layout
 * {@code TestScriptedDropSecurity_layout.xml}) that checks the security on its model. The role
 * {@link #ROLE} grants the {@link SimpleBoundCommandGroup#WRITE write} group in that component. The
 * user {@link #_user} has this role on {@link #_allowed} and {@link #_vetoed}, but not on
 * {@link #_denied}. The {@link CommandApprovalService} (see
 * {@code TestScriptedDropSecurity.config.xml}) refuses writing {@link #_vetoed}.
 * </p>
 * 
 * <p>
 * All drop operations throw a {@link ScriptAbort}, when they are executed. This proves that the
 * script is executed for an allowed drop and not executed for a refused one.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestScriptedDropSecurity extends AbstractSearchExpressionTest {

	private static final String CONFIG_FILE = "TestScriptedDropSecurity.xml";

	private static final String ROLE = "TestScriptedDropSecurity.Writer";

	/** Data of the object the {@link CommandApprovalService} refuses to write, see the config. */
	private static final String VETOED_DATA = "vetoed";

	/**
	 * Type of the drop targets.
	 * 
	 * <p>
	 * The type is declared without security, so that TL-Script functions can access its instances
	 * for every user.
	 * </p>
	 */
	private static final String TARGET_TYPE = "UnsecuredData";

	private static final String TARGET_MODULE = "TestTLScriptSecurity";

	private KnowledgeBase _kb;

	private final Map<ObjectKey, KnowledgeItem> _toDelete = new HashMap<>();

	private final UpdateListener _creationListener = new UpdateListener() {
		@Override
		public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
			_toDelete.putAll(event.getCreatedObjects());
			_toDelete.keySet().removeAll(event.getDeletedObjectKeys());
		}
	};

	private Person _user;

	private TLObject _allowed;

	private TLObject _denied;

	private TLObject _vetoed;

	private DropTestComponent _component;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		SecurityComponentCache.disableCache();

		_kb = PersistencyLayer.getKnowledgeBase();
		_kb.addUpdateListener(_creationListener);

		File layout = ComponentTestUtils.getLayoutFile(TestScriptedDropSecurity.class, "layout.xml");
		ComponentName securityLayoutName = ComponentTestUtils.newComponentName(layout, "sl");

		try (Transaction tx = _kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			PersBoundComp.createInstance(_kb, securityLayoutName);

			_user = TestPerson.createPerson("dropSecUser");
			_allowed = newTarget("allowed");
			_denied = newTarget("denied");
			_vetoed = newTarget(VETOED_DATA);
			tx.commit();
		}

		LayoutContainer securityLayout = (LayoutContainer) ComponentTestUtils.createComponent(layout);
		_component = (DropTestComponent) securityLayout.getChildList().get(0);
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

	public void testTableDropAllowed() {
		BusinessObjectTableDrop drop = _component.tableDrop("default");
		List<TLObject> dragged = List.of(_denied);

		assertTrue(drop.canDrop(dragged, _allowed));
		assertScriptExecuted(() -> drop.handleDrop(dragged, _allowed));
	}

	public void testTableDropDeniedWithoutRole() {
		BusinessObjectTableDrop drop = _component.tableDrop("default");
		List<TLObject> dragged = List.of(_allowed);

		assertFalse(drop.canDrop(dragged, _denied));
		assertRefused(() -> drop.handleDrop(dragged, _denied));
	}

	public void testTableDropVetoedByApproval() {
		BusinessObjectTableDrop drop = _component.tableDrop("default");
		List<TLObject> dragged = List.of(_allowed);

		assertFalse(drop.canDrop(dragged, _vetoed));
		assertRefused(() -> drop.handleDrop(dragged, _vetoed));
	}

	/**
	 * Without a reference row, the security is checked on the model of the component.
	 */
	public void testTableDropWithoutReferenceUsesComponentModel() {
		BusinessObjectTableDrop drop = _component.tableDrop("default");
		List<TLObject> dragged = List.of(_allowed);

		_component.setModel(_allowed);
		assertTrue(drop.canDrop(dragged, null));

		_component.setModel(_denied);
		assertFalse(drop.canDrop(dragged, null));
		assertRefused(() -> drop.handleDrop(dragged, null));
	}

	/**
	 * A reference row that is a technical tree node is checked on its business object.
	 */
	public void testTableDropUnwrapsTreeNode() {
		BusinessObjectTableDrop drop = _component.tableDrop("default");
		List<TLObject> dragged = List.of(_allowed);

		assertTrue(drop.canDrop(dragged, node(_allowed)));
		assertFalse(drop.canDrop(dragged, node(_denied)));
	}

	/**
	 * The configured target function computes the security object from the drop arguments, here
	 * the first dragged object.
	 */
	public void testTableDropConfiguredTarget() {
		BusinessObjectTableDrop drop = _component.tableDrop("dragged-target");

		assertTrue(drop.canDrop(List.of(_allowed), _denied));
		assertScriptExecuted(() -> drop.handleDrop(List.of(_allowed), _denied));

		assertFalse(drop.canDrop(List.of(_denied), _allowed));
		assertRefused(() -> drop.handleDrop(List.of(_denied), _allowed));
	}

	/**
	 * A configured target function returning a tree node is checked on the node's business object.
	 */
	public void testTableDropConfiguredTargetUnwrapsTreeNode() {
		BusinessObjectTableDrop drop = _component.tableDrop("dragged-target");

		assertTrue(drop.canDrop(List.of(node(_allowed)), _denied));
		assertFalse(drop.canDrop(List.of(node(_denied)), _allowed));
	}

	/**
	 * A drop configured with another command group requires a role granting that group: the role of
	 * the user grants only {@link SimpleBoundCommandGroup#WRITE}.
	 */
	public void testTableDropConfiguredGroup() {
		BusinessObjectTableDrop drop = _component.tableDrop("delete-group");

		assertFalse(drop.canDrop(List.of(_denied), _allowed));
		assertRefused(() -> drop.handleDrop(List.of(_denied), _allowed));
	}

	/**
	 * The drop onto a tree node is checked on the business object of the node.
	 */
	public void testOntoTreeDrop() {
		TreeDropTargetByExpression drop = _component.treeDrop("onto");
		List<TLObject> dragged = List.of(_denied);

		assertTrue(drop.canDrop(dragged, Args.some(_allowed)));
		assertScriptExecuted(() -> drop.handleDrop(dragged, Args.some(_allowed)));

		assertFalse(drop.canDrop(dragged, Args.some(_denied)));
		assertRefused(() -> drop.handleDrop(dragged, Args.some(_denied)));

		assertFalse(drop.canDrop(dragged, Args.some(_vetoed)));
		assertRefused(() -> drop.handleDrop(dragged, Args.some(_vetoed)));

		// A technical tree node passed as drop position (e.g. by a script replay) is unwrapped.
		assertTrue(drop.canDrop(dragged, Args.some(node(_allowed))));
		assertFalse(drop.canDrop(dragged, Args.some(node(_denied))));
	}

	/**
	 * The ordered drop into a tree is checked on the parent into which the objects are inserted,
	 * not on the sibling before which they are inserted.
	 */
	public void testOrderedTreeDrop() {
		TreeDropTargetByExpression drop = _component.treeDrop("ordered");
		List<TLObject> dragged = List.of(_denied);

		assertTrue(drop.canDrop(dragged, Args.some(_allowed, _denied)));
		assertScriptExecuted(() -> drop.handleDrop(dragged, Args.some(_allowed, _denied)));

		assertFalse(drop.canDrop(dragged, Args.some(_denied, _allowed)));
		assertRefused(() -> drop.handleDrop(dragged, Args.some(_denied, _allowed)));
	}

	private static TLObject newTarget(String data) {
		TLClass type = (TLClass) TLModelUtil.findType(TARGET_MODULE, TARGET_TYPE);
		TLObject result = DynamicModelService.getFactoryFor(TARGET_MODULE).createObject(type);
		result.tUpdateByName("data", data);
		return result;
	}

	private static TLTreeNode<?> node(Object businessObject) {
		return new DefaultMutableTLTreeModel(businessObject).getRoot();
	}

	private static void assertScriptExecuted(Runnable drop) {
		try {
			drop.run();
		} catch (RuntimeException ex) {
			assertTrue("Drop script not executed: " + ex, hasCause(ex, ScriptAbort.class));
			return;
		}
		fail("Drop script not executed.");
	}

	private static void assertRefused(Runnable drop) {
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
	 * {@link BoundComponent} providing the drop targets under test.
	 */
	public static class DropTestComponent extends BoundComponent {

		/**
		 * Configuration options for {@link DropTestComponent}.
		 */
		public interface Config extends BoundComponent.Config {

			@Name("table-drops")
			@EntryTag("drop")
			@Key(NamedConfiguration.NAME_ATTRIBUTE)
			Map<String, NamedTableDrop> getTableDrops();

			@Name("tree-drops")
			@EntryTag("drop")
			@Key(NamedConfiguration.NAME_ATTRIBUTE)
			Map<String, NamedTreeDrop> getTreeDrops();

		}

		/**
		 * Named table drop target.
		 */
		public interface NamedTableDrop extends com.top_logic.basic.config.NamedConfigMandatory {

			@Name("impl")
			PolymorphicConfiguration<? extends BusinessObjectTableDrop> getImpl();

		}

		/**
		 * Named tree drop target.
		 */
		public interface NamedTreeDrop extends com.top_logic.basic.config.NamedConfigMandatory {

			@Name("impl")
			PolymorphicConfiguration<? extends TreeDropTargetByExpression> getImpl();

		}

		private final Map<String, BusinessObjectTableDrop> _tableDrops = new HashMap<>();

		private final Map<String, TreeDropTargetByExpression> _treeDrops = new HashMap<>();

		/**
		 * Creates a {@link DropTestComponent} from configuration.
		 */
		public DropTestComponent(InstantiationContext context, Config config) throws ConfigurationException {
			super(context, config);
			for (NamedTableDrop drop : config.getTableDrops().values()) {
				_tableDrops.put(drop.getName(), context.getInstance(drop.getImpl()));
			}
			for (NamedTreeDrop drop : config.getTreeDrops().values()) {
				_treeDrops.put(drop.getName(), context.getInstance(drop.getImpl()));
			}
		}

		@Override
		protected boolean supportsInternalModel(Object object) {
			return true;
		}

		BusinessObjectTableDrop tableDrop(String name) {
			return _tableDrops.get(name);
		}

		TreeDropTargetByExpression treeDrop(String name) {
			return _treeDrops.get(name);
		}

	}

	public static Test suite() {
		Test kbTest = KBSetup.getSingleKBTest(TestScriptedDropSecurity.class,
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
		String configFile = CustomPropertiesDecorator.createFileName(TestScriptedDropSecurity.class, CONFIG_FILE);
		return TLTestSetup.createTLTestSetup(TestUtils.doNotMerge(new CustomPropertiesSetup(kbTest, configFile, true)));
	}

}
