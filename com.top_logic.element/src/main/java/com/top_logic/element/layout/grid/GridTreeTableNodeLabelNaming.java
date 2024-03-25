/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.col.search.SearchResult;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.util.Utils;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.ui.BreadcrumbStrings;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.ColumnConfiguration;

/**
 * A {@link ModelNamingScheme} for identifying a row in the tree grid by the label path to it.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class GridTreeTableNodeLabelNaming
		extends AbstractModelNamingScheme<GridTreeTableNode, GridTreeTableNodeLabelNaming.GridNodeLabelName> {

	/** {@link ModelName} for the {@link GridTreeTableNodeLabelNaming}. */
	public interface GridNodeLabelName extends ModelName {

		/**
		 * Reference to the {@link GridComponent}.
		 */
		ModelName getComponent();

		/** @see #getComponent() */
		void setComponent(ModelName value);

		/**
		 * Path of labels from the root to the referenced node.
		 */
		@Format(BreadcrumbStrings.class)
		List<String> getPath();

		/** @see #getPath() */
		void setPath(List<String> value);

	}

	/**
	 * This class has a long name to make sure nobody tries to use it accidentally outside of this
	 * class.
	 */
	enum ScriptingGridPerColumnWrapperLabelProvider {

		NAME_COLUMN {

			@Override
			String getLabel(GridTreeTableNode node, TableModel tableModel) {
				return getCellLabel(node, tableModel, NAME_COLUMN_NAME);
			}

		},
		OTHER_COLUMN {

			@Override
			String getLabel(GridTreeTableNode node, TableModel tableModel) {
				/* This might not be the label the user actually sees, but it's good enough for the
				 * scripting for now, as a fallback for weird tree-grids without self- and
				 * name-column. */
				Object attributed = getAttributed(node);
				return MetaLabelProvider.INSTANCE.getLabel(attributed);
			}

			private Object getAttributed(GridTreeTableNode node) {
				FormGroup formGroup = (FormGroup) node.getBusinessObject();
				return GridComponent.getRowObject(formGroup);
			}

		};

		static ScriptingGridPerColumnWrapperLabelProvider getInstanceFor(TableModel tableModel) {
			if (hasColumn(tableModel, NAME_COLUMN_NAME)) {
				return NAME_COLUMN;
			}
			return OTHER_COLUMN;
		}

		abstract String getLabel(GridTreeTableNode node, TableModel tableModel);

		static boolean hasColumn(TableModel tableModel, String columnName) {
			return tableModel.getColumnNames().contains(columnName);
		}

		@SuppressWarnings({ "unchecked" })
		static String getCellLabel(GridTreeTableNode node, TableModel tableModel, String columnName) {
			ColumnConfiguration columnConfiguration = tableModel.getColumnDescription(columnName);
			Object accessorValue = tableModel.getValueAt(node, columnName);
			Object rawOrFieldValue = columnConfiguration.getSortKeyProvider().map(accessorValue);
			return columnConfiguration.getFullTextProvider().getLabel(rawOrFieldValue);
		}

	}

	static final String NAME_COLUMN_NAME = AbstractWrapper.NAME_ATTRIBUTE;

	@Override
	protected boolean isCompatibleModel(GridTreeTableNode node) {
		GridComponent grid = getGrid(node);
		Maybe<? extends ModelName> modelName = ModelResolver.buildModelNameIfAvailable(grid);
		if (!modelName.hasValue()) {
			return false;
		}
		Collection<String> labelPath = getStringPathFromRoot(node, grid);
		return !(labelPath.contains(null) || labelPath.contains(""));
	}

	@Override
	protected void initName(GridNodeLabelName name, GridTreeTableNode node) {
		GridComponent grid = getGrid(node);
		name.setComponent(ModelResolver.buildModelName(grid));
		name.setPath(getStringPathFromRoot(node, grid));
	}

	private List<String> getStringPathFromRoot(GridTreeTableNode node, GridComponent grid) {
		List<GridTreeTableNode> nodePath = getNodePathFromRoot(node);
		List<String> labelPath = new ArrayList<>();
		TableModel tableModel = getTableModel(grid);
		ScriptingGridPerColumnWrapperLabelProvider labelProvider =
			ScriptingGridPerColumnWrapperLabelProvider.getInstanceFor(tableModel);
		for (GridTreeTableNode ancestor : nodePath) {
			labelPath.add(labelProvider.getLabel(ancestor, tableModel));
		}
		return labelPath;
	}

	private List<GridTreeTableNode> getNodePathFromRoot(GridTreeTableNode node) {
		GridTreeTableNode ancestor = node;
		List<GridTreeTableNode> path = new ArrayList<>();
		do {
			path.add(ancestor);
			ancestor = ancestor.getParent();

		} while (ancestor != null);
		Collections.reverse(path);
		return path;
	}

	private GridComponent getGrid(GridTreeTableNode node) {
		GridTreeTableModel treeModel = (GridTreeTableModel) node.getModel();
		return treeModel.getGrid();
	}

	@Override
	public GridTreeTableNode locateModel(ActionContext context, GridNodeLabelName name) {
		GridComponent grid = (GridComponent) context.resolve(name.getComponent());
		TableModel tableModel = getTableModel(grid);
		GridTreeTableModel treeModel = getTree(grid);
		ScriptingGridPerColumnWrapperLabelProvider labelProvider =
			ScriptingGridPerColumnWrapperLabelProvider.getInstanceFor(tableModel);
		GridTreeTableNode root = treeModel.getRoot();
		List<String> pathWithRoot = name.getPath();
		String expectedRootLabel = pathWithRoot.get(0);
		String actualRootLabel = labelProvider.getLabel(root, tableModel);
		if (!Utils.equals(actualRootLabel, expectedRootLabel)) {
			throw new NoSuchElementException("The label of the root node is expected to be '" + expectedRootLabel
				+ "' but it actually is '" + actualRootLabel + "'.");
		}
		GridTreeTableNode node = root;
		List<String> pathWithoutRoot = pathWithRoot.subList(1, pathWithRoot.size());
		for (String label : pathWithoutRoot) {
			SearchResult<GridTreeTableNode> searchResult = new SearchResult<>();
			for (GridTreeTableNode child : node.getChildren()) {
				if (Utils.equals(labelProvider.getLabel(child, tableModel), label)) {
					searchResult.add(child);
				}
			}
			node = searchResult.getSingleResult("Failed to resolve path to tree-grid node. Path: " + pathWithRoot
				+ ". Path entry that failed: '" + label + "'.");
		}
		return node;
	}

	private TableModel getTableModel(GridComponent grid) {
		return grid.getTableField(grid.getFormContext()).getTableModel();
	}

	private GridTreeTableModel getTree(GridComponent grid) {
		AbstractTreeGridBuilder<?>.TreeGridHandler handler = (AbstractTreeGridBuilder<?>.TreeGridHandler) grid._handler;
		return handler._treeModel;
	}

	@Override
	public Class<? extends GridNodeLabelName> getNameClass() {
		return GridNodeLabelName.class;
	}

	@Override
	public Class<GridTreeTableNode> getModelClass() {
		return GridTreeTableNode.class;
	}

}
