/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.binary;

import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.output.CloseShieldOutputStream;

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
 * Constructs a {@link ZipEntrySource} to be used as entry of a {@link ZipArchive}.
 */
public class ZipEntry extends GenericMethod {

	/**
	 * Creates a {@link ZipEntry}.
	 */
	public ZipEntry(String name, SearchExpression self, SearchExpression[] args) {
		super(name, self, args);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new ZipEntry(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.BINARY_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		BinaryDataSource contents = (BinaryDataSource) arguments[0];
		String comment = asString(arguments[1], null);
		Object rawTime = arguments[2];
		long time;
		if (rawTime instanceof Date) {
			time = ((Date) rawTime).getTime();
		} else if (rawTime instanceof Calendar) {
			time = ((Calendar) rawTime).getTimeInMillis();
		} else {
			time = asLong(rawTime, Long.MIN_VALUE);
		}
		boolean compress = asBoolean(arguments[3]);

		return new ZipEntrySource() {
			@Override
			public void deliverTo(ZipOutputStream zip) throws IOException {
				java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(contents.getName());
				entry.setComment(comment);
				if (time > Long.MIN_VALUE) {
					entry.setLastModifiedTime(FileTime.fromMillis(time));
				}
				entry.setMethod(compress ? java.util.zip.ZipEntry.DEFLATED : java.util.zip.ZipEntry.STORED);

				zip.putNextEntry(entry);
				try {
					contents.deliverTo(CloseShieldOutputStream.wrap(zip));
				} finally {
					zip.closeEntry();
				}
			}
		};
	}

	/**
	 * {@link MethodBuilder} creating {@link ZipEntry} instances.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ZipEntry> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("contents")
			.optional("comment")
			.optional("time")
			.optional("compress", true)
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
		public ZipEntry build(Expr expr, SearchExpression self, SearchExpression[] args) throws ConfigurationException {
			return new ZipEntry(getConfig().getName(), self, args);
		}

	}

}
