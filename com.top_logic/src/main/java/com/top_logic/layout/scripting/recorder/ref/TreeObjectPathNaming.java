/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.scripting.ScriptingUtil;
import com.top_logic.layout.scripting.recorder.ref.ui.BreadcrumbStrings;
import com.top_logic.layout.scripting.recorder.specialcases.ValueScopeFactory;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.table.tree.TreeTableData;
import com.top_logic.layout.tree.model.IndexedTreeTableModel;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TLTreeNode;

/**
 * {@link ModelNamingScheme} to locate business objects in a tree table by a path of node labels to
 * the root of his tree.
 * 
 * <p>
 * The tree model has to be an {@link IndexedTreeTableModel} to compute the corresponding node
 * holding the business object for which a {@link ModelName} should be created.
 * </p>
 * 
 * @see IndexedTreeTableModel
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TreeObjectPathNaming extends ModelNamingScheme<TreeTableData, Object, TreeObjectPathNaming.Name> {

	/**
	 * {@link ModelName} for a object in a tree by using the path to the root of this tree.
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	public interface Name extends ModelName {

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
	 * Creates a {@link TreeObjectPathNaming}.
	 */
	@SuppressWarnings({})
	public TreeObjectPathNaming() {
		super(Object.class, TreeObjectPathNaming.Name.class, TreeTableData.class);
	}

	@Override
	protected Maybe<Name> buildName(TreeTableData valueContext, Object model) {
		TLTreeModel<?> treeModel = valueContext.getTree();

		if (treeModel instanceof IndexedTreeTableModel<?>) {
			Collection<?> nodes = ((IndexedTreeTableModel<?>) treeModel).getIndex().getNodes(model);

			if (nodes.size() == 1) {
				Object node = CollectionUtils.extractSingleton(nodes);

				Maybe<List<String>> treeLabelPath = getTreeLabelPath(valueContext, (TLTreeNode<?>) node);

				if (treeLabelPath.hasValue()) {
					return Maybe.some(createTreeLabelPathName(treeLabelPath));
				}
			}

			return Maybe.none();
		}

		return Maybe.none();
	}

	private Maybe<List<String>> getTreeLabelPath(TreeTableData valueContext, TLTreeNode<?> model) {
		return ScriptingUtil.createTreeLabelPath(model, valueContext.getTree(), getLabelProvider(valueContext));
	}

	private TreeObjectPathNaming.Name createTreeLabelPathName(Maybe<List<String>> treeLabelPath) {
		TreeObjectPathNaming.Name treeModelName = TypedConfiguration.newConfigItem(TreeObjectPathNaming.Name.class);

		treeModelName.setPath(treeLabelPath.get());

		return treeModelName;
	}

	@Override
	public Object locateModel(ActionContext context, TreeTableData valueContext, Name name) {
		return ((TLTreeNode<?>) ScriptingUtil.findNodeByLabelPath(name.getPath(), valueContext.getTree(),
			getLabelProvider(valueContext))).getBusinessObject();
	}

	private LabelProvider getLabelProvider(TreeTableData valueContext) {
		return ValueScopeFactory.getLabelProvider(valueContext.getTableModel());
	}

}
