/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.form;

import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormTree;
import com.top_logic.layout.scripting.ScriptingUtil;
import com.top_logic.layout.scripting.recorder.ref.ui.BreadcrumbStrings;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TreeUIModel;

/**
 * {@link AbstractFieldMatcher} to identify form groups within {@link FormTree}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TreeFieldMatcher extends AbstractFieldMatcher<TreeFieldMatcher.Config> {

	/**
	 * Configuration options for {@link TreeFieldMatcher}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<TreeFieldMatcher> {

		/**
		 * Short-cut tag name for {@link TreeFieldMatcher}s.
		 */
		String TAG_NAME = "tree-member";

		/**
		 * The path from root to a node in the tree.
		 * 
		 * <p>
		 * The path does not contain the root node.
		 * </p>
		 */
		@Format(BreadcrumbStrings.class)
		List<String> getPath();

		/**
		 * Setter for {@link #getPath()}.
		 */
		void setPath(List<String> treePath);

	}

	/**
	 * Creates a new {@link TreeFieldMatcher} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link TreeFieldMatcher}.
	 */
	public TreeFieldMatcher(InstantiationContext context, Config config) {
		super(config);
	}

	TreeFieldMatcher(Config config) {
		super(config);
	}

	@Override
	public Filter<? super FormMember> createFilter(final ActionContext context) {
		return new Filter<>() {

			@Override
			public boolean accept(FormMember anObject) {
				FormContainer parent = anObject.getParent();
				if (!(parent instanceof FormTree)) {
					return false;
				}
				FormTree parentTree = (FormTree) parent;
				TreeUIModel<?> treeModel = parentTree.getTreeApplicationModel();
				ResourceProvider resourceProvider = ScriptingUtil.getResourceProvider(parentTree);

				Object foundNode;
				try {
					// Root is not contained in path.
					// See ScriptingUtil#createTreeLabelPath(Object, TLTreeModel, LabelProvider)
					foundNode =
						ScriptingUtil.findNodeByLabelPath(getConfig().getPath(), treeModel, resourceProvider, false);
				} catch (AssertionError err) {
					// path is not a valid path in the parent form tree. This is not the represented
					// tree.
					return false;
				}
				return foundNode == parentTree.getNode(anObject);
			}
		};
	}

	/**
	 * {@link FieldAnalyzer} creating {@link FieldLabelMatcher}s.
	 */
	public static class Analyzer implements FieldAnalyzer {

		@Override
		public FieldMatcher getMatcher(FormMember field) {
			FormContainer parent = field.getParent();
			if (!(parent instanceof FormTree)) {
				return null;
			}
			FormTree parentTree = (FormTree) parent;
			Object currentNode = parentTree.getNode(field);
			TLTreeModel<?> treeModel = parentTree.getTreeApplicationModel();
			ResourceProvider resourceProvider = ScriptingUtil.getResourceProvider(parentTree);
			Maybe<List<String>> treePath = ScriptingUtil.createTreeLabelPath(currentNode, treeModel, resourceProvider);
			if (!treePath.hasValue()) {
				return null;
			}
			return new TreeFieldMatcher(config(treePath.get()));
		}

		private static Config config(List<String> path) {
			Config result = TypedConfiguration.newConfigItem(Config.class);
			result.setPath(path);
			return result;
		}
	}


}
