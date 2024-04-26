/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.List;
import java.util.Locale;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.TLContextManager;

/**
 * {@link SearchExpression} returning the {@link LabelProvider#getLabel(Object) label} for the given
 * object in the given language.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class Label extends GenericMethod {

	/** Creates an {@link Label}. */
	Label(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public Label copy(SearchExpression[] arguments) {
		return new Label(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.STRING_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Locale locale = asLocaleNullable(arguments[1]);
		return TLContextManager.getSubSession().withLocale(locale, () -> getLabel(arguments[0]));
	}

	private String getLabel(Object object) {
		return MetaLabelProvider.INSTANCE.getLabel(object);
	}

	/** {@link MethodBuilder} creating {@link Label}. */
	public static final class Builder extends AbstractSimpleMethodBuilder<Label> {

		/** Description of parameters for a {@link Label}. */
		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("object")
			.optional("lang")
			.build();

		/** Creates a {@link Builder}. */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public Label build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new Label(getConfig().getName(), args);
		}
	}
}
