/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.Sink;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.ModeSelector;
import com.top_logic.model.form.definition.FormVisibility;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.expr.trace.ScriptTracer;
import com.top_logic.model.util.Pointer;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * {@link ModeSelector} that can be parameterized by TL-Script expressions.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class ModeSelectorByExpression<C extends ModeSelectorByExpression.Config<?>>
		extends AbstractConfiguredInstance<C> implements ModeSelector {

	private final QueryExecutor _selector;

	private final ScriptTracer _selectorAnalyzer;

	/**
	 * Configuration options for {@link ModeSelectorByExpression}.
	 */
	@TagName("constraint-by-expression")
	public interface Config<I extends ModeSelectorByExpression<?>> extends PolymorphicConfiguration<I> {

		/**
		 * Function computing the form field mode for an attribute.
		 * 
		 * <p>
		 * The function expects the object defining the displayed attribute as single argument:
		 * </p>
		 * 
		 * <code>object -> [one of <code>"editable"</code>, <code>"disabled"</code>,
		 * <code>"read-only"</code>, <code>"mandatory"</code>, <code>"hidden"</code>]</code>
		 * 
		 * @see FormVisibility
		 */
		Expr getSelector();

	}

	/**
	 * Creates a {@link ModeSelectorByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ModeSelectorByExpression(InstantiationContext context, C config) {
		super(context, config);

		_selector = QueryExecutor.compile(config.getSelector());
		_selectorAnalyzer = ScriptTracer.compile(model(), config.getSelector());
	}

	private static TLModel model() {
		return ModelService.getApplicationModel();
	}

	@Override
	public FormVisibility getMode(TLObject object, TLStructuredTypePart attribute) {
		return toFieldMode(_selector.execute(object));
	}

	@Override
	public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace) {
		_selectorAnalyzer.execute(trace, object);
	}

	private static FormVisibility toFieldMode(Object result) {
		if (result == null) {
			return FormVisibility.DEFAULT;
		}

		if (result instanceof Boolean) {
			return ((Boolean) result).booleanValue() ? FormVisibility.EDITABLE : FormVisibility.HIDDEN;
		}

		if (result instanceof String) {
			try {
				return FormVisibility.fromExternalName(((String) result).toLowerCase());
			} catch (IllegalArgumentException ex) {
				// Problem reported below.
			}
		}

		throw new TopLogicException(I18NConstants.ERROR_MODE__VALUE_OPTIONS.fill(result,
			Arrays.stream(FormVisibility.values()).map(FormVisibility::getExternalName)
				.collect(Collectors.joining(", "))));
	}

}
