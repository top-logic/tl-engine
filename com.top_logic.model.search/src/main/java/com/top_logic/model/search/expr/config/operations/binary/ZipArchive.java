/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.binary;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.output.CloseShieldOutputStream;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;

/**
 * Constructs a {@link BinaryDataSource binary value} with a ZIP archive content.
 */
public class ZipArchive extends GenericMethod {

	/**
	 * Creates a {@link ZipArchive}.
	 */
	public ZipArchive(String name, SearchExpression[] args) {
		super(name, args);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ZipArchive(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.BINARY_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		String name = asString(arguments[0]);
		List<?> entries = asList(arguments[1]);
		String comment = asString(arguments[2], null);
		boolean compress = asBoolean(arguments[3]);
		int level = Math.max(0, Math.min(9, asInt(arguments[4])));
		Charset charSet = Charset.forName(asString(arguments[5]));

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
				return "application/zip";
			}

			@Override
			public void deliverTo(OutputStream out) throws IOException {
				try (ZipOutputStream zip = new ZipOutputStream(out, charSet)) {
					if (comment != null) {
						zip.setComment(comment);
					}
					zip.setMethod(compress ? ZipOutputStream.DEFLATED : ZipOutputStream.STORED);
					zip.setLevel(level);

					deliverEntries(zip, entries);
				}
			}

			private void deliverEntries(ZipOutputStream zip, Collection<?> contents) throws IOException {
				for (Object entry : contents) {
					deliverEntry(zip, entry);
				}
			}

			private void deliverEntry(ZipOutputStream zip, Object entry) throws IOException {
				if (entry == null) {
					return;
				}
				if (entry instanceof BinaryDataSource) {
					BinaryDataSource contents = ((BinaryDataSource) entry);

					zip.putNextEntry(new java.util.zip.ZipEntry(contents.getName()));
					try {
						contents.deliverTo(CloseShieldOutputStream.wrap(zip));
					} finally {
						zip.closeEntry();
					}
				} else if (entry instanceof ZipEntrySource) {
					((ZipEntrySource) entry).deliverTo(zip);
				} else if (entry instanceof Collection<?>) {
					deliverEntries(zip, (Collection<?>) entry);
				} else {
					Logger.error("Cannot add a value of type " + entry.getClass().getName() + " to a ZIP archive.",
						ZipArchive.class);
				}
			}
		};
	}

	/**
	 * {@link MethodBuilder} creating {@link ZipArchive} expressions.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ZipArchive> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("name")
			.mandatory("entries")
			.optional("comment")
			.optional("compress", true)
			.optional("level", 9)
			.optional("charset", "utf-8")
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
		public ZipArchive build(Expr expr, SearchExpression self, SearchExpression[] args) throws ConfigurationException {
			return new ZipArchive(getConfig().getName(), args);
		}

	}

}
