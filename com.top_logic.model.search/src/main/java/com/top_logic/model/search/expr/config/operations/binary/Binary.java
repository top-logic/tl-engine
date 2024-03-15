/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.binary;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.json.JsonUtilities;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.io.binary.BinaryDataSourceProxy;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.mime.MimeTypesModule;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * Constructs a {@link BinaryDataSource binary value} e.g. for creating a download.
 */
public class Binary extends GenericMethod {

	/**
	 * Creates a {@link Binary}.
	 */
	protected Binary(String name, SearchExpression[] args) {
		super(name, args);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Binary(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.BINARY_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		String name = asString(arguments[0]);
		Object rawData = arguments[1];
		String specifiedContentType = asString(arguments[2], null);
		String specifiedEncoding = asString(arguments[3], null);
		Object rawSize = arguments[4];

		if (rawData instanceof BinaryDataSource) {
			return streamData(name, (BinaryDataSource) rawData, rawSize, specifiedContentType);
		} else {
			return customData(name, rawData, specifiedContentType, specifiedEncoding);
		}
	}

	private Object streamData(String name, BinaryDataSource data, Object rawSize, String specifiedContentType) {
		String contentType;

		if (specifiedContentType == null) {
			String dataType = data.getContentType();
			contentType = dataType != null ? dataType : MimeTypesModule.getInstance().getMimeType(name);
		} else {
			contentType = specifiedContentType;
		}

		long size = rawSize == null ? data.getSize() : asLong(rawSize);

		return new BinaryDataSourceProxy(data) {
			@Override
			public String getName() {
				return name;
			}

			@Override
			public long getSize() {
				return size;
			}

			@Override
			public String getContentType() {
				return contentType;
			}
		};
	}

	private Object customData(String name, Object rawData, String specifiedContentType, String specifiedEncoding) {
		MimeType mimeType = parseContentType(name, specifiedContentType);
		String charset = ensureCharset(specifiedEncoding, mimeType);

		if (rawData instanceof HTMLFragment) {
			return htmlData(name, (HTMLFragment) rawData, mimeType, charset);
		} else if (JsonUtilities.JSON_CONTENT_TYPE.equals(mimeType.getBaseType())) {
			return jsonData(name, rawData, mimeType, charset);
		} else {
			return textData(name, rawData, mimeType, charset);
		}
	}

	private MimeType parseContentType(String fileName, String specifiedContentType) {
		String contentType = ensureContentType(fileName, specifiedContentType);
		try {
			return new MimeType(contentType);
		} catch (MimeTypeParseException ex) {
			throw new TopLogicException(I18NConstants.ERROR_INVALID_CONTENT_TYPE_SPECIFIED__VALUE_MSG
				.fill(contentType, ex.getMessage()));
		}
	}

	private String ensureContentType(String name, String specifiedContentType) {
		if (specifiedContentType == null) {
			return MimeTypesModule.getInstance().getMimeType(name);
		} else {
			return specifiedContentType;
		}
	}

	private String ensureCharset(String specifiedEncoding, MimeType mimeType) {
		if (specifiedEncoding != null) {
			mimeType.setParameter("charset", specifiedEncoding);
			return specifiedEncoding;
		} else {
			String charset = mimeType.getParameter("charset");
			if (charset == null) {
				charset = StringServices.UTF8;
				mimeType.setParameter("charset", charset);
			}
			return charset;
		}
	}

	private Object htmlData(String name, HTMLFragment data, MimeType contentType, String charset) {
		return new CustomData(name, contentType) {
			@Override
			public void deliverTo(OutputStream out) throws IOException {
				try (TagWriter w = new TagWriter(new OutputStreamWriter(out, charset))) {
					data.write(DefaultDisplayContext.getDisplayContext(), w);
				}
			}
		};
	}

	private Object jsonData(String name, Object data, MimeType contentType, String charset) {
		return new CustomData(name, contentType) {
			@Override
			public void deliverTo(OutputStream out) throws IOException {
				try (Writer w = new OutputStreamWriter(out, charset)) {
					JSON.write(w, data);
				}
			}
		};
	}

	private Object textData(String name, Object data, MimeType contentType, String charset) {
		return new CustomData(name, contentType) {
			@Override
			public void deliverTo(OutputStream out) throws IOException {
				try (Writer w = new OutputStreamWriter(out, charset)) {
					w.write(asString(data));
				}
			}
		};
	}

	private abstract static class CustomData implements BinaryDataSource {
		private final String _name;

		private final MimeType _contentType;

		/**
		 * Creates a {@link CustomData}.
		 */
		public CustomData(String name, MimeType contentType) {
			_name = name;
			_contentType = contentType;
		}

		@Override
		public String getName() {
			return _name;
		}

		@Override
		public long getSize() {
			return -1;
		}

		@Override
		public String getContentType() {
			return _contentType.toString();
		}

	}

	/**
	 * {@link MethodBuilder} creating {@link Binary} data.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Binary> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("name")
			.mandatory("data")
			.optional("contentType")
			.optional("encoding")
			.optional("size")
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
		public Binary build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new Binary(getConfig().getName(), args);
		}

	}

}
