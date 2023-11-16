/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.binary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;

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
import com.top_logic.model.search.expr.WithFlatMapSemantics;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link GenericMethod} encoding a binary value into a string.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Base64Encode extends GenericMethod implements WithFlatMapSemantics<Encoder> {

	/** 
	 * Creates a {@link Base64Encode}.
	 */
	protected Base64Encode(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Base64Encode(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.STRING_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Encoder encoding = asBoolean(arguments[1]) ? Base64.getMimeEncoder() : Base64.getEncoder();

		return evalPotentialFlatMap(definitions, arguments[0], encoding);
	}

	@Override
	public Object evalDirect(EvalContext definitions, Object self, Encoder encoding) {
		if (self == null) {
			return null;
		}
		if (self instanceof BinaryDataSource) {
			OutputStream buffer = new ASCIIBuffer();
			try (OutputStream encoder = encoding.wrap(buffer)) {
				((BinaryDataSource) self).deliverTo(encoder);
			} catch (IOException ex) {
				throw errorEncodingFailed(ex);
			}
			return buffer.toString();
		} else if (self instanceof BinaryContent) {
			
			OutputStream buffer = new ASCIIBuffer();
			try (InputStream data = ((BinaryContent) self).getStream(); OutputStream encoder = encoding.wrap(buffer)) {
				StreamUtilities.copyStreamContents(data, encoder);
			} catch (IOException ex) {
				throw errorEncodingFailed(ex);
			}
			return buffer.toString();
		} else {
			throw new TopLogicException(I18NConstants.ERROR_NOT_A_BINARY_VALUE__VAL_EXPR.fill(self, getArguments()[0]));
		}
	}

	private TopLogicException errorEncodingFailed(IOException ex) {
		return new TopLogicException(
			I18NConstants.ENCODING_FAILED__MSG_EXPR.fill(ex.getMessage(), getArguments()[0]));
	}

	/**
	 * String buffer accepting binary data in ASCII encoding scheme.
	 */
	private static final class ASCIIBuffer extends OutputStream {
		private final StringBuilder _buffer = new StringBuilder();

		@Override
		public void write(int b) throws IOException {
			// In a stream, only 8 bit of the passed value can be set, higher bits must be ignored.
			// However, this stream expects that only 7 bits are in use (ASCII). This allows a
			// direct mapping to Java UTF-16 character representation.
			_buffer.append((char) (b & 0xFF));
		}

		@Override
		public String toString() {
			return _buffer.toString();
		}
	}

	/**
	 * {@link MethodBuilder} for {@link Base64Encode}.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<Base64Encode> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.optional("input")
			.optional("mime", false)
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public Base64Encode build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new Base64Encode(getName(), args);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

	}
}
