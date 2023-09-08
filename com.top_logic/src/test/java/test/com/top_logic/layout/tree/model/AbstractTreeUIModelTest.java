/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import static com.top_logic.layout.tree.model.TreeModelEvent.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.layout.tree.model.TreeModelEvent;
import com.top_logic.layout.tree.model.TreeModelListener;
import com.top_logic.layout.tree.model.TreeUIModel;

/**
 * Abstract class for testing {@link TreeUIModel}.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractTreeUIModelTest<N> extends BasicTestCase {

	/** Model under test. */
	protected TreeUIModel<N> _testModel;

	/** Children of root. At least 4 children. */
	protected List<? extends N> _firstLevelChildren;

	/** Child of root with at least 3 children. */
	protected N _childWithChildren;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_testModel = newTestModel();
		N root = _testModel.getRoot();
		_firstLevelChildren = _testModel.getChildren(root);
		assertTrue(_firstLevelChildren.size() >= 4);
		for (N child : _firstLevelChildren) {
			if (_testModel.getChildren(child).size() >= 3) {
				_childWithChildren = child;
				break;
			}
		}
		assertNotNull(_childWithChildren);

	}

	/**
	 * Tests sending events when collapsing or expanding model.
	 * 
	 */
	public void testCollapseExpandEvent() {
		testCollapseExpandEvent(_testModel, _childWithChildren);
	}

	/**
	 * Tests that {@link TreeModelEvent#BEFORE_COLLAPSE}, {@link TreeModelEvent#AFTER_COLLAPSE},
	 * {@link TreeModelEvent#BEFORE_EXPAND}, and {@link TreeModelEvent#AFTER_EXPAND} are fired
	 * correctly.
	 * 
	 * @param treeModel
	 *        The {@link TreeUIModel} under test.
	 * @param node
	 *        The node to collapse/expand.
	 */
	public static <N> void testCollapseExpandEvent(TreeUIModel<N> treeModel, final N node) {
		final Set<Integer> relevantEventTypes = set(BEFORE_COLLAPSE, AFTER_COLLAPSE, BEFORE_EXPAND, AFTER_EXPAND);
		final ArrayList<TreeModelEvent> events = new ArrayList<>();
		treeModel.addTreeModelListener(new TreeModelListener() {

			@Override
			public void handleTreeUIModelEvent(TreeModelEvent evt) {
				if (evt.getNode().equals(node)
					&& relevantEventTypes.contains(Integer.valueOf(evt.getType()))) {
					events.add(evt);
				}
			}
		});
		if (treeModel.isExpanded(node)) {
			testCollapse(events, treeModel, node);
			testExpand(events, treeModel, node);
		} else {
			testExpand(events, treeModel, node);
			testCollapse(events, treeModel, node);
		}
	}

	private static <N> void testExpand(ArrayList<TreeModelEvent> events, TreeUIModel<N> treeModel, final N node) {
		treeModel.setExpanded(node, true);
		assertEquals("Expected 'before expand' and 'after expand' event.", 2, events.size());
		assertSame("First event is a 'before expand' event.", BEFORE_EXPAND, events.get(0).getType());
		assertSame("Second event is an 'after expand' event.", AFTER_EXPAND, events.get(1).getType());
		events.clear();

		treeModel.setExpanded(node, true);
		assertTrue("Expanding node twice must not fire events.", events.isEmpty());
	}

	private static <N> void testCollapse(ArrayList<TreeModelEvent> events, TreeUIModel<N> treeModel, final N node) {
		treeModel.setExpanded(node, false);
		assertEquals("Expected 'before collapse' and 'after collapse' event.", 2, events.size());
		assertSame("First event is a 'before collapse' event.", BEFORE_COLLAPSE, events.get(0).getType());
		assertSame("Second event is an 'after collapse' event.", AFTER_COLLAPSE, events.get(1).getType());
		events.clear();

		treeModel.setExpanded(node, false);
		assertTrue("Callapsing node twice must not fire events.", events.isEmpty());
	}

	/**
	 * Creates a {@link TreeUIModel} with
	 * <ul>
	 * <li>At least 4 children of root.</li>
	 * <li>At least 1 child of root with 3 children.</li>
	 * </ul>
	 */
	protected abstract TreeUIModel<N> newTestModel();

	/**
	 * a cumulative {@link Test} for all Tests in given class.
	 */
	public static Test suite(Class<? extends AbstractTreeUIModelTest<?>> testClass) {
		return TLTestSetup.createTLTestSetup(testClass);
	}

}

