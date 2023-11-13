/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.string;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SimpleGenericMethod;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * Computes the sub-string from a given start index to an (optional) end index.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SubString extends SimpleGenericMethod {

	/**
	 * Creates a {@link SubString}.
	 */
	protected SubString(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new SubString(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.STRING_TYPE);
	}

	@Override
	public Object eval(Object[] arguments) {
		Object from = arguments[1];
		Object to = arguments[2];

		String str = asString(arguments[0]);
		int beginIndex = toIndex(str, asInt(from));
		int endIndex = (to == null) ? str.length() : toIndex(str, asInt(to));

		// Special case for using the semantics "negative indices count from the end of the list".
		// (Negative) zero means the end of the list, if the range would be invalid otherwise.
		if (endIndex == 0 && endIndex < beginIndex) {
			endIndex = str.length();
		}
		try {
			return str.substring(beginIndex, endIndex);
		} catch (IndexOutOfBoundsException ex) {
			throw new TopLogicException(
				I18NConstants.ERROR_INVALID_STRING_INDEX__STR_START_STOP_EXPR.fill(str, beginIndex, endIndex, this));
		}
	}

	private int toIndex(String str, int index) {
		// Allow to use negative number to create an index starting from the end of the string.
		if (index < 0) {
			index = str.length() + index;
		}

		return index;
	}

	/**
	 * {@link MethodBuilder} creating {@link SubString}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<SubString> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("input")
			.optional("from", 0)
			.optional("to")
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
		public SubString build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			return new SubString(getConfig().getName(), self, args);
		}

	}

}
