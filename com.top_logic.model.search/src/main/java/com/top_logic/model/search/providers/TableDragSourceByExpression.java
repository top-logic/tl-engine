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
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.dnd.TableDragSource;
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
	public interface Config extends PolymorphicConfiguration<TableDragSourceByExpression> {

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
		 * Whether dragging from the given table is enabled.
		 */
		@Name(DRAG_ENABLED)
		@BooleanDefault(true)
		@Hidden
		boolean getDragEnabled();

	}

	private QueryExecutor _canDrag;

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
	public TableDragSourceByExpression(InstantiationContext context, Config config) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLModel model = ModelService.getApplicationModel();

		_canDrag = QueryExecutor.compile(kb, model, config.canDrag());
		_dragEnabled = config.getDragEnabled();
	}

	@Override
	public boolean dragEnabled(TableData data, Object row) {
		return (boolean) _canDrag.execute(row);
	}

	@Override
	public boolean dragEnabled(TableData data) {
		return _dragEnabled;
	}

	@Override
	public Object getDragObject(TableData tableData, int row) {
		return tableData.getViewModel().getRowObject(row);
	}

}
