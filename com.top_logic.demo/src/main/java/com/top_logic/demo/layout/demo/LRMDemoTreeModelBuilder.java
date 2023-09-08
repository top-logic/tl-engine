/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import com.top_logic.layout.tree.component.TreeComponent;
import com.top_logic.layout.tree.component.TreeModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.export.NoPreload;
import com.top_logic.model.export.PreloadOperation;

/**
 * {@link TreeModelBuilder} for testing {@link TreeComponent}.
 * 
 * @author <a href=mailto:twi@top-logic.com>twi</a>
 */
public class LRMDemoTreeModelBuilder implements TreeModelBuilder<DemoTreeNodeModel> {

	private static final int DEFAULT_TREE_DEPTH = 5;
	private static final int DEFAULT_TREE_FANOUT = 4;

	/**
	 * Singleton {@link LRMDemoTreeModelBuilder} instance.
	 */
	public static final LRMDemoTreeModelBuilder INSTANCE = new LRMDemoTreeModelBuilder();

	private LRMDemoTreeModelBuilder() {
		// Singleton constructor.
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent component) {
		return new DemoTreeNodeModel(null, "Node", 0);
	}
	
    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel == null;
    }

	@Override
	public boolean supportsNode(LayoutComponent contextComponent, Object node) {
		return node instanceof DemoTreeNodeModel;
	}

	@Override
	public boolean canExpandAll() {
		return true;
	}

	@Override
	public boolean isLeaf(LayoutComponent contextComponent, DemoTreeNodeModel node) {
		return node.getDepth() == DEFAULT_TREE_DEPTH;
	}

	@Override
	public Iterator<? extends DemoTreeNodeModel> getChildIterator(LayoutComponent contextComponent,
			DemoTreeNodeModel node) {

		ArrayList<DemoTreeNodeModel> result = new ArrayList<>();
		if (node.getDepth() < DEFAULT_TREE_DEPTH) {
			for (int n = 0; n < DEFAULT_TREE_FANOUT; n++) {
				result.add(new DemoTreeNodeModel(node, node.getName() + "." + n, node.getDepth() + 1));
			}
		}
		return result.iterator();
	}

	@Override
	public Object retrieveModelFromNode(LayoutComponent contextComponent, DemoTreeNodeModel node) {
		return null;
	}

	@Override
	public Collection<? extends DemoTreeNodeModel> getParents(LayoutComponent contextComponent,
			DemoTreeNodeModel node) {
		DemoTreeNodeModel parent = node.getParent();
		return parent == null ? Collections.emptyList() : Collections.singletonList(parent);
	}

	@Override
	public PreloadOperation loadForExpansion() {
		return NoPreload.INSTANCE;
	}

	@Override
	public Collection<? extends DemoTreeNodeModel> getNodesToUpdate(LayoutComponent contextComponent,
			Object businessObject) {
		return Collections.emptySet();
	}

	@Override
	public Set<TLStructuredType> getTypesToObserve() {
		return Collections.emptySet();
	}

}
