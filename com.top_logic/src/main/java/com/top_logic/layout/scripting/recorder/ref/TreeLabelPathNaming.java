/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import java.util.List;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.scripting.ScriptingUtil;
import com.top_logic.layout.scripting.recorder.ref.ui.BreadcrumbStrings;
import com.top_logic.layout.scripting.recorder.specialcases.ValueScopeFactory;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.table.tree.TreeTableComponent;
import com.top_logic.layout.table.tree.TreeTableData;
import com.top_logic.layout.tree.model.TLTreeNode;

/**
 * A path of labels, used to identify a {@link TLTreeNode}.
 * 
 * @see TreeTableComponent
 * @see TreeTableData
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TreeLabelPathNaming
		extends ModelNamingScheme<TreeTableData, TLTreeNode<?>, TreeLabelPathNaming.TreeLabelPathName> {

	/**
	 * {@link ModelName} for a tree label.
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	public interface TreeLabelPathName extends ModelName {

		/**
		 * A path of labels, used to identify a tree node.
		 * 
		 * <p>
		 * Root is not contained in the path and represented by an empty path.
		 * </p>
		 */
		@Format(BreadcrumbStrings.class)
		List<String> getPath();

		/**
		 * @see #getPath()
		 */
		void setPath(List<String> value);

	}

	/**
	 * Creates a {@link TreeLabelPathNaming}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public TreeLabelPathNaming() {
		super((Class) TLTreeNode.class, TreeLabelPathName.class, TreeTableData.class);
	}

	@Override
	public TLTreeNode<?> locateModel(ActionContext context, TreeTableData valueContext, TreeLabelPathName name) {
		LabelProvider labelProvider = getLabelProvider(valueContext);

		return (TLTreeNode<?>) ScriptingUtil.findNodeByLabelPath(name.getPath(), valueContext.getTree(), labelProvider);
	}

	@Override
	protected Maybe<TreeLabelPathName> buildName(TreeTableData valueContext, TLTreeNode<?> model) {
		Maybe<List<String>> treeLabelPath = getTreeLabelPath(valueContext, model);

		if (treeLabelPath.hasValue()) {
			return Maybe.some(createTreeLabelPathName(treeLabelPath));
		}

		return Maybe.none();
	}

	private TreeLabelPathName createTreeLabelPathName(Maybe<List<String>> treeLabelPath) {
		TreeLabelPathName treeModelName = TypedConfiguration.newConfigItem(TreeLabelPathName.class);

		treeModelName.setPath(treeLabelPath.get());

		return treeModelName;
	}

	private Maybe<List<String>> getTreeLabelPath(TreeTableData valueContext, TLTreeNode<?> model) {
		return ScriptingUtil.createTreeLabelPath(model, valueContext.getTree(), getLabelProvider(valueContext));
	}

	private LabelProvider getLabelProvider(TreeTableData valueContext) {
		return ValueScopeFactory.getLabelProvider(valueContext.getTableModel());
	}

}
