/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import java.util.Comparator;

import junit.framework.Test;

import test.com.top_logic.layout.table.WrappingTableRowFilter;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.DefaultTreeTableModel;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableBuilder;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.tree.model.TreeTableModel;

/**
 * Standard tree model test for {@link DefaultTreeTableModel}.
 * 
 * @see TestDefaultTreeTableModelEmbedding
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDefaultTreeTableModel extends AbstractTreeTableModelTest<DefaultTreeTableNode> {

	@Override
	protected AbstractTreeTableModel<DefaultTreeTableNode> createTreeTableModel() {
		TableConfiguration tableConfig = TableConfigurationFactory.table();
		tableConfig.setFilterDisplayChildren(false);
		tableConfig.setFilterDisplayParents(false);

		DefaultTreeTableModel result = new DefaultTreeTableModel(new DefaultTreeTableBuilder(), null, list("A", "B", "C"),
			tableConfig);

		return result;
	}

	private DefaultTreeTableModel treeTableModel() {
		return (DefaultTreeTableModel) treemodel;
	}

	public void testCollapseExpandEvent() {
		AbstractTreeUIModelTest.testCollapseExpandEvent(treeTableModel(), child_1);
	}

	public void testExpandAfterFilter() {
		DefaultTreeTableModel model = treeTableModel();
		DefaultTreeTableNode filteredChild = rootNode.createChild("filterOut");
		DefaultTreeTableNode notFilteredGrandChild = filteredChild.createChild("notFilteredOut");

		TreeTableModel table = model.getTable();
		table.setFilter(new WrappingTableRowFilter(new Filter<DefaultTreeTableNode>() {

			@Override
			public boolean accept(DefaultTreeTableNode anObject) {
				return !"filterOut".equals(anObject.getBusinessObject().toString());
			}
		}), table.getOrder());

		model.setExpanded(rootNode, true);
		assertTrue(model.isDisplayed(rootNode));
		assertFalse("Node does not match filter.", model.isDisplayed(filteredChild));
		assertFalse("Node matches filter but parent doesn't.", model.isDisplayed(notFilteredGrandChild));
	}

	public void testRemoveAfterSort() {
		DefaultTreeTableModel model = treeTableModel();
		DefaultTreeTableNode child1 = child_1;
		for (int i = 0; i < 10; i++) {
			child1.createChild("child_1." + i);
		}
		child1.setExpanded(true);
		DefaultTreeTableNode child2 = child_2;
		// The implementation bug is that the children of the first (in tree order) child are used
		// to determine the next index to check. Therefore not the first (in table order) child must
		// be removed but e.g. the second. Therefore this additional node is needed.
		DefaultTreeTableNode child3 = rootNode.createChild("3");

		TreeTableModel table = model.getTable();
		table.setOrder(new Comparator<DefaultTreeTableNode>() {

			@Override
			public int compare(DefaultTreeTableNode o1, DefaultTreeTableNode o2) {
				return -((String) o1.getBusinessObject()).compareTo((String) o2.getBusinessObject());
			}
		});
		assertTrue(table.getRowOfObject(child2) < table.getRowOfObject(child1));
		assertTrue(table.getRowOfObject(child3) < table.getRowOfObject(child2));
		rootNode.removeChild(rootNode.getIndex(child2));
		assertEquals(-1, table.getRowOfObject(child2));
	}

	/**
	 * When updating a tree table after an object has been deleted, the remove operation must not
	 * depend on the sort order, since this can no loger be computed on deleted objects.
	 */
	public void testRemoveNotDependingOnSortOder() {
		class Obj {
			private String _value;

			public Obj(String value) {
				_value = value;
			}

			public void delete() {
				_value = "";
			}

			@Override
			public String toString() {
				return _value;
			}
		}

		TableConfiguration tableConfig = TableConfigurationFactory.table();
		Comparator<TLTreeNode<?>> order = new Comparator<>() {
			@Override
			public int compare(TLTreeNode<?> o1, TLTreeNode<?> o2) {
				return ((Obj) o1.getBusinessObject())._value.compareTo(((Obj) o2.getBusinessObject())._value);
			}
		};

		Obj rootObj = new Obj("root");
		DefaultTreeTableModel treeTable =
			new DefaultTreeTableModel(new DefaultTreeTableBuilder(), rootObj, list("A"),
				tableConfig);

		DefaultTreeTableNode root = treeTable.getRoot();
		Obj cObj;
		DefaultTreeTableNode c = root.createChild(cObj = new Obj("C"));
		DefaultTreeTableNode c1 = c.createChild(new Obj("x"));
		DefaultTreeTableNode c2 = c.createChild(new Obj("y"));
		Obj bObj;
		DefaultTreeTableNode b = root.createChild(bObj = new Obj("B"));
		Obj aObj;
		DefaultTreeTableNode a = root.createChild(aObj = new Obj("A"));

		TreeTableModel table = treeTable.getTable();
		table.setOrder(order);
		root.setExpanded(true);
		c.setExpanded(true);

		assertEquals(5, table.getDisplayedRows().size());
		assertEquals(0, table.getRowOfObject(a));
		assertEquals(1, table.getRowOfObject(b));
		assertEquals(2, table.getRowOfObject(c));
		assertEquals(3, table.getRowOfObject(c1));
		assertEquals(4, table.getRowOfObject(c2));

		bObj.delete();
		cObj.delete();
		root.removeChild(root.getIndex(b));
		root.removeChild(root.getIndex(c));

		assertEquals(1, table.getDisplayedRows().size());
	}

	public void testIgnoreTableAspect() {
		DefaultTreeTableModel model = treeTableModel();
		model.setRootVisible(true);
		rootNode.setExpanded(true);
		assertTrue(child_1.isDisplayed());
		assertFalse("Tests needs that child_1 is not expanded, such that expandend should display children.",
			child_1.isExpanded());
		child_1.setExpanded(true);
		assertTrue("Ticket #17866: Expansion of a displayed node should display it children.", child_1_1.isDisplayed());
	}

	public static Test suite() {
		return suite(TestDefaultTreeTableModel.class);
	}

}
