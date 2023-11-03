/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.impl;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.component.Editor;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.editor.PlainEditor;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.dom.Expr.Define;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.TLScriptPropertyEditor;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ServiceMethodBuilder} that can be parameterized with a TL-Script expression to execute
 * upon request.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("method-by-expression")
public class ServiceMethodBuilderByExpression extends AbstractConfiguredInstance<ServiceMethodBuilderByExpression.Config>
		implements ServiceMethodBuilder {

	/**
	 * Configuration options for {@link ServiceMethodBuilderByExpression}.
	 */
	@DisplayOrder({
		Config.OPERATION,
		Config.TRANSACTION
	})
	@TagName("method-by-expression")
	public interface Config extends PolymorphicConfiguration<ServiceMethodBuilderByExpression> {

		/**
		 * @see #getOperation()
		 */
		String OPERATION = "operation";

		/**
		 * @see #isTransaction()
		 */
		String TRANSACTION = "transaction";

		/**
		 * The operation to execute upon request.
		 * 
		 * <p>
		 * Expected is an expression that creates a value that can be serialized as JSON value.
		 * </p>
		 * 
		 * <p>
		 * The expression has access to the implicit parameters defined for this service method.
		 * </p>
		 * 
		 * @implNote Note that {@link PlainEditor} instead of the default {@link Editor} for
		 *           {@link Expr} ({@link TLScriptPropertyEditor}) is used here to avoid checking
		 *           the expression for validity: When executing the query it is enhanced by adding
		 *           the defined parameters. These parameters are not available at input time.
		 */
		@Name(OPERATION)
		@Mandatory
		@PropertyEditor(PlainEditor.class)
		Expr getOperation();

		/**
		 * Setter for {@link #getOperation()}.
		 */
		void setOperation(Expr operation);

		/**
		 * Whether {@link #getOperation()} should be executed in a transaction context.
		 */
		@Name(TRANSACTION)
		boolean isTransaction();
	}

	/**
	 * Creates a {@link ServiceMethodBuilderByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ServiceMethodBuilderByExpression(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public ServiceMethod build(String path, List<String> parameters) {
		Expr expr = getConfig().getOperation();

		// Note: The last argument is passed to the innermost function. Therefore, the lambdas must
		// be constructed in reverse order.
		for (int n = parameters.size() - 1; n >= 0; n--) {
			expr = lambda(parameters.get(n), expr);
		}

		QueryExecutor operation;
		try {
			operation = QueryExecutor.compile(expr);
		} catch (RuntimeException ex) {
			throw new TopLogicException(
				I18NConstants.ERROR_COMPILING_EXPRESSION__PATH_PARAMETERS.fill(path, parameters), ex);
		}
		boolean transaction = getConfig().isTransaction();

		return new ServiceMethodByExpression(path, parameters, transaction, operation);
	}

	private static Expr lambda(String param, Expr expr) {
		Define result = TypedConfiguration.newConfigItem(Define.class);
		result.setName(param);
		result.setExpr(expr);
		return result;
	}

}
