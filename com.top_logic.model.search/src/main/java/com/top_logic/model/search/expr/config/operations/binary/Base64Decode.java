/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.binary;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.mime.MimeTypesModule;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.WithFlatMapSemantics;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link GenericMethod} encoding a binary value into a string.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Base64Decode extends GenericMethod implements WithFlatMapSemantics<Object[]> {

	/** 
	 * Creates a {@link Base64Decode}.
	 */
	protected Base64Decode(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new Base64Decode(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.STRING_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		return evalPotentialFlatMap(definitions, null, arguments);
	}

	@Override
	public Object evalDirect(EvalContext definitions, Object self, Object[] arguments) {
		String input = asString(arguments[0]);
		if (input.isEmpty()) {
			return null;
		}

		String name = asString(arguments[1]);

		String specifiedContentType = asString(arguments[2]);
		String contentType;
		if (specifiedContentType == null) {
			contentType = MimeTypesModule.getInstance().getMimeType(name);
		} else {
			contentType = specifiedContentType;
		}

		return new BinaryData() {
			@Override
			public InputStream getStream() throws IOException {
				return Base64.getDecoder().wrap(new ASCIISource(input));
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public long getSize() {
				return -1;
			}

			@Override
			public String getContentType() {
				return contentType;
			}
		};
	}

	/**
	 * String buffer accepting binary data in ASCII encoding scheme.
	 */
	private static final class ASCIISource extends InputStream {
		private final String _input;

		private int _pos;

		/**
		 * Creates a {@link ASCIISource}.
		 *
		 * @param input
		 *        The ASCII value to read.
		 */
		public ASCIISource(String input) {
			_input = input;
			_pos = 0;
		}

		@Override
		public int read() throws IOException {
			if (_pos >= _input.length()) {
				return -1;
			}
			return _input.charAt(_pos++) & 0xFF;
		}
	}

	/**
	 * {@link MethodBuilder} for {@link Base64Decode}.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<Base64Decode> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("encodedData")
			.optional("name", "data")
			.optional("contentType")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public Base64Decode build(Expr expr, SearchExpression self, SearchExpression[] args) throws ConfigurationException {
			return new Base64Decode(getName(), self, args);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

	}
}
