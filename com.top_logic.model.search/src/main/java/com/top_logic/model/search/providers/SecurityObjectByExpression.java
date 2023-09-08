/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.SecurityObjectProvider;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link SecurityObjectProvider} computing the security object by evaluating an {@link Expr} on the
 * base model.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
public class SecurityObjectByExpression extends AbstractConfiguredInstance<SecurityObjectByExpression.Config>
		implements SecurityObjectProvider {

	/**
	 * Typed configuration interface definition for {@link SecurityObjectByExpression}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	@TagName("security-object-by-expression")
	public interface Config extends PolymorphicConfiguration<SecurityObjectByExpression> {

		/** Name of the property {@link #getFunction()}. */
		String FUNCTION_PROPERTY = "function";

		/**
		 * Function that computes the security object from the given model.
		 * 
		 * <p>
		 * The function is expected to accept one parameter, the model for which a security object
		 * is required. It must return the security object for the given model.
		 * </p>
		 * 
		 * @implNote The returned object must be a {@link BoundObject}.
		 */
		@Mandatory
		@Name(FUNCTION_PROPERTY)
		Expr getFunction();

	}

	private final QueryExecutor _function;

	/**
	 * Create a {@link SecurityObjectByExpression}.
	 * 
	 * @param context
	 *            the {@link InstantiationContext} to create the new object in
	 * @param config
	 *            the configuration object to be used for instantiation
	 */
	public SecurityObjectByExpression(InstantiationContext context, Config config) {
		super(context, config);
		_function = QueryExecutor.compile(config.getFunction());
	}

	@Override
	public BoundObject getSecurityObject(BoundChecker aChecker, Object model, BoundCommandGroup aCommandGroup) {
		Object securityObject = _function.execute(model);
		if (securityObject != null && !(securityObject instanceof BoundObject)) {
			throw new TopLogicException(
				I18NConstants.ERROR_NO_BOUND_OBJECT_RESULT__EXPR__SRC__VALUE.fill(_function.getSearch(), model,
					securityObject));
		}
		return (BoundObject) securityObject;
	}

}

