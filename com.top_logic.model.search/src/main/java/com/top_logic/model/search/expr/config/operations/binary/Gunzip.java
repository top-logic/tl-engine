/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.binary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.top_logic.basic.Named;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.mime.MimeTypesModule;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.I18NConstants;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link GenericMethod} decompressing a binary value compressed with the GZIP compression format.
 */
public class Gunzip extends GenericMethod {

	/**
	 * Creates a {@link Gunzip}.
	 */
	protected Gunzip(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Gunzip(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.BINARY_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object input = arguments[0];
		if (input == null) {
			return null;
		}

		BinaryContent content;
		if (input instanceof BinaryContent) {
			content = (BinaryContent) input;
		} else if (input instanceof BinaryDataSource) {
			content = ((BinaryDataSource) input).toData();
		} else {
			throw new TopLogicException(
				I18NConstants.ERROR_NOT_A_BINARY_VALUE__VAL_EXPR.fill(input, getArguments()[0]));
		}

		String name = asString(arguments[1], defaultName(((Named) input).getName()));
		String contentType = asString(arguments[2], MimeTypesModule.getInstance().getMimeType(name));

		return new BinaryDataSource() {
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

			@Override
			public void deliverTo(OutputStream out) throws IOException {
				try (InputStream in = new GZIPInputStream(content.getStream())) {
					StreamUtilities.copyStreamContents(in, out);
				}
			}
		};
	}

	/**
	 * The name of the decompressed result derived from the given input name by stripping the
	 * {@link Gzip#GZ_SUFFIX} suffix, if present.
	 */
	private static String defaultName(String inputName) {
		if (inputName.endsWith(Gzip.GZ_SUFFIX)) {
			return inputName.substring(0, inputName.length() - Gzip.GZ_SUFFIX.length());
		}
		return inputName;
	}

	/**
	 * {@link MethodBuilder} creating {@link Gunzip} expressions.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Gunzip> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("input")
			.optional("name")
			.optional("contentType")
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
		public Gunzip build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new Gunzip(getConfig().getName(), args);
		}

	}

}
