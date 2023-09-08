/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.model.utility;

import java.util.function.Supplier;

import junit.framework.TestCase;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.form.model.utility.DefaultTreeOptionModel;
import com.top_logic.layout.form.model.utility.LazyTreeOptionModel;
import com.top_logic.layout.form.model.utility.TreeOptionModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;

/**
 * Test for {@link LazyTreeOptionModel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestLazyTreeOptionModel extends TestCase {

	static class TreeOptionModelInitializer implements Supplier<TreeOptionModel<DefaultMutableTLTreeNode>> {

		Filter<Object> _initialOptionsFilter = item -> true;


		boolean _modelAquired = false;

		@Override
		public TreeOptionModel<DefaultMutableTLTreeNode> get() {
			_modelAquired = true;
			DefaultTreeOptionModel<DefaultMutableTLTreeNode> model =
				new DefaultTreeOptionModel<>(new DefaultMutableTLTreeModel());
			model.setSelectableOptionsFilter(_initialOptionsFilter);
			return model;
		}

	}

	public void testLazyConstruction() {
		TreeOptionModelInitializer impl = new TreeOptionModelInitializer();

		LazyTreeOptionModel<DefaultMutableTLTreeNode> treeOptionModel = new LazyTreeOptionModel<>(impl);
		assertFalse(impl._modelAquired);
		assertEquals(1, treeOptionModel.getOptionCount());
		assertTrue(impl._modelAquired);
	}

	public void testOptionFilter() {
		Filter<Object> filter = item -> item.hashCode() % 2 == 0;
		checkUpdateFilterBeforeGet(filter);
		checkUpdateFilterAfterGet(filter);
	}

	private void checkUpdateFilterBeforeGet(Filter<Object> filter) {
		TreeOptionModelInitializer impl = new TreeOptionModelInitializer();

		LazyTreeOptionModel<DefaultMutableTLTreeNode> treeOptionModel = new LazyTreeOptionModel<>(impl);
		assertFalse(impl._modelAquired);
		treeOptionModel.setSelectableOptionsFilter(filter);
		assertFalse(impl._modelAquired);
		assertEquals(filter, treeOptionModel.getSelectableOptionsFilter());
		assertFalse(impl._modelAquired);
		assertEquals(filter, treeOptionModel.delegate().getSelectableOptionsFilter());
		assertTrue(impl._modelAquired);
	}

	private void checkUpdateFilterAfterGet(Filter<Object> filter) {
		TreeOptionModelInitializer impl = new TreeOptionModelInitializer();

		LazyTreeOptionModel<DefaultMutableTLTreeNode> treeOptionModel = new LazyTreeOptionModel<>(impl);
		assertFalse(impl._modelAquired);
		Filter<? super DefaultMutableTLTreeNode> optionsFilter = treeOptionModel.getSelectableOptionsFilter();
		assertNotSame(filter, optionsFilter);
		assertTrue(impl._modelAquired);
		impl._modelAquired = false;
		treeOptionModel.setSelectableOptionsFilter(filter);
		assertEquals(filter, treeOptionModel.getSelectableOptionsFilter());
		assertFalse("Must not initialize delegate again.", impl._modelAquired);
	}

	public void testShowRoot() {
		checkUpdateShowRootBeforeGet(true);
		checkUpdateShowRootBeforeGet(false);
		checkUpdateShowRootAfterGet();
	}

	private void checkUpdateShowRootAfterGet() {
		TreeOptionModelInitializer impl = new TreeOptionModelInitializer();

		LazyTreeOptionModel<DefaultMutableTLTreeNode> treeOptionModel = new LazyTreeOptionModel<>(impl);
		assertFalse(impl._modelAquired);
		boolean showRootNode = treeOptionModel.showRootNode();
		assertTrue(impl._modelAquired);
		impl._modelAquired = false;
		treeOptionModel.setShowRoot(!showRootNode);
		assertEquals(!showRootNode, treeOptionModel.showRootNode());
		assertFalse("Must not initialize delegate again.", impl._modelAquired);
	}

	private void checkUpdateShowRootBeforeGet(boolean showRoot) {
		TreeOptionModelInitializer impl = new TreeOptionModelInitializer();

		LazyTreeOptionModel<DefaultMutableTLTreeNode> treeOptionModel = new LazyTreeOptionModel<>(impl);
		assertFalse(impl._modelAquired);
		treeOptionModel.setShowRoot(showRoot);
		assertFalse(impl._modelAquired);
		assertEquals(showRoot, treeOptionModel.showRootNode());
		assertFalse(impl._modelAquired);
		assertEquals(showRoot, treeOptionModel.delegate().showRootNode());
		assertTrue(impl._modelAquired);
	}

}
