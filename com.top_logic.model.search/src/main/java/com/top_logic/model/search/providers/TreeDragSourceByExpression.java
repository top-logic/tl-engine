/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.dnd.DropEvent;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.dnd.TableDragSource;
import com.top_logic.layout.tree.TreeData;
import com.top_logic.layout.tree.dnd.TreeDragSource;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModel;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.model.ModelService;

/**
 * {@link TreeDragSource} that can be completely configured using model queries.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@InApp(classifiers = { "tree" })
public class TreeDragSourceByExpression implements TreeDragSource {

	/** {@link ConfigurationItem} for the {@link TreeDragSourceByExpression}. */
	public interface Config extends PolymorphicConfiguration<TreeDragSourceByExpression> {

		/**
		 * Name of {@link #canDrag()}.
		 */
		public static final String CAN_DRAG = "canDrag";

		/**
		 * Name of {@link #getDragEnabled()}.
		 */
		public static final String DRAG_ENABLED = "dragEnabled";

		/**
		 * Function checking if a given tree node can be dragged.
		 * 
		 * <p>
		 * The function receives a tree node.
		 * </p>
		 */
		@Name(CAN_DRAG)
		@ItemDefault(Expr.True.class)
		Expr canDrag();

		/**
		 * Whether dragging from the given tree is enabled.
		 */
		@Name(DRAG_ENABLED)
		@BooleanDefault(true)
		@Hidden
		boolean getDragEnabled();

		/**
		 * Function creating the drag source model.
		 * 
		 * <p>
		 * The drag source model can be retrieved at the drop location together with the dragged
		 * objects.
		 * </p>
		 * 
		 * <p>
		 * The function receives the component's model as single argument.
		 * </p>
		 * 
		 * <pre>
		 * <code>model -> ...</code>
		 * </pre>
		 * 
		 * <p>
		 * If not given, the component's model is used as drag source model.
		 * </p>
		 * 
		 * @see TableDragSource#getDragSourceModel(TableData)
		 * @see DropEvent#getSource()
		 */
		Expr getDragSourceModel();
	}

	private LayoutComponent _contextComponent;

	private final QueryExecutor _canDrag;

	private final QueryExecutor _sourceModel;

	private final boolean _dragEnabled;

	/**
	 * Creates a {@link TreeDropTargetByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TreeDragSourceByExpression(InstantiationContext context, Config config) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLModel model = ModelService.getApplicationModel();

		context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class, component -> {
			_contextComponent = component;
		});

		_canDrag = QueryExecutor.compile(kb, model, config.canDrag());
		_sourceModel = QueryExecutor.compileOptional(config.getDragSourceModel());
		_dragEnabled = config.getDragEnabled();
	}

	@Override
	public boolean canDrag(TreeData data, Object node) {
		return (boolean) _canDrag.execute(((TLTreeNode<?>) node).getBusinessObject());
	}

	@Override
	public boolean dragEnabled(TreeData data) {
		return _dragEnabled;
	}

	@Override
	public Object getDragSourceModel(TreeData treeData) {
		Object model = _contextComponent.getModel();

		if (_sourceModel == null) {
			return model;
		} else {
			return _sourceModel.execute(model);
		}
	}

}
