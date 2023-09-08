/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLCreateVisibility;
import com.top_logic.model.provider.DefaultProvider;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link DefaultProvider} retrieving the default value from an expression.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("Computed default value")
public class DefaultByExpression extends AbstractConfiguredInstance<DefaultByExpression.Config>
		implements DefaultProvider {

	/**
	 * Configuration for the {@link DefaultByExpression}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("default-by-expression")
	public interface Config extends PolymorphicConfiguration<DefaultByExpression> {

		/**
		 * Function computing the default value for the annotated {@link TLStructuredTypePart
		 * attribute}.
		 * 
		 * <p>
		 * The function is expected to accept the context object of the newly created object as
		 * single argument. The context object of the new object is passed to the <code>new</code>
		 * function at the location, where the object is created. In tree structures, the context
		 * object is typically the parent node of the newly created node.
		 * </p>
		 * 
		 * @see #getInTransaction()
		 */
		@Mandatory
		Expr getValue();

		/**
		 * Whether the computation in {@link #getValue()} should be delayed until the create
		 * transaction is performed.
		 * 
		 * <p>
		 * This setting is necessary, if the script in {@link #getValue()} performs operation that
		 * can only be executed when in transaction context (e.g. <code>new</code>).
		 * </p>
		 * 
		 * <p>
		 * When setting this option, it is advisable to hide the property in the create dialog using
		 * the annotation {@link TLCreateVisibility}.
		 * </p>
		 * 
		 * @see #getValue()
		 */
		boolean getInTransaction();

	}

	private QueryExecutor _defaultExpr;

	/**
	 * Creates a new {@link DefaultByExpression}.
	 */
	public DefaultByExpression(InstantiationContext context, Config config) {
		super(context, config);

		_defaultExpr = QueryExecutor.compile(config.getValue());
	}

	@Override
	public Object createDefault(Object context, TLStructuredTypePart attribute, boolean createForUI) {
		if (getConfig().getInTransaction() && createForUI) {
			// The expression contains transactional operations and cannot be executed without a
			// transaction context.
			return null;
		}
		return _defaultExpr.execute(context);
	}

}

