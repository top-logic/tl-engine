/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.binary;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.WithFlatMapSemantics;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link GenericMethod} parsing a string to a JSON object.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ParseJson extends GenericMethod implements WithFlatMapSemantics<Object[]> {

	/** 
	 * Creates a {@link ParseJson}.
	 */
	protected ParseJson(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ParseJson(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		return evalPotentialFlatMap(definitions, null, arguments);
	}

	@Override
	public Object evalDirect(EvalContext definitions, Object self, Object[] arguments) {
		Object input = arguments[0];
		if (input == null) {
			return null;
		}

		try {
			if (input instanceof BinaryDataSource) {
				BinaryDataSource data = (BinaryDataSource) input;

				MimeType mimeType = new MimeType(data.getContentType());
				String charset = mimeType.getParameter("charset");
				if (charset == null) {
					charset = StringServices.UTF8;
				}
				try (InputStream in = data.toData().getStream()) {
					return JSON.read(new InputStreamReader(in, charset));
				}
			} else {
				String json = input.toString();
				return JSON.fromString(json);
			}
		} catch (ParseException | IOException | MimeTypeParseException ex) {
			throw new TopLogicException(I18NConstants.ERROR_CONVERSION_FAILED__MSG.fill(ex.getMessage()), ex);
		}
	}

	/**
	 * {@link MethodBuilder} for {@link ParseJson}.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<ParseJson> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("input")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ParseJson build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new ParseJson(getName(), args);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

	}
}
