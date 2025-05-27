/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.binary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.util.error.TopLogicException;

/**
 * Parses a {@link BinaryContent} as ZIP file and delivers the uncompressed entries.
 */
public class Unzip extends GenericMethod {

	/**
	 * Creates a {@link Unzip}.
	 */
	protected Unzip(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Unzip(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		List<Map<String, Object>> result = new ArrayList<>();

		BinaryContent zipData = (BinaryContent) arguments[0];
		try {
			ZipInputStream zipStream = new ZipInputStream(zipData.getStream());
			while (true) {
				ZipEntry entry = zipStream.getNextEntry();
				if (entry == null) {
					break;
				}

				if (entry.isDirectory()) {
					continue;
				}

				Map<String, Object> entryObj = new HashMap<>();
				if (entry.getComment() != null) {
					entryObj.put("comment", entry.getComment());
				}
				entryObj.put("name", entry.getName());

				if (entry.getCreationTime() != null) {
					entryObj.put("creationTime", new Date(entry.getCreationTime().toMillis()));
				}
				if (entry.getExtra() != null) {
					entryObj.put("extra", BinaryDataFactory.createBinaryData(entry.getExtra()));
				}
				if (entry.getLastAccessTime() != null) {
					entryObj.put("lastAccessTime", new Date(entry.getLastAccessTime().toMillis()));
				}
				if (entry.getLastModifiedTime() != null) {
					entryObj.put("lastModifiedTime", new Date(entry.getLastModifiedTime().toMillis()));
				}

				entryObj.put("method", entry.getMethod() == ZipEntry.DEFLATED ? "deflated" : "stored");
				entryObj.put("size", Double.valueOf(entry.getSize()));
				entryObj.put("compressedSize", Double.valueOf(entry.getCompressedSize()));
				entryObj.put("crc", Double.valueOf(entry.getCrc()));

				long entrySize = entry.getSize();
				byte[] buffer;
				if (entrySize >= 0) {
					buffer = new byte[(int) entrySize];
					StreamUtilities.readFully(zipStream, buffer);
				} else {
					try (ByteArrayOutputStream bufferStream = new ByteArrayOutputStream()) {
						StreamUtilities.copyStreamContents(zipStream, bufferStream);
						buffer = bufferStream.toByteArray();
					}
				}
				BinaryData entryData = BinaryDataFactory.createBinaryData(buffer);
				entryObj.put("data", entryData);

				result.add(entryObj);
			}
		} catch (IOException ex) {
			throw new TopLogicException(
				I18NConstants.ERROR_UNZIP_FAILED__NAME_MSG.fill(zipData.getName(), ex.getMessage()), ex);
		}

		return result;
	}

	/**
	 * {@link MethodBuilder} creating {@link Unzip} expressions.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Unzip> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("zip")
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
		public Unzip build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new Unzip(getConfig().getName(), args);
		}

	}

}
