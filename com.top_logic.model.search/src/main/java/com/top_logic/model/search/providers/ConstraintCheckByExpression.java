/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.Sink;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.ConstraintCheck;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.expr.trace.ScriptTracer;
import com.top_logic.model.util.Pointer;
import com.top_logic.util.model.ModelService;

/**
 * {@link ConstraintCheck} that can be parameterized by TL-Script expressions.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class ConstraintCheckByExpression<C extends ConstraintCheckByExpression.Config<?>>
		extends AbstractConfiguredInstance<C> implements ConstraintCheck {

	private final QueryExecutor _check;

	private final ScriptTracer _checkAnalyzer;

	/**
	 * Configuration options for {@link ConstraintCheckByExpression}.
	 */
	@TagName("constraint-by-expression")
	public interface Config<I extends ConstraintCheckByExpression<?>> extends PolymorphicConfiguration<I> {

		/**
		 * Function testing a certain consistency constraint on an object.
		 * 
		 * <p>
		 * The function expects the current value of the context attribute as first argument and the
		 * base object defining the attribute as second argument. A failure is signaled by returning
		 * an internationalized text describing the problem. A <code>null</code> value is
		 * interpreted as success:
		 * </p>
		 * 
		 * <code>value -> object -> [expression computing {@link ResKey}]</code>
		 * 
		 * <p>
		 * The context attribute is the attribute, this {@link ConstraintCheck} algorithm is
		 * annotated to. It's value is the main value to check. Any check failure messages are
		 * reported at the input field for this context attribute, if the check is performed in the
		 * user interface.
		 * </p>
		 */
		Expr getCheck();

	}

	/**
	 * Creates a {@link ConstraintCheckByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConstraintCheckByExpression(InstantiationContext context, C config) {
		super(context, config);

		_check = QueryExecutor.compile(config.getCheck());
		_checkAnalyzer = ScriptTracer.compile(model(), config.getCheck());
	}

	private static TLModel model() {
		return ModelService.getApplicationModel();
	}

	@Override
	public ResKey check(TLObject object, TLStructuredTypePart attribute) {
		return toResKey(_check.execute(object.tValue(attribute), object));
	}

	@Override
	public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace) {
		_checkAnalyzer.execute(trace, object.tValue(attribute), object);
	}

	private static ResKey toResKey(Object result) {
		if (result == null || result instanceof Boolean && ((Boolean) result).booleanValue()) {
			return null;
		}

		if (result instanceof ResKey) {
			return (ResKey) result;
		} else if (result instanceof String) {
			return ResKey.text((String) result);
		} else {
			return I18NConstants.ERROR_INVALID_VALUE;
		}
	}

}
