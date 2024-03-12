/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.cache;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.export.AccessContext;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.model.export.PreloadOperation;
import com.top_logic.model.export.Preloader;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.search.expr.interpreter.ConstantFolding;

/**
 * Database preload operation.
 */
public class Preload extends GenericMethod {

	/**
	 * Creates a {@link Preload}.
	 */
	protected Preload(String name, SearchExpression[] arguments) {
		super(name, arguments);

		// Compile-time preload building.
		SearchExpression specExpr = arguments[1];
		if (ConstantFolding.isLiteral(specExpr)) {
			arguments[1] = SearchExpressionFactory.literal(toPreload(specExpr, ConstantFolding.literalValue(specExpr)));
		}
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Preload(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		// Delivers the value computed by the given function on the input:
		// input -> paths -> fun -> { context = preload($input); $fun($input); }
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Collection<?> input = asCollection(arguments[0]);
		PreloadOperation preload = toPreload(getArguments()[1], arguments[1]);
		SearchExpression fun = asSearchExpression(arguments[2]);

		try (AccessContext context = preload.prepare(input)) {
			return fun.eval(definitions, input);
		}
	}

	private static PreloadOperation toPreload(SearchExpression specExpr, Object spec) {
		if (spec instanceof PreloadOperation) {
			return (PreloadOperation) spec;
		}

		Collection<?> paths = asCollection(spec);
		Preloader builder = new Preloader();
		for (Object entry : paths) {
			Object element = asSingleElement(specExpr, entry);
			if (element == null) {
				continue;
			}
			TLStructuredTypePart part = asTypePart(specExpr, element);
			PreloadContribution contribution = AttributeOperations.getStorageImplementation(part).getPreload();
			contribution.contribute(builder);
		}
		return builder;
	}

	/**
	 * {@link MethodBuilder} creating {@link Preload}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Preload> {
		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor
			.builder()
			.mandatory("objects")
			.mandatory("attributes")
			.mandatory("fun")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public Preload build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new Preload(getConfig().getName(), args);
		}

	}

}
