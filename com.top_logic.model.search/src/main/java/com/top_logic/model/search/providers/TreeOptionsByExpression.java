/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.AttributedValueFilter;
import com.top_logic.element.meta.kbbased.filtergen.Generator;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.model.utility.TreeOptionModel;
import com.top_logic.layout.tree.model.BusinessObjectTreeModel;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.trace.ScriptTracer;
import com.top_logic.model.search.providers.OptionsByExpression.ScriptObservingOptions;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link Generator} that can be configured dynamically using {@link Expr search expressions} for
 * providing {@link TreeOptionModel tree like options}.
 * 
 * @see TreeOptionModel
 * @see Generator
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@InApp
public class TreeOptionsByExpression extends AbstractConfiguredInstance<TreeOptionsByExpression.Config<?>>
		implements Generator {

	/**
	 * Configuration options for {@link TreeOptionsByExpression}.
	 */
	@TagName("treeoptions-by-expression")
	@DisplayOrder({
		Config.ROOT_NODE,
		Config.CHILDREN,
		Config.PARENTS,
		Config.SELECTION_FILTER,
		Config.SHOW_ROOT,
		Config.FINITE,
	})
	public interface Config<I extends TreeOptionsByExpression> extends PolymorphicConfiguration<I> {

		/**
		 * Configuration property label for {@link #getRoot()}
		 */
		String ROOT_NODE = "rootNode";

		/**
		 * Configuration property label for {@link #getChildren()}
		 */
		String CHILDREN = "children";

		/**
		 * Configuration property label for {@link #getParents()}
		 */
		String PARENTS = "parents";

		/**
		 * Configuration property label for {@link #getSelectionFilter()}
		 */
		String SELECTION_FILTER = "selection-filter";

		/**
		 * @see #getShowRoot()
		 */
		String SHOW_ROOT = "showRoot";

		/**
		 * @see #getFinite()
		 */
		String FINITE = "finite";

		/**
		 * Function resolving the root node of this tree.
		 * 
		 * <p>
		 * The function receives the current object being edited or created as first argument.
		 * </p>
		 */
		@Name(ROOT_NODE)
		@Mandatory
		Expr getRoot();

		/**
		 * Function resolving the children of a given object in this tree.
		 * 
		 * <p>
		 * The function receives a tree node as first argument and the current object being edited
		 * or created as second argument. As result, a list of children nodes of the given tree node
		 * is expected. A result of <code>null</code> or the empty list means that the given node is
		 * a leaf node.
		 * </p>
		 */
		@Name(CHILDREN)
		@Mandatory
		Expr getChildren();

		/**
		 * Function resolving the parent node(s) in this tree.
		 * 
		 * <p>
		 * The function receives a tree node as first argument and the current object being edited
		 * or created as second argument. The function must return a list of those nodes in this
		 * tree for which the given object is in the collection computed by {@link #getChildren()}.
		 * </p>
		 */
		@Name(PARENTS)
		@Mandatory
		Expr getParents();

		/**
		 * Filter, which determines, which nodes are selectable.
		 * 
		 * <p>
		 * The function receives a tree node as first argument and the current object being edited
		 * or created as second argument. The function returns true if the given node can be
		 * selected, otherwise false.
		 * </p>
		 */
		@Name(SELECTION_FILTER)
		Expr getSelectionFilter();

		/**
		 * Whether to show the root node.
		 */
		@Name(SHOW_ROOT)
		@BooleanDefault(true)
		boolean getShowRoot();

		/**
		 * Whether to created tree model will be finite.
		 */
		@Name(FINITE)
		@BooleanDefault(false)
		boolean getFinite();

	}

	ScriptTracer _root;

	ScriptTracer _children;

	ScriptTracer _parents;

	ScriptTracer _selectionFilter;

	private final boolean _showRoot;

	private final boolean _finite;

	/**
	 * Creates a {@link TreeOptionsByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TreeOptionsByExpression(InstantiationContext context, Config<?> config) {
		super(context, config);

		_root = ScriptTracer.compile(config.getRoot());
		_children = ScriptTracer.compile(config.getChildren());
		_parents = ScriptTracer.compile(config.getParents());
		_selectionFilter = ScriptTracer.compileOptional(config.getSelectionFilter());
		_showRoot = config.getShowRoot();
		_finite = config.getFinite();
	}

	@Override
	public OptionModel<?> generate(EditContext editContext) {
		return new ScriptOptionTree(editContext);
	}

	private class ScriptOptionTree extends ScriptObservingOptions implements TreeOptionModel<Object> {

		private Filter<Object> _optionsFilter;

		private BusinessObjectTreeModel<Object> _treeModel;

		/**
		 * Creates a {@link ScriptOptionTree}.
		 */
		public ScriptOptionTree(EditContext editContext) {
			super(editContext.getOverlay());
			_optionsFilter = createSelectionFilter(editContext);
		}

		@Override
		public TLTreeModel<Object> getBaseModel() {
			if (_treeModel == null) {
				_treeModel = createTreeModel(getObject());
			}
			return _treeModel;
		}

		@Override
		public Filter<? super Object> getSelectableOptionsFilter() {
			return _optionsFilter;
		}

		@Override
		public void setSelectableOptionsFilter(Filter<? super Object> selectableOptionsFilter) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean showRootNode() {
			return _showRoot;
		}

		@Override
		public void setShowRoot(boolean showRoot) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void reset() {
			_treeModel = null;
		}

		private Filter<? super Object> createSelectionFilter(EditContext editContext) {
			List<Filter<Object>> filters = new ArrayList<>();

			addConfiguredSelectionFilter(filters, editContext.getOverlay());
			addInstanceFilter(filters, editContext);
			addAttributeConstraintFilter(filters, editContext);

			return FilterFactory.and(filters);
		}

		private void addConfiguredSelectionFilter(List<Filter<Object>> filters, TLObject currentObject) {
			if (_selectionFilter != null) {
				filters.add(node -> SearchExpression.asBoolean(_selectionFilter.execute(this, node, currentObject)));
			}
		}

		private void addInstanceFilter(List<Filter<Object>> filters, EditContext context) {
			filters.add(node -> TLModelUtil.isCompatibleInstance(context.getValueType(), node));
		}

		@SuppressWarnings("deprecation")
		private void addAttributeConstraintFilter(List<Filter<Object>> filters, EditContext context) {
			AttributedValueFilter filter = context.getFilter();

			if (filter != null) {
				filters.add(node -> filter.accept(node, context));
			}
		}

		private BusinessObjectTreeModel<Object> createTreeModel(TLObject currentObject) {
			Function<Object, Collection<?>> childrenByNode = createChildrenByNode(currentObject);
			Function<Object, Collection<?>> parentsByNode = createParentsByNode(currentObject);

			return new BusinessObjectTreeModel<>(
				_root.execute(this, currentObject), childrenByNode, parentsByNode, _finite);
		}

		private Function<Object, Collection<?>> createParentsByNode(TLObject currentObject) {
			return node -> SearchExpression.asCollection(_parents.execute(this, node, currentObject));
		}

		private Function<Object, Collection<?>> createChildrenByNode(TLObject currentObject) {
			return node -> SearchExpression.asCollection(_children.execute(this, node, currentObject));
		}
	}
		
}
