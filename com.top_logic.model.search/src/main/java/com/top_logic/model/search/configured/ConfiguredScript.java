/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.configured;

import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.compile.transform.HasSideEffects;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.dom.Expr.Define;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptorBuilder;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.model.ModelService;

/**
 * {@link ConfiguredMethodBuilder} that creates {@link SearchExpression} based on a configured
 * {@link Expr}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfiguredScript extends AbstractConfiguredMethodBuilder<ConfiguredScript.Config> {

	/**
	 * Configuration for {@link ConfiguredScript}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("configured-script")
	public interface Config extends ConfiguredMethodBuilder.Config<ConfiguredScript>, ScriptConfiguration {
		// sum interface
	}

	private QueryExecutor _executor;

	private Boolean _hasSideEffect;

	private final ArgumentDescriptor _descriptor;

	/**
	 * Create a {@link ConfiguredScript}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ConfiguredScript(InstantiationContext context, Config config) {
		super(context, config);
		_descriptor = createArgumentDescriptor();
	}

	@Override
	public void resolveExternalRelations() {
		Expr expr = createBaseExpression();
		if (ModelService.Module.INSTANCE.isActive()) {
			_hasSideEffect = computeHasSideEffects(expr);
		} else {
			/* Computed lazy in #isSideEffectFree(). */
		}
		_executor = QueryExecutor.compile(expr);
	}

	private Boolean computeHasSideEffects(Expr expr) {
		return QueryExecutor.compileExpr(expr).visit(HasSideEffects.INSTANCE, null);
	}

	private ArgumentDescriptor createArgumentDescriptor() {
		ArgumentDescriptorBuilder builder = ArgumentDescriptor.builder();
		for (ScriptParameter parameter : getConfig().getParameters()) {
			addArgument(builder, parameter);
		}
		return builder.build();
	}

	private void addArgument(ArgumentDescriptorBuilder builder, ScriptParameter parameter) {
		if (parameter.isMandatory()) {
			builder.mandatory(parameter.getName());
		} else {
			Expr defaultValue = parameter.getDefaultValue();
			if (defaultValue == null) {
				builder.optional(parameter.getName());
			} else {
				builder.optional(parameter.getName(), () -> QueryExecutor.compileExpr(defaultValue));
			}
		}
	}

	private Expr createBaseExpression() {
		Expr expr = getConfig().getImplementation();

		// Note: The last argument is passed to the innermost function. Therefore, the lambdas must
		// be constructed in reverse order.
		List<ScriptParameter> parameters = getConfig().getParameters();
		for (int n = parameters.size() - 1; n >= 0; n--) {
			expr = Define.create(parameters.get(n).getName(), expr);
		}
		return expr;
	}

	@Override
	public SearchExpression build(Expr expr, SearchExpression[] args) throws ConfigurationException {
		/* Note: The executor must not be used direct, because SearchExpression may be accessed,
		 * before the Executor is resolved. */
		/* Note: "sideeffectfree" must not be used direct, because it needs the ModelService which
		 * is eventually not yet started. */
		return new QueryExecutorMethod(this::getExecutor, getConfig().getName(), args, this::isSideEffectFree);
	}

	private boolean isSideEffectFree() {
		if (_hasSideEffect == null) {
			_hasSideEffect = computeHasSideEffects(createBaseExpression());
		}
		return !_hasSideEffect.booleanValue();
	}

	private QueryExecutor getExecutor() {
		return _executor;
	}

	@Override
	public Object getId() {
		return getConfig().getName();
	}

	@Override
	public ArgumentDescriptor descriptor() {
		return _descriptor;
	}

	@Override
	public HTMLFragment documentation() {
		return new ConfiguredScriptDocumentation(getConfig());
	}

}
