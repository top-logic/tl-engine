/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.selection;

import java.util.Collections;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.utility.TreeOptionModel;
import com.top_logic.layout.form.selection.SelectDialogConfig;
import com.top_logic.layout.form.selection.SelectDialogProvider;
import com.top_logic.layout.form.selection.TreeSelectorContext;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelFilter;

/**
 * Tests for {@link TreeSelectorContext}
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public abstract class TestTreeSelectorContext extends BasicTestCase {

	private SelectField selectField;
	private LabelProvider labelProvider;
	protected TreeSelectorContext context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createSelectField();
		createLabelProvider();
		createTreeSelectorContext();
	}

	private void createSelectField() {
		TLTreeModel treeModel = createTLTreeModel();
		selectField = createField(treeModel);
		setFixedOptions();
	}

	protected abstract SelectField createField(TLTreeModel treeModel);

	private void setFixedOptions() {
		TLTreeNode fixedOption = getFixedOption();
		selectField.setFixedOptions(FilterFactory.not(new SelectionModelFilter(Collections
			.singletonList(fixedOption))));
	}

	private TLTreeNode getFixedOption() {
		return ((DefaultMutableTLTreeNode) selectField.getOptionsAsTree().getBaseModel().getRoot()).getChildAt(1);
	}

	private TLTreeModel createTLTreeModel() {
		DefaultMutableTLTreeModel treeModel = new DefaultMutableTLTreeModel();
		DefaultMutableTLTreeNode root = treeModel.getRoot();
		root.setBusinessObject("root");

		DefaultMutableTLTreeNode node1 = root.createChild("1");
		DefaultMutableTLTreeNode node2 = root.createChild("2");
		DefaultMutableTLTreeNode node11 = node1.createChild("11");
		DefaultMutableTLTreeNode node12 = node1.createChild("12");
		DefaultMutableTLTreeNode node111 = node11.createChild("111");

		return treeModel;
	}

	private void createLabelProvider() {
		labelProvider = new LabelProvider() {

			@Override
			public String getLabel(Object object) {
				return (String) ((DefaultMutableTLTreeNode) object).getBusinessObject();
			}
		};

	}

	private void createTreeSelectorContext() {
		SelectDialogConfig selectorConfig = SelectDialogProvider.newDefaultConfig();
		context = new TreeSelectorContext(selectField, null, selectorConfig);
	}

	public void testCannotMoveFixedOptionToSelection() {
		TLTreeNode fixedNode = getFixedOption();
		assertFalse("Fixed options must not be selectable!", getOptions().isSelectable(fixedNode));
	}

	public void testCannotRemoveFixedSelection() {
		createFixedSelection();

		assertFalse("Fixed options must not be removable from selection", isSelectableSelectionEntry(getFixedOption(), context));
	}

	public void testCanMoveLegalOptionToSelection() {
		assertTrue("Legal options must be selectable!", getOptions().isSelectable(getRootNode()));
	}

	public void testCanRemoveLegalSelection() {
		createLegalSelection();

		assertTrue("Legal selection must be removable!", isSelectableSelectionEntry(getSelectedNode(), context));
	}

	public void testCannotMoveIllegalOptionToSelection() {
		createLegalOptions();

		assertFalse("Not selected invalid options must not be selectable!", getOptions().isSelectable(getRootNode()));
	}

	/* Test for situation, when select field contains an illegal selection, the user opens the popup
	 * select dialog, removes the illegal selection, then one still must be able to reselect the
	 * illegal option, as long as the dialog has not been closed. */
	public void testCanMoveIllegalSelectedOptionToSelection() {
		createIllegalSelection();
		context.removeAllFromSelection();

		assertTrue("Options from illegal selection must be selectable!",
			getOptions().isSelectable(getSelectedNode()));
	}

	public void testCanRemoveIllegalSelection() {
		createIllegalSelection();

		assertTrue("Illegal selection must be removable!", isSelectableSelectionEntry(getSelectedNode(), context));
	}

	protected SelectionModel getOptions() {
		return context.getOptionTree().getSelectionModel();
	}

	private void createFixedSelection() {
		Filter fixedOptions = selectField.getFixedOptions();
		selectField.setFixedOptions(Collections.emptyList());
		selectField.setAsSingleSelection(getFixedOption());
		selectField.setFixedOptions(fixedOptions);

		createTreeSelectorContext();
	}

	private void createIllegalSelection() {
		createLegalOptions();
		TLTreeNode currentSelection = getSelectedNode();
		selectField.setAsSelection(Collections.singletonList(currentSelection));
		createTreeSelectorContext();
	}

	private void createLegalOptions() {
		final TLTreeNode<?> onlyLegalOption = getRootNode().getChildAt(0);
		@SuppressWarnings("unchecked")
		TreeOptionModel<TLTreeNode<?>> optionsAsTree = (TreeOptionModel<TLTreeNode<?>>) selectField.getOptionsAsTree();
		optionsAsTree.setSelectableOptionsFilter(new Filter<TLTreeNode<?>>() {
			@Override
			public boolean accept(TLTreeNode<?> node) {
				return node.equals(onlyLegalOption);
			}
		});

		createTreeSelectorContext();
	}

	private void createLegalSelection() {
		TLTreeNode currentSelection = getSelectedNode();
		selectField.setAsSelection(Collections.singletonList(currentSelection));
		createTreeSelectorContext();
	}

	private TLTreeNode getSelectedNode() {
		return getRootNode().getChildAt(0).getChildAt(0);
	}

	protected abstract boolean isSelectableSelectionEntry(TLTreeNode node, TreeSelectorContext context);

	protected TLTreeNode getRootNode() {
		return ((TLTreeNode) selectField.getOptionsAsTree().getBaseModel().getRoot());
	}
}
