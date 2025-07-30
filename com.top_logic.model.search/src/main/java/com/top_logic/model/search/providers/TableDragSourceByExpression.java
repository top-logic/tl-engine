/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.Collection;
import java.util.stream.Collectors;

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
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModel;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.model.ModelService;

/**
 * {@link TableDragSource} that can be completely configured using model queries.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@InApp(classifiers = { "table" })
public class TableDragSourceByExpression implements TableDragSource {

	/** {@link ConfigurationItem} for the {@link TableDragSourceByExpression}. */
	public interface Config<I extends TableDragSourceByExpression> extends PolymorphicConfiguration<I> {

		/**
		 * Name of {@link #canDrag()}.
		 */
		public static final String CAN_DRAG = "canDrag";

		/**
		 * Name of {@link #getDragEnabled()}.
		 */
		public static final String DRAG_ENABLED = "dragEnabled";

		/**
		 * Function checking if a given table row can be dragged.
		 * 
		 * <p>
		 * The function receives a table row.
		 * </p>
		 */
		@Name(CAN_DRAG)
		@ItemDefault(Expr.True.class)
		Expr canDrag();

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

		/**
		 * Whether dragging from the given table is enabled.
		 */
		@Name(DRAG_ENABLED)
		@BooleanDefault(true)
		@Hidden
		boolean getDragEnabled();

	}

	private LayoutComponent _contextComponent;

	private QueryExecutor _canDrag;

	private QueryExecutor _sourceModel;

	private boolean _dragEnabled;

	/**
	 * Creates a {@link TableDragSourceByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TableDragSourceByExpression(InstantiationContext context, Config<?> config) {
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
	public boolean dragEnabled(TableData data, Object rowObject) {
		return (boolean) _canDrag.execute(unwrap(rowObject));
	}

	@Override
	public boolean dragEnabled(TableData data) {
		return _dragEnabled;
	}

	@Override
	public final Object getDragObject(TableData tableData, int row) {
		return unwrap(tableData.getViewModel().getRowObject(row));
	}

	@Override
	public Collection<?> getDragSelection(TableData tableData, int row) {
		return tableData.getSelectionModel().getSelection().stream().map(this::unwrap).collect(Collectors.toSet());
	}

	@Override
	public Object getDragSourceModel(TableData tableData) {
		Object model = _contextComponent.getModel();

		if (_sourceModel == null) {
			return model;
		} else {
			return _sourceModel.execute(model);
		}
	}

	/**
	 * Hook for sub-classes to unwrap technical objects and hide them from the script layer.
	 */
	protected Object unwrap(Object rowObject) {
		return rowObject;
	}
}
