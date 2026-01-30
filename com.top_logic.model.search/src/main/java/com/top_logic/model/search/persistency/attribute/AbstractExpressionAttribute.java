/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.persistency.attribute;

import static com.top_logic.model.search.expr.query.QueryExecutor.*;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.exception.I18NFailure;
import com.top_logic.element.meta.kbbased.storage.AbstractDerivedStorage;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.providers.AttributeByExpression;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link AbstractDerivedStorage} for an attribute that is based on a configured search expression.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractExpressionAttribute<C extends AbstractExpressionAttribute.Config<?>>
		extends AbstractDerivedStorage<C> {

	private SearchExpression _expr;

	/**
	 * Configuration options for {@link AttributeByExpression}.
	 */
	@Abstract
	public interface Config<I extends AbstractExpressionAttribute<?>> extends AbstractDerivedStorage.Config<I> {

		/**
		 * The expression that computes the attribute value.
		 * 
		 * <p>
		 * The expression is expected to be a function that takes the context object that declares
		 * the attribute as single argument.
		 * </p>
		 */
		@Name("expr")
		@Label("Function")
		@DefaultContainer
		Expr getExpr();
	}

	/**
	 * Creates a {@link AttributeByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractExpressionAttribute(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public void init(TLStructuredTypePart attribute) {
		super.init(attribute);

		try {
			// Note: The expression must be compiled in the context of the attribute it computes.
			// Otherwise, access to model literals that is resolved by the compiler use the wrong
			// versions (e.g. classifier instances).
			_expr = compileExpr(attribute.getModel(), getConfig().getExpr());
		} catch (Exception ex) {
			throw new TopLogicException(
				I18NConstants.ERROR_INITIALIZING_ATTR__ATTR_MSG.fill(TLModelUtil.qualifiedName(attribute),
					ex instanceof I18NFailure i18n ? i18n.getErrorKey() : ex.getMessage()));
		}
	}

	/**
	 * The configured expression.
	 */
	public SearchExpression getExpr() {
		return _expr;
	}

}
