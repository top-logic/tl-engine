/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.element.structured.util.DynamicSequenceName;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link DynamicSequenceName} that can be implemented with a <i>TL-Script</i> expression.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class SequenceNameByExpression<C extends SequenceNameByExpression.Config<?>>
		extends AbstractConfiguredInstance<C> implements DynamicSequenceName {

	/**
	 * Configuration options for {@link SequenceNameByExpression}.
	 */
	public interface Config<I extends SequenceNameByExpression<?>> extends PolymorphicConfiguration<I> {

		/**
		 * Function computing the sequence name for a given context.
		 * 
		 * <p>
		 * The function expects the creation context of an object as single argument. The result of
		 * the function is an identifier for the sequence. A new sequence of numbers is started for
		 * each unique value returned from this function.
		 * </p>
		 */
		@Mandatory
		Expr getSequenceName();

	}

	private QueryExecutor _sequenceName;

	/**
	 * Creates a new {@link SequenceNameByExpression}.
	 */
	public SequenceNameByExpression(InstantiationContext context, C config) {
		super(context, config);
		_sequenceName = QueryExecutor.compile(config.getSequenceName());
	}

	@Override
	public Object getSequenceName(Object context) {
		return _sequenceName.execute(context);
	}

}
