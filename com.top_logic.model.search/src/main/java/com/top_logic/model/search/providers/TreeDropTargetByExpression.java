/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.tree.dnd.BusinessObjectTreeDrop;
import com.top_logic.layout.tree.dnd.TreeDropEvent;
import com.top_logic.layout.tree.dnd.TreeDropTarget;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModel;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.providers.TreeDropActionOp.TreeDropAction;
import com.top_logic.util.model.ModelService;

/**
 * {@link TreeDropTarget} that can be completely configured using model queries.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public abstract class TreeDropTargetByExpression extends BusinessObjectTreeDrop {

	/** {@link ConfigurationItem} for the {@link TreeDropTargetByExpression}. */
	@Abstract
	public interface Config extends PolymorphicConfiguration<TreeDropTargetByExpression>, DropTargetByExpressionConfig {
		// Pure sum interface.
	}

	private final QueryExecutor _handleDrop;

	private final QueryExecutor _canDrop;

	private final List<PostCreateAction> _postCreateActions;

	private final boolean _inTransaction;

	LayoutComponent _contextComponent;

	/**
	 * Creates a {@link TreeDropTargetByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TreeDropTargetByExpression(InstantiationContext context, Config config) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLModel model = ModelService.getApplicationModel();

		_handleDrop = QueryExecutor.compile(kb, model, config.getHandleDrop());
		_canDrop = QueryExecutor.compile(kb, model, config.getCanDrop());
		_postCreateActions = TypedConfiguration.getInstanceList(context, config.getPostCreateActions());
		_inTransaction = config.getInTransaction();

		context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class, component -> {
			_contextComponent = component;
		});
	}

	@Override
	public void handleDrop(TreeDropEvent event, Collection<?> droppedObjects) {
		if (ScriptingRecorder.isRecordingActive()) {
			ScriptingRecorder.recordAction(() -> record(event));
		}

		handleDrop(droppedObjects, getDropArguments(event));
	}

	/**
	 * Executes the drop with the given arguments.
	 */
	public void handleDrop(Collection<?> droppedObjects, Args dropArguments) {
		Object createdObject;

		if (_inTransaction) {
			createdObject = KBUtils.inTransaction(() -> _handleDrop.executeWith(Args.cons(droppedObjects, dropArguments)));
		} else {
			createdObject = _handleDrop.executeWith(Args.cons(droppedObjects, dropArguments));
		}

		/* Ensure that components have received the model created event, before the new object is
		 * used in explicit communication with the components. Otherwise, selection would not work,
		 * if the new object was not included into component models. See also Ticket #669. */
		_contextComponent.getMainLayout().processGlobalEvents();
		_postCreateActions.forEach(action -> action.handleNew(_contextComponent, createdObject));
	}

	@Override
	public boolean canDrop(TreeDropEvent event, Collection<?> draggedObjects) {
		Args dropArguments = getDropArguments(event);

		return canDrop(draggedObjects, dropArguments);
	}

	/** Whether the business logic allows the drop with the given arguments. */
	public boolean canDrop(Collection<?> draggedObjects, Args dropArguments) {
		return SearchExpression.isTrue(_canDrop.executeWith(Args.cons(draggedObjects, dropArguments)));
	}

	/**
	 * Computes the reference argument(s) for the {@link Config#getHandleDrop() drop handler}.
	 */
	protected abstract Args getDropArguments(TreeDropEvent event);

	/** Record the drop for the scripting. */
	protected abstract TreeDropAction record(TreeDropEvent event);

}
