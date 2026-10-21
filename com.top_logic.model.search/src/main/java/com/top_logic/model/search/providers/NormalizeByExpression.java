/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.ValueNormalizer;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ValueNormalizer} that can be parameterized by a TL-Script function.
 *
 * <p>
 * The function runs with the rights of the current user, since it only transforms the value the
 * user entered.
 * </p>
 */
@InApp
public class NormalizeByExpression<C extends NormalizeByExpression.Config<?>>
		extends AbstractConfiguredInstance<C> implements ValueNormalizer {

	private final QueryExecutor _function;

	/**
	 * Configuration options for {@link NormalizeByExpression}.
	 */
	@TagName("normalize-by-expression")
	public interface Config<I extends NormalizeByExpression<?>> extends PolymorphicConfiguration<I> {

		/** Configuration name for {@link #getFunction()}. */
		String FUNCTION = "function";

		/**
		 * Function computing the canonical form of an entered value.
		 *
		 * <p>
		 * The function expects the entered value as first argument and the edited object as second
		 * argument. The object is <code>null</code>, if the value belongs to an input of a view
		 * that is not bound to an attribute:
		 * </p>
		 *
		 * <code>value -> object -> [normalized value]</code>
		 *
		 * <p>
		 * A value that cannot be canonicalized must be returned unchanged, e.g.
		 * <code>value -> object -> $value.trim()</code> for a text. A <code>null</code> result is
		 * stored as empty value.
		 * </p>
		 */
		@Name(FUNCTION)
		@Mandatory
		Expr getFunction();

	}

	/**
	 * Creates a {@link NormalizeByExpression} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public NormalizeByExpression(InstantiationContext context, C config) {
		super(context, config);

		_function = QueryExecutor.compile(config.getFunction());
	}

	@Override
	public Object normalize(TLObject object, TLStructuredTypePart attribute, Object value) {
		return _function.execute(value, object);
	}

}
