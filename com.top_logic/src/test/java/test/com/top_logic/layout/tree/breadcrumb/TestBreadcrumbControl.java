/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.breadcrumb;

import junit.framework.Test;

import test.com.top_logic.layout.TestControl;
import test.com.top_logic.layout.tree.model.TestTLTreeModelHelper;

import com.top_logic.layout.tree.breadcrumb.BreadcrumbControl;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbData;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbDataOwner.NoBreadcrumbDataOwner;
import com.top_logic.layout.tree.breadcrumb.DefaultBreadcrumbRenderer;
import com.top_logic.layout.tree.breadcrumb.ImmutableBreadcrumbData;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;

/**
 * Tests the {@link BreadcrumbControl}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestBreadcrumbControl extends TestControl {

	private BreadcrumbData _data;

	private BreadcrumbControl _breadcrumb;
	
	private DefaultMutableTLTreeModel _tree;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_tree = TestTLTreeModelHelper.createInfiniteTree(2, "root");
		DefaultSingleSelectionModel selectionModel = new DefaultSingleSelectionModel(SelectionModelOwner.NO_OWNER);
		DefaultSingleSelectionModel displayModel = new DefaultSingleSelectionModel(SelectionModelOwner.NO_OWNER);
		_data = new ImmutableBreadcrumbData(_tree, selectionModel, displayModel, NoBreadcrumbDataOwner.INSTANCE);
		_breadcrumb = new BreadcrumbControl(DefaultBreadcrumbRenderer.defaultRenderer(), _data);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		_breadcrumb.detach();
		_data = null;
	}

	public void testSaveSelection() {
		final DefaultMutableTLTreeNode root = (DefaultMutableTLTreeNode) _tree.getRoot();
		final TLTreeNode child_1 = root.getChildAt(0);
		final TLTreeNode child_1_1 = child_1.getChildAt(0);
		final TLTreeNode child_1_1_1 = child_1_1.getChildAt(0);
		_data.getDisplayModel().setSingleSelection(child_1_1_1);
		_data.getSelectionModel().setSingleSelection(child_1_1_1);

		writeControl(_breadcrumb);

		root.updateNodeStructure();
		final TLTreeNode new_child_1 = root.getChildAt(0);
		final TLTreeNode new_child_1_1 = new_child_1.getChildAt(0);
		final TLTreeNode new_child_1_1_1 = new_child_1_1.getChildAt(0);
		
		assertEquals(new_child_1_1_1, _data.getDisplayModel().getSingleSelection());
		assertEquals(new_child_1_1_1, _data.getSelectionModel().getSingleSelection());
	}
	
	public static Test suite() {
		return TestControl.suite(TestBreadcrumbControl.class);
	}


}

