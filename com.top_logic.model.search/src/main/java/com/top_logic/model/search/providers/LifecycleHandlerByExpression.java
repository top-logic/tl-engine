/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.knowledge.service.CommitActions;
import com.top_logic.knowledge.service.event.Modification;
import com.top_logic.model.TLObject;
import com.top_logic.model.annotate.TLLifecycleHandler;
import com.top_logic.model.annotate.TLLifecycleHandlers;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * {@link TLLifecycleHandler} that executes TL-Script expressions when an object of the annotated
 * type is created or deleted.
 *
 * @see TLLifecycleHandlers
 */
@InApp
@Label("Lifecycle handler with TL-Script")
public class LifecycleHandlerByExpression extends AbstractConfiguredInstance<LifecycleHandlerByExpression.Config>
		implements TLLifecycleHandler {

	/**
	 * Configuration options for {@link LifecycleHandlerByExpression}.
	 */
	@TagName(Config.TAG_NAME)
	@DisplayOrder({
		Config.CREATED,
		Config.DELETING,
	})
	public interface Config extends PolymorphicConfiguration<LifecycleHandlerByExpression> {

		/**
		 * Tag name of a {@link LifecycleHandlerByExpression} in {@link TLLifecycleHandlers}.
		 */
		String TAG_NAME = "script";

		/**
		 * Configuration name for the property {@link #getCreated()}.
		 */
		String CREATED = "created";

		/**
		 * Configuration name for the property {@link #getDeleting()}.
		 */
		String DELETING = "deleting";

		/**
		 * TL-Script that is executed when an object of the annotated type is created.
		 *
		 * <p>
		 * The script gets the created object as its single argument. Its result is ignored.
		 * </p>
		 *
		 * <p>
		 * The script runs within the commit of the transaction that created the object, after the
		 * object initializers of all objects created in this transaction and before the commit
		 * checks (e.g. the model constraints). Changes it makes become part of the same commit and
		 * are rolled back with it, if the commit fails. Objects it creates are not reported to
		 * lifecycle handlers. An object that is deleted in the same transaction that created it
		 * is not reported.
		 * </p>
		 *
		 * <p>
		 * The script runs without security checks: It may read and modify any object, independent
		 * of the rights of the user committing the transaction.
		 * </p>
		 *
		 * <p>
		 * A message shown with <code>info()</code> appears immediately, also if the commit fails
		 * afterwards.
		 * </p>
		 */
		@Name(CREATED)
		Expr getCreated();

		/**
		 * TL-Script that is executed when an object of the annotated type is deleted.
		 *
		 * <p>
		 * The script gets the object being deleted as its single argument. It runs when the object
		 * is deleted, before the deletion is executed. At this time the transaction is locked: the
		 * object and its values can be read, but nothing may be modified.
		 * </p>
		 *
		 * <p>
		 * Changes are made by the function the script returns: It is called after the object and
		 * the objects deleted with it have been removed, still within the deleting transaction.
		 * Since the deleted object can no longer be read at that time, the function gets
		 * <code>null</code> as its single argument; everything it needs from the deleted object
		 * must be read by the script and be captured in the function, e.g.
		 * <code>item -> { owner = $item.get(`my.module:Item#owner`); x -> $owner.set(`my.module:Owner#itemDeleted`, true) }</code>.
		 * A script that makes no changes returns nothing. Any other result is an error.
		 * </p>
		 *
		 * <p>
		 * The script is not executed for an object that is deleted in the same transaction that
		 * created it. An object deleted by the returned function is reported to the lifecycle
		 * handlers of its type as well.
		 * </p>
		 *
		 * <p>
		 * The script and the function it returns run without security checks: They may read and
		 * modify any object, independent of the rights of the user deleting the object.
		 * </p>
		 */
		@Name(DELETING)
		Expr getDeleting();

	}

	private final QueryExecutor _created;

	private final QueryExecutor _deleting;

	/**
	 * Creates a {@link LifecycleHandlerByExpression} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public LifecycleHandlerByExpression(InstantiationContext context, Config config) {
		super(context, config);
		_created = unsecured(QueryExecutor.compileOptional(config.getCreated()));
		_deleting = unsecured(QueryExecutor.compileOptional(config.getDeleting()));
	}

	/**
	 * Switches off the security checks of the given script: It is consistency logic declared in the
	 * model, independent of the rights of the user committing the transaction.
	 */
	private static QueryExecutor unsecured(QueryExecutor executor) {
		if (executor != null) {
			executor.disableSecurity();
		}
		return executor;
	}

	@Override
	public void created(TLObject object, CommitActions commitActions) {
		if (_created == null) {
			return;
		}
		_created.execute(object);
	}

	@Override
	public Modification deleting(TLObject object, CommitActions commitActions) {
		if (_deleting == null) {
			return Modification.NONE;
		}
		Object result = _deleting.execute(object);
		if (result == null) {
			return Modification.NONE;
		}
		if (!(result instanceof SearchExpression function)) {
			throw new TopLogicException(
				I18NConstants.ERROR_DELETING_SCRIPT_RESULT_NOT_A_FUNCTION__OBJECT_RESULT.fill(object, result));
		}
		QueryExecutor modification =
			unsecured(QueryExecutor.executor(object.tKnowledgeBase(), ModelService.getApplicationModel(), function));
		return () -> modification.execute((Object) null);
	}

}
