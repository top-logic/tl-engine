/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.compare;

import java.util.List;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.TreeView;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.decorator.CompareInfo;
import com.top_logic.layout.form.decorator.CompareService;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.TreeTableDialog;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.util.Utils;

/**
 * Comparison of two trees.
 * 
 * @see CompareListsDialog
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompareTreesDialog<N> extends TreeTableDialog {

	static final DisplayDimension EIGHTY_PERCENT = DisplayDimension.dim(80, DisplayUnit.PERCENT);

	/**
	 * Creates a new {@link CompareTreesDialog}.
	 * @param width
	 *        See
	 *        {@link TreeTableDialog#TreeTableDialog(DisplayDimension, DisplayDimension)}.
	 * @param height
	 *        See
	 *        {@link TreeTableDialog#TreeTableDialog(DisplayDimension, DisplayDimension)}.
	 * @param baseValueTree
	 *        The original tree.
	 * @param baseValueRoot
	 *        The root model of the original tree.
	 * @param changeValueTree
	 *        The tree to compare with base value tree.
	 * @param changeValueRoot
	 *        The root model of the compare tree.
	 * @param identifierMapping
	 *        {@link Mapping} that defines the identity of the objects in comparison view.
	 * @param columnNames
	 *        The names of the columns to display.
	 * 
	 * @return May be <code>null</code>, when original root an compare root are not equal.
	 */
	public static <N> CompareTreesDialog<N> newCompareTreesDialog(DisplayDimension width, DisplayDimension height,
			TreeView<N> baseValueTree, N baseValueRoot, TreeView<N> changeValueTree, N changeValueRoot, Mapping<Object, ?> identifierMapping,
			List<String> columnNames, CompareAlgorithm algorithm) {
		if (!Utils.equals(identifierMapping.map(baseValueRoot), identifierMapping.map(changeValueRoot))) {
			return null;
		}
		return new CompareTreesDialog<>(width, height, baseValueTree, baseValueRoot, changeValueTree,
			changeValueRoot, identifierMapping,
			columnNames, algorithm);
	}

	/**
	 * Creates a new {@link CompareTreesDialog}, with width and height of 80%.
	 * 
	 * @param baseValueTree
	 *        The original tree.
	 * @param baseValueRoot
	 *        The root model of the original tree.
	 * @param changeValueTree
	 *        The tree to compare with base value tree.
	 * @param changeValueRoot
	 *        The root model of the compare tree.
	 * @param columnNames
	 *        The names of the columns to display.
	 * 
	 * @return May be <code>null</code>, when original root an compare root are not equal.
	 */
	public static <N> CompareTreesDialog<N> newCompareTreesDialog(TreeView<N> baseValueTree, N baseValueRoot,
			TreeView<N> changeValueTree, N changeValueRoot, List<String> columnNames, CompareAlgorithm algorithm) {
		return newCompareTreesDialog(EIGHTY_PERCENT, EIGHTY_PERCENT, baseValueTree, baseValueRoot, changeValueTree,
			changeValueRoot, CompareInfo.identifierMapping(), columnNames, algorithm);
	}

	/**
	 * Creates a new {@link CompareTreesDialog} for trees where original and compare tree are the
	 * same.
	 * 
	 * @see #newCompareTreesDialog(TreeView, Object, TreeView, Object, List, CompareAlgorithm)
	 */
	public static <N> CompareTreesDialog<N> newCompareTreesDialog(TreeView<N> tree, N baseValueRoot, N changeValueRoot,
			List<String> columnNames, CompareAlgorithm algorithm) {
		return newCompareTreesDialog(tree, baseValueRoot, tree, changeValueRoot, columnNames, algorithm);
	}

	private final TreeView<N> _changeValueTree;

	private final TreeView<N> _baseValueTree;

	private final List<String> _columnNames;

	private final N _baseValueRoot;

	private final N _changeValueRoot;

	private final Mapping<Object, ?> _identifierMapping;

	private final CompareAlgorithm _algorithm;

	private TableConfigurationProvider _tableConfig = TableConfigurationFactory.emptyProvider();

	private ConfigKey _tableConfigKey = ConfigKey.none();

	private boolean _rootVisible;

	private CompareTreesDialog(DisplayDimension width, DisplayDimension height, TreeView<N> origTree,
			N origRoot, TreeView<N> compareTree, N compareRoot, Mapping<Object, ?> identifierMapping,
			List<String> columnNames, CompareAlgorithm algorithm) {
		super(width, height);
		_baseValueTree = origTree;
		_baseValueRoot = origRoot;
		_changeValueTree = compareTree;
		_changeValueRoot = compareRoot;
		_identifierMapping = identifierMapping;
		_columnNames = columnNames;
		_algorithm = algorithm;
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(MessageBox.button(ButtonType.CLOSE, getDiscardClosure()));
	}


	@Override
	protected AbstractTreeTableModel<?> createTreeModel() {
		CompareService<CompareInfo> compareService = new CompareService<>(null, _algorithm);
		TableConfigurationProvider tableConfig =
			TableConfigurationFactory.combine(_tableConfig, WrapCompareCells.INSTANCE);
		AbstractTreeTableModel<?> compareTreeTableModel =
			compareService.getCompareTreeTableModel(_baseValueTree, _baseValueRoot,
			_changeValueTree, _changeValueRoot, _columnNames, tableConfig, _identifierMapping);
		if (_rootVisible) {
			compareTreeTableModel.setRootVisible(true);
			// Expand root as just one displayed node looks poor 
			compareTreeTableModel.getRoot().setExpanded(true);
		}
		return compareTreeTableModel;
	}

	@Override
	protected ConfigKey configKey() {
		return _tableConfigKey;
	}

	/**
	 * Sets the {@link ConfigKey key} to store modification on the table in the personal
	 * configuration.
	 */
	public void setConfigKey(ConfigKey tableConfigKey) {
		_tableConfigKey = tableConfigKey;
	}

	/**
	 * Sets the {@link TableConfigurationProvider} to define the displayed table.
	 */
	public void setTableConfig(TableConfigurationProvider tableConfig) {
		_tableConfig = tableConfig;
	}

	/** 
	 * Defines whether the root node of the tree should be visible.
	 */
	public void setRootVisible(boolean rootVisible) {
		_rootVisible = rootVisible;
	}

}

