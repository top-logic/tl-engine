/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;

import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.DefaultStructureTreeUIModel;
import com.top_logic.layout.tree.model.TreeModelEvent;
import com.top_logic.layout.tree.model.TreeModelListener;
import com.top_logic.layout.tree.model.TreeUIModel;

/**
 * Tests for {@link DefaultStructureTreeUIModel}.
 * 
 * @since 5.7.4
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestDefaultStructureTreeUIModel extends AbstractTreeUIModelTest<DefaultMutableTLTreeNode> {

	@Override
	protected TreeUIModel<DefaultMutableTLTreeNode> newTestModel() {
		DefaultMutableTLTreeModel applicationModel = new DefaultMutableTLTreeModel();
		DefaultMutableTLTreeNode root = applicationModel.getRoot();
		DefaultMutableTLTreeNode childWithChildren = root.createChild("0");
		root.createChild("1");
		root.createChild("2");
		root.createChild("3");
		childWithChildren.createChild("0.0");
		childWithChildren.createChild("0.1");
		childWithChildren.createChild("0.2");
		return new DefaultStructureTreeUIModel<>(applicationModel);
	}
	
	private DefaultStructureTreeUIModel<DefaultMutableTLTreeNode> getModel() {
		return (DefaultStructureTreeUIModel<DefaultMutableTLTreeNode>) _testModel;
	}

	public void testCollapseExpandAll() {
		DefaultStructureTreeUIModel<DefaultMutableTLTreeNode> model = getModel();
		model.setExpandAll(false);
		checkHierarchy(model, new CheckAllNodeCallback() {

			@Override
			public void check(DefaultStructureTreeUIModel<DefaultMutableTLTreeNode> model, DefaultMutableTLTreeNode node) {
				assertFalse(model.isExpanded(node));
			}

		});

		final List<TreeModelEvent> events = new ArrayList<>();
		model.addTreeModelListener(new TreeModelListener() {

			@Override
			public void handleTreeUIModelEvent(TreeModelEvent evt) {
				events.add(evt);
			}
		});

		model.setExpandAll(true);
		checkHierarchy(model, checkEventCallback(events, TreeModelEvent.BEFORE_EXPAND, TreeModelEvent.AFTER_EXPAND));
		events.clear();

		model.setExpandAll(false);
		checkHierarchy(model, checkEventCallback(events, TreeModelEvent.BEFORE_COLLAPSE, TreeModelEvent.AFTER_COLLAPSE));
		events.clear();
	}

	private CheckAllNodeCallback checkEventCallback(final List<TreeModelEvent> events, final int beforeEventType,
			final int afterEventType) {
		return new CheckAllNodeCallback() {

			@Override
			public void check(DefaultStructureTreeUIModel<DefaultMutableTLTreeNode> model, DefaultMutableTLTreeNode node) {
				int i = 0;
				for (TreeModelEvent evt : events) {
					if (evt.getNode() != node) {
						continue;
					}
					if (evt.getType() == beforeEventType) {
						switch (i) {
							case 0: {
								i = 1;
								break;
							}
							case 1: {
								fail("'Before' event found twice.");
								break;
							}
							case 2: {
								fail("'Before' event found after 'after' event.");
								break;
							}
							default: {
								fail("Unexpected state");
								break;
							}
						}
					} else if (evt.getType() == afterEventType) {
						switch (i) {
							case 0: {
								fail("No 'Before' event found.");
								break;
							}
							case 1: {
								i = 2;
								break;
							}
							case 2: {
								fail("'After' event found twice.");
								break;
							}
							default: {
								fail("Unexpected state");
								break;
							}
						}
						break;
					}
				}
				switch (i) {
					case 0: {
						fail("No 'before' event found.");
						break;
					}
					case 1: {
						fail("No 'after' event found.");
						break;
					}
					case 2: {
						// found first 'before' then 'after'
						break;
					}
					default: {
						fail("Unexpected state");
						break;
					}
				}
			}
		};
	}

	private interface CheckAllNodeCallback {
		void check(DefaultStructureTreeUIModel<DefaultMutableTLTreeNode> model, DefaultMutableTLTreeNode node);
	}

	private void checkHierarchy(DefaultStructureTreeUIModel<DefaultMutableTLTreeNode> model,
			CheckAllNodeCallback callback) {
		checkHierarchy(model, model.getRoot(), callback);
	}

	private void checkHierarchy(DefaultStructureTreeUIModel<DefaultMutableTLTreeNode> model,
			DefaultMutableTLTreeNode node, CheckAllNodeCallback callback) {
		callback.check(model, node);
		Iterator<? extends DefaultMutableTLTreeNode> children = model.getChildIterator(node);
		while (children.hasNext()) {
			checkHierarchy(model, children.next(), callback);
		}
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestDefaultStructureTreeUIModel}.
	 */
	public static Test suite() {
		return suite(TestDefaultStructureTreeUIModel.class);
	}

}

