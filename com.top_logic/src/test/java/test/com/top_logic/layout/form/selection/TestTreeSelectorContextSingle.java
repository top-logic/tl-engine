/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.selection;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.TLTestSetup;

import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.selection.TreeSelectorContext;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TLTreeNode;

/**
 * Tests for {@link TreeSelectorContext}, which is based on a {@link SelectField}, that allows
 * single selection.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TestTreeSelectorContextSingle extends TestTreeSelectorContext {

	@Override
	protected SelectField createField(TLTreeModel treeModel) {
		return FormFactory.newSelectField("testSingleTreeSelectField", treeModel, false, false);
	}

	@Override
	protected boolean isSelectableSelectionEntry(TLTreeNode node, TreeSelectorContext context) {
		return context.getOptionTree().getSelectionModel().isSelectable(node);
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(new TestSuite(TestTreeSelectorContextSingle.class));
	}
}
