/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.NoTransaction;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.component.WithCommitMessage;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.form.component.WithPostCreateActions;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandler.ConfirmConfig.VisibleIf;

/**
 * Execute a script block optionally in a transaction context.
 */
@InApp
public class ScriptExecution extends AbstractConfiguredInstance<ScriptExecution.Config<?>> implements PostCreateAction {

	/**
	 * Configuration options for {@link CommandHandlerByExpression}.
	 */
	@DisplayOrder({
		Config.ARGUMENTS,
		Config.OPERATION,
		Config.TRANSACTION,
		Config.COMMIT_MESSAGE,
		Config.POST_CREATE_ACTIONS,
	})
	@TagName("execute-script")
	public interface Config<I extends ScriptExecution>
			extends PolymorphicConfiguration<I>, WithPostCreateActions.Config, WithCommitMessage,
			com.top_logic.model.search.providers.WithTransaction.Config {

		/**
		 * @see #getArguments()
		 */
		String ARGUMENTS = "arguments";

		/**
		 * @see #getOperation()
		 */
		String OPERATION = "operation";

		/**
		 * Additional arguments to the {@link #getOperation()}.
		 * 
		 * <p>
		 * Note: The commands target model is always passed automatically as first argument.
		 * </p>
		 */
		@Name(ARGUMENTS)
		@Label("Additional arguments")
		List<ModelSpec> getArguments();

		/**
		 * The operation to perform.
		 * 
		 * <p>
		 * The expression is expected to be a function taking the input as first argument. If
		 * additional {@link #getArguments()} are configured, these are additional inputs to the
		 * operation. The function may return an arbitrary result. The result produced is passed to
		 * potential {@link #getPostCreateActions() follow-up actions} configured.
		 * </p>
		 */
		@Name(OPERATION)
		Expr getOperation();

		@Override
		@DynamicMode(fun = VisibleIf.class, args = @Ref(TRANSACTION))
		ResKey1 getCommitMessage();
	}

	private final QueryExecutor _operation;

	private final List<PostCreateAction> _actions;

	private CommandHandler _command;

	private List<ChannelLinking> _arguments;

	/**
	 * Creates a {@link CommandHandlerByExpression}.
	 */
	public ScriptExecution(InstantiationContext context, Config<?> config) {
		super(context, config);

		Expr operation = config.getOperation();
		_operation = QueryExecutor.compileOptional(operation);
		_actions = TypedConfigUtil.createInstanceList(config.getPostCreateActions());

		_arguments = TypedConfigUtil.createInstanceList(config.getArguments());
		Collections.reverse(_arguments);

		context.resolveReference(InstantiationContext.OUTER, CommandHandler.class, c -> _command = c);
	}

	@Override
	public void handleNew(LayoutComponent component, Object model) {
		Object result;
		try (Transaction tx = beginTransaction(component, model)) {
			Args args = Args.none();
			for (ChannelLinking additional : _arguments) {
				args = Args.cons(additional.eval(component), args);
			}
			args = Args.cons(model, args);

			result = _operation.executeWith(args);

			tx.commit();
		}

		WithPostCreateActions.processCreateActions(_actions, component, result);
	}

	private Transaction beginTransaction(LayoutComponent component, Object model) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Config<?> config = getConfig();
		if (config.isInTransaction()) {
			ResKey message = config.buildCommandMessage(component, _command, model);
			return kb.beginTransaction(message);
		} else {
			return new NoTransaction(kb);
		}
	}

}
