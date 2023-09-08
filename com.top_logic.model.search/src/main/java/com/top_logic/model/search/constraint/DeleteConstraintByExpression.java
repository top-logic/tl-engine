/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.constraint;

import java.util.Optional;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.access.DeleteConstraint;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.TLResKeyUtil;

/**
 * {@link DeleteConstraint} that can be configured through a search expression.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DeleteConstraintByExpression<C extends DeleteConstraintByExpression.Config<?>>
		extends AbstractConfiguredInstance<C> implements DeleteConstraint {

	private final QueryExecutor _expr;

	/**
	 * Configuration options for {@link DeleteConstraintByExpression}.
	 */
	@TagName("delete-constraint")
	public interface Config<I extends DeleteConstraintByExpression<?>> extends PolymorphicConfiguration<I> {
		/**
		 * Function that is called on an object that is checked for deletion.
		 * 
		 * <p>
		 * To allow deletion, <code>true</code> or <code>null</code> must be returned. Any other
		 * value denies deletion. A returned string message or resource key that explains why the
		 * instance cannot be deleted is used to show the user why the action could not be
		 * performed.
		 * </p>
		 */
		@Mandatory
		@DefaultContainer
		Expr getExpr();
	}

	/**
	 * Creates a {@link DeleteConstraintByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DeleteConstraintByExpression(InstantiationContext context, C config) {
		super(context, config);

		QueryExecutor expr;
		try {
			expr = QueryExecutor.compile(config.getExpr());
		} catch (Exception ex) {
			Logger.error("Creating delete constraint failed.", ex, DeleteConstraintByExpression.class);
			expr = null;
		}
		_expr = expr;
	}

	@Override
	public Optional<ResKey> getDeleteVeto(TLObject obj) {
		if (_expr == null) {
			return Optional.empty();
		}
		Object result = _expr.execute(obj);
		if (result == null || Boolean.TRUE.equals(result)) {
			return Optional.empty();
		}

		ResKey errorKey = I18NConstants.NO_DELETION_POSSIBLE__OBJECT.fill(MetaLabelProvider.INSTANCE.getLabel(obj));

		return Optional.of(TLResKeyUtil.toResKey(result, errorKey));
	}
}
