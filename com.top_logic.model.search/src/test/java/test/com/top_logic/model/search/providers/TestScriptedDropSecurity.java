/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.providers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.layout.table.dnd.BusinessObjectTableDrop;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.ScriptAbort;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.providers.DropSecurity;
import com.top_logic.model.search.providers.TreeDropTargetByExpression;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.service.CommandApprovalService;

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
public class TestScriptedDropSecurity extends AbstractDropSecurityTest<TestScriptedDropSecurity.DropTestComponent> {

	private static final String CONFIG_FILE = "TestScriptedDropSecurity.xml";

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

	@Override
	protected TLObject newTarget(String name) {
		TLClass type = (TLClass) TLModelUtil.findType(TARGET_MODULE, TARGET_TYPE);
		TLObject result = DynamicModelService.getFactoryFor(TARGET_MODULE).createObject(type);
		result.tUpdateByName("data", name);
		return result;
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
		return suite(TestScriptedDropSecurity.class, CONFIG_FILE);
	}

}
