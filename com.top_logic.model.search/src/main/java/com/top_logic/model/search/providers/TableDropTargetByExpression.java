/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.table.dnd.BusinessObjectTableDrop;
import com.top_logic.layout.table.dnd.TableDropTarget;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModel;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.model.ModelService;

/**
 * {@link TableDropTarget} that can be completely configured using model queries.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@InApp(classifiers = { "table" })
public class TableDropTargetByExpression extends BusinessObjectTableDrop {

	/** {@link ConfigurationItem} for the {@link TableDropTargetByExpression}. */
	public interface Config
			extends PolymorphicConfiguration<TableDropTargetByExpression>, DropTargetByExpressionConfig {

		/**
		 * Name of {@link #getDropType()}.
		 */
		public static final String DROP_TYPE = "dropType";

		/**
		 * Specifies if the drop occurs ordered, i.e. before or after, or on the referenced row
		 * itself.
		 */
		@Name(DROP_TYPE)
		DropType getDropType();

		/**
		 * Function executing a drop in the context of a referenced row.
		 * 
		 * <p>
		 * The function receives the dragged elements as first argument and a referenced row as
		 * second argument.
		 * </p>
		 * 
		 * <p>
		 * Depending on the {@link #getDropType()} setting, the drop operation happens either just
		 * before the referenced row (or at the end of all rows in case of a <code>null</code>
		 * referenced row) in case of an ordered drop, or on the referenced row, otherwise.
		 * </p>
		 * 
		 * <p>
		 * The value returned from the function is passed to potential
		 * {@link #getPostCreateActions() post-drop actions}.
		 * </p>
		 */
		@Override
		Expr getHandleDrop();

		/**
		 * Function checking if a drop in the context of a referenced row can be performed.
		 * 
		 * <p>
		 * The function receives the dragged elements as first argument and a referenced row as
		 * second argument.
		 * </p>
		 * 
		 * <p>
		 * Depending on the {@link #getDropType()} setting, the drop operation happens either just
		 * before the referenced row (or at the end of all rows in case of a <code>null</code>
		 * referenced row) in case of an ordered drop, or on the referenced row, otherwise.
		 * </p>
		 * 
		 * <p>
		 * The function is expected to return a boolean value, <code>true</code> if the drop can be
		 * performed and <code>false</code> otherwise.
		 * </p>
		 */
		@Override
		Expr getCanDrop();

	}

	private final DropType _dropType;

	private final QueryExecutor _handleDrop;

	private final QueryExecutor _canDrop;

	private final List<PostCreateAction> _postCreateActions;

	private final boolean _inTransaction;

	LayoutComponent _contextComponent;

	/**
	 * Creates a {@link TableDropTargetByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TableDropTargetByExpression(InstantiationContext context, Config config) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLModel model = ModelService.getApplicationModel();

		_dropType = config.getDropType();
		_handleDrop = QueryExecutor.compile(kb, model, config.getHandleDrop());
		_canDrop = QueryExecutor.compile(kb, model, config.getCanDrop());
		_postCreateActions = TypedConfiguration.getInstanceList(context, config.getPostCreateActions());
		_inTransaction = config.getInTransaction();

		context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class, component -> {
			_contextComponent = component;
		});
	}

	@Override
	public DropType getDropType() {
		return _dropType;
	}

	@Override
	public void handleDrop(Collection<?> droppedObjects, Object referenceRow) {
		Object createdObject;

		if (_inTransaction) {
			createdObject = KBUtils.inTransaction(() -> _handleDrop.execute(droppedObjects, referenceRow));
		} else {
			createdObject = _handleDrop.execute(droppedObjects, referenceRow);
		}

		/* Ensure that components have received the model created event, before the new object is
		 * used in explicit communication with the components. Otherwise, selection would not work,
		 * if the new object was not included into component models. See also Ticket #669. */
		_contextComponent.getMainLayout().processGlobalEvents();
		_postCreateActions.forEach(action -> action.handleNew(_contextComponent, createdObject));
	}

	@Override
	public boolean canDrop(Collection<?> draggedObjects, Object referenceRow) {
		return SearchExpression.isTrue(_canDrop.execute(draggedObjects, referenceRow));
	}

}
