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
import java.util.zip.GZIPOutputStream;

import com.top_logic.basic.Named;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryDataSource;
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
 * {@link GenericMethod} compressing a binary value using the GZIP compression format.
 */
public class Gzip extends GenericMethod {

	/**
	 * The content type of the compressed result.
	 */
	public static final String CONTENT_TYPE_GZIP = "application/gzip";

	/**
	 * The file name suffix appended to the input name, if no explicit name is given.
	 */
	public static final String GZ_SUFFIX = ".gz";

	/**
	 * Creates a {@link Gzip}.
	 */
	protected Gzip(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Gzip(getName(), arguments);
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
		if (!(input instanceof BinaryDataSource) && !(input instanceof BinaryContent)) {
			throw new TopLogicException(
				I18NConstants.ERROR_NOT_A_BINARY_VALUE__VAL_EXPR.fill(input, getArguments()[0]));
		}

		String name = asString(arguments[1], ((Named) input).getName() + GZ_SUFFIX);
		int level = Math.max(0, Math.min(9, asInt(arguments[2])));

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
				return CONTENT_TYPE_GZIP;
			}

			@Override
			public void deliverTo(OutputStream out) throws IOException {
				try (GZIPOutputStream zip = new GZIPOutputStream(out) {
					{
						def.setLevel(level);
					}
				}) {
					if (input instanceof BinaryDataSource) {
						((BinaryDataSource) input).deliverTo(zip);
					} else {
						try (InputStream in = ((BinaryContent) input).getStream()) {
							StreamUtilities.copyStreamContents(in, zip);
						}
					}
				}
			}
		};
	}

	/**
	 * {@link MethodBuilder} creating {@link Gzip} expressions.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Gzip> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("input")
			.optional("name")
			.optional("level", 9)
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
		public Gzip build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new Gzip(getConfig().getName(), args);
		}

	}

}
