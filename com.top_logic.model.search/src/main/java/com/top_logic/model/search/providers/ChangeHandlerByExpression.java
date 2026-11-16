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
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.knowledge.service.CommitActions;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLChangeHandler;
import com.top_logic.model.annotate.TLChangeHandlers;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link TLChangeHandler} that executes a TL-Script expression within the commit of a change of
 * the annotated attribute.
 *
 * @see TLChangeHandlers
 */
@InApp
@Label("Change handler with TL-Script")
public class ChangeHandlerByExpression extends AbstractConfiguredInstance<ChangeHandlerByExpression.Config>
		implements TLChangeHandler {

	/**
	 * Configuration options for {@link ChangeHandlerByExpression}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<ChangeHandlerByExpression> {

		/**
		 * Tag name of a {@link ChangeHandlerByExpression} in {@link TLChangeHandlers}.
		 */
		String TAG_NAME = "script";

		/**
		 * Configuration name for the property {@link #getOperation()}.
		 */
		String OPERATION = "operation";

		/**
		 * TL-Script that is executed when a transaction that changes the value of the annotated
		 * attribute of an existing object is committed.
		 *
		 * <p>
		 * The operation gets four arguments: the changed object, the new value, the old value (the
		 * value before the transaction), and the changed attribute. Its result is ignored.
		 * </p>
		 *
		 * <p>
		 * The operation runs within the commit, before the commit checks (e.g. the model
		 * constraints). Changes it makes (e.g. with <code>$object.set(`attribute`, value)</code>)
		 * become part of the same commit and are rolled back with it, if the commit fails. They do
		 * not trigger change handlers again. The operation is not executed for an object that is
		 * created or deleted in the committed transaction.
		 * </p>
		 *
		 * <p>
		 * The operation runs without security checks: It may read and modify any object,
		 * independent of the rights of the user committing the transaction.
		 * </p>
		 *
		 * <p>
		 * A message shown with <code>info()</code> appears immediately, also if the commit fails
		 * afterwards.
		 * </p>
		 */
		@Name(OPERATION)
		@Mandatory
		Expr getOperation();

	}

	private final QueryExecutor _operation;

	/**
	 * Creates a {@link ChangeHandlerByExpression} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public ChangeHandlerByExpression(InstantiationContext context, Config config) {
		super(context, config);
		_operation = QueryExecutor.compile(config.getOperation());
		// Consistency logic declared in the model, independent of the committing user.
		_operation.disableSecurity();
	}

	@Override
	public void changed(TLObject object, TLStructuredTypePart attribute, Object oldValue, Object newValue,
			CommitActions commitActions) {
		_operation.execute(object, newValue, oldValue, attribute);
	}

}
