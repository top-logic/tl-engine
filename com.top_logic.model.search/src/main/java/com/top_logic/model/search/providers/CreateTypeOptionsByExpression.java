/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.element.layout.create.CreateTypeOptions;
import com.top_logic.model.TLClass;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link CreateTypeOptions} implementation that can be parameterized with TL-Script expressions.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Dynamic type options")
public class CreateTypeOptionsByExpression<C extends CreateTypeOptionsByExpression.Config<?>>
		extends AbstractConfiguredInstance<C> implements CreateTypeOptions {

	/**
	 * Configuration options for {@link CreateTypeOptionsByExpression}.
	 */
	public interface Config<I extends CreateTypeOptionsByExpression<?>> extends PolymorphicConfiguration<I> {

		/**
		 * Function computing possible create types for the given context.
		 * 
		 * <p>
		 * The function expects the context model as single argument. The function is expected to
		 * return a list of types that can be instantiated in this context.
		 * </p>
		 */
		@Mandatory
		Expr getPossibleTypes();

		/**
		 * Whether to automatically add all sub-types for classes returned by
		 * {@link #getPossibleTypes()}.
		 */
		boolean getIncludeSubtypes();

		/**
		 * Function selecting the default create type for the given context.
		 * 
		 * <p>
		 * The function expects two arguments. The first argument is the context model and the
		 * second argument is the list of possible create types computed by
		 * {@link #getPossibleTypes()}. The function result is expected to be the type that should
		 * be created by default, if the user does not choose from {@link #getPossibleTypes()}.
		 * </p>
		 * 
		 * <p>
		 * If not given, the default is to choose the first type returned from the
		 * {@link #getPossibleTypes()}.
		 * </p>
		 */
		Expr getDefaultType();
	}

	private QueryExecutor _possibleTypes;

	private QueryExecutor _defaultType;

	/**
	 * Creates a {@link CreateTypeOptionsByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CreateTypeOptionsByExpression(InstantiationContext context, C config) {
		super(context, config);
		_possibleTypes = QueryExecutor.compile(config.getPossibleTypes());
		_defaultType = QueryExecutor.compileOptional(config.getDefaultType());
	}


	@Override
	public List<TLClass> getPossibleTypes(Object contextModel) {
		List<TLClass> possibleTypes = CollectionUtil.dynamicCastView(TLClass.class,
			SearchExpression.asList(_possibleTypes.execute(contextModel)));

		if (getConfig().getIncludeSubtypes()) {
			Set<TLClass> result = new LinkedHashSet<>();

			for (TLClass direct : possibleTypes) {
				result.addAll(TLModelUtil.getConcreteReflexiveTransitiveSpecializations(direct));
			}
			return new ArrayList<>(result);
		} else {
			return FilterUtil.filterList(t -> !t.isAbstract(), possibleTypes);
		}
	}

	@Override
	public TLClass getDefaultType(Object contextModel, List<TLClass> typeOptions) {
		if (_defaultType != null) {
			return SearchExpression.asTLObject(TLClass.class, _defaultType.getSearch(),
				_defaultType.execute(contextModel, typeOptions));
		}
		return CreateTypeOptions.super.getDefaultType(contextModel, typeOptions);
	}

	@Override
	public boolean supportsContext(Object contextModel) {
		return !getPossibleTypes(contextModel).isEmpty();
	}

}
