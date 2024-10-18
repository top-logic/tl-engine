/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.display;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.form.fieldprovider.form.FormTypeResolver;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link FormTypeResolver} implemented by a TL-Script function.
 */
public class ScriptFormTypeComputation extends AbstractConfiguredInstance<ScriptFormTypeComputation.Config<?>>
		implements FormTypeResolver {

	/**
	 * Configuration options for {@link ScriptFormTypeComputation}.
	 */
	@TagName("script")
	public interface Config<I extends ScriptFormTypeComputation> extends PolymorphicConfiguration<I> {
		/**
		 * Function computing the type of objects to create a form for.
		 * 
		 * <p>
		 * It is expected that the expression accepts the component model as single argument.
		 * </p>
		 */
		Expr getFunction();
	}

	private final QueryExecutor _expr;

	/**
	 * Creates a {@link ScriptFormTypeComputation} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ScriptFormTypeComputation(InstantiationContext context, Config<?> config) {
		super(context, config);

		_expr = QueryExecutor.compile(config.getFunction());
	}

	@Override
	public TLStructuredType getFormType(TLObject context) {
		return (TLStructuredType) _expr.execute(context);
	}

}
