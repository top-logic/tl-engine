/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.col.search.SearchResult;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.util.Utils;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.ui.BreadcrumbStrings;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.AbstractTreeTableModel.AbstractTreeTableNode;
import com.top_logic.layout.tree.model.TLTreeModelUtil;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * A {@link ModelNamingScheme} for identifying a row in the tree component by the label path to it.
 */
public abstract class TreeTableNodeLabelNaming<N extends AbstractTreeTableNode<N>, NAME extends TreeTableNodeLabelNaming.Name, C>
		extends AbstractModelNamingScheme<N, NAME> {

	/**
	 * {@link ModelName} for the {@link TreeTableNodeLabelNaming}.
	 */
	@Abstract
	public interface Name extends ModelName {

		/**
		 * Reference to the tree table.
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

	private enum ColumnLabel {

		NAME_COLUMN {

			@Override
			String getLabel(Object node, TableModel tableModel) {
				return getCellLabel(node, tableModel, NAME_COLUMN_NAME);
			}

		},
		OTHER_COLUMN {

			@Override
			String getLabel(Object node, TableModel tableModel) {
				/* This might not be the label the user actually sees, but it's good enough for the
				 * scripting for now, as a fallback for weird tree-grids without self- and
				 * name-column. */
				return MetaLabelProvider.INSTANCE.getLabel(node);
			}

		};

		static ColumnLabel getInstanceFor(TableModel tableModel) {
			if (hasColumn(tableModel, NAME_COLUMN_NAME)) {
				return NAME_COLUMN;
			}
			return OTHER_COLUMN;
		}

		abstract String getLabel(Object node, TableModel tableModel);

		static boolean hasColumn(TableModel tableModel, String columnName) {
			return tableModel.getColumnNames().contains(columnName);
		}

		@SuppressWarnings({ "unchecked" })
		static String getCellLabel(Object node, TableModel tableModel, String columnName) {
			ColumnConfiguration columnConfiguration = tableModel.getColumnDescription(columnName);
			Object accessorValue = tableModel.getValueAt(node, columnName);
			Object rawOrFieldValue = columnConfiguration.getSortKeyProvider().map(accessorValue);
			return columnConfiguration.getFullTextProvider().getLabel(rawOrFieldValue);
		}

	}

	static final String NAME_COLUMN_NAME = AbstractWrapper.NAME_ATTRIBUTE;

	/**
	 * Creates a new {@link TreeTableNodeLabelNaming}.
	 * 
	 * @param nodeType
	 *        Value of {@link #getModelClass()}.
	 * @param nameType
	 *        Value of {@link #getNameClass()}.
	 */
	protected TreeTableNodeLabelNaming(Class<N> nodeType, Class<NAME> nameType) {
		super(nodeType, nameType);
	}

	@Override
	protected boolean isCompatibleModel(N node) {
		C owner = getOwner(node);
		if (owner == null) {
			return false;
		}
		Maybe<? extends ModelName> modelName = ModelResolver.buildModelNameIfAvailable(owner);
		if (!modelName.hasValue()) {
			return false;
		}
		Collection<String> labelPath = getStringPathFromRoot(node, owner);
		return !(labelPath.contains(null) || labelPath.contains(""));
	}

	@Override
	protected void initName(NAME name, N node) {
		C owner = getOwner(node);
		name.setComponent(ModelResolver.buildModelName(owner));
		name.setPath(getStringPathFromRoot(node, owner));
	}

	private List<String> getStringPathFromRoot(N node, C owner) {
		List<N> nodePath = getNodePathFromRoot(node);
		List<String> labelPath = new ArrayList<>();
		TableModel tableModel = getTableModel(owner);
		ColumnLabel labelProvider =
			ColumnLabel.getInstanceFor(tableModel);
		for (N ancestor : nodePath) {
			labelPath.add(labelProvider.getLabel(ancestor, tableModel));
		}
		return labelPath;
	}

	private List<N> getNodePathFromRoot(N node) {
		List<N> path = TLTreeModelUtil.createPathToRoot(node);
		Collections.reverse(path);
		return path;
	}

	/**
	 * Determines the {@link LayoutComponent owner} for the given node, that can deliver the
	 * corresponding {@link TreeTableData}.
	 * 
	 * @return <code>null</code>, when no owner could be determined.
	 * 
	 * @see #getTreeTable(Object)
	 */
	protected abstract C getOwner(N node);

	/**
	 * Determines the {@link TreeTableData} for owner component.
	 * 
	 * @see #getOwner(AbstractTreeTableNode)
	 */
	protected abstract TreeTableData getTreeTable(C owner);

	@Override
	public N locateModel(ActionContext context, Name name) {
		@SuppressWarnings("unchecked")
		C component = (C) context.resolve(name.getComponent());
		TableModel tableModel = getTableModel(component);
		AbstractTreeTableModel<N> treeModel = getTree(component);
		ColumnLabel labelProvider = ColumnLabel.getInstanceFor(tableModel);
		N root = treeModel.getRoot();
		List<String> pathWithRoot = name.getPath();
		String expectedRootLabel = pathWithRoot.get(0);
		String actualRootLabel = labelProvider.getLabel(root, tableModel);
		if (!Utils.equals(actualRootLabel, expectedRootLabel)) {
			throw new NoSuchElementException("The label of the root node is expected to be '" + expectedRootLabel
				+ "' but it actually is '" + actualRootLabel + "'.");
		}
		N node = root;
		List<String> pathWithoutRoot = pathWithRoot.subList(1, pathWithRoot.size());
		for (String label : pathWithoutRoot) {
			SearchResult<N> searchResult = new SearchResult<>();
			for (N child : node.getChildren()) {
				if (Utils.equals(labelProvider.getLabel(child, tableModel), label)) {
					searchResult.add(child);
				}
			}
			node = searchResult.getSingleResult("Failed to resolve path to tree-grid node. Path: " + pathWithRoot
				+ ". Path entry that failed: '" + label + "'.");
		}
		return node;
	}

	private TableModel getTableModel(C owner) {
		return getTreeTable(owner).getTableModel();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private AbstractTreeTableModel<N> getTree(C owner) {
		return (AbstractTreeTableModel) getTreeTable(owner).getTree();
	}

}
