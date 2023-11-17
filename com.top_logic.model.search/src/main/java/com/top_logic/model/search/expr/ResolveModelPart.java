/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Resolves a {@link TLModelPart} by its qualified name.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ResolveModelPart extends GenericMethod {

	/**
	 * Creates a {@link ResolveModelPart}.
	 */
	protected ResolveModelPart(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ResolveModelPart(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		TLModel model;
		if (argumentTypes.get(0) != null) {
			model = argumentTypes.get(0).getModel();
		} else {
			model = ModelService.getApplicationModel();
		}
		TLModule tlModelModule = model.getModule(TlModelFactory.TL_MODEL_STRUCTURE);
		return tlModelModule.getType(TLModelPart.TL_MODEL_PART_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		String qualifiedPartName = asString(arguments[0]);
		return TLModelUtil.resolveModelPart(qualifiedPartName);
	}

	/**
	 * The qualified name of a model element can represent different objects at different times
	 * because the model part may have been deleted or newly created.
	 */
	@Override
	public boolean canEvaluateAtCompileTime(Object[] arguments) {
		return false;
	}

	/**
	 * {@link MethodBuilder} creating {@link ResolveModelPart}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ResolveModelPart> {
		
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ResolveModelPart build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			checkSingleArg(expr, args);
			return new ResolveModelPart(getName(), args);
		}

	}
}
