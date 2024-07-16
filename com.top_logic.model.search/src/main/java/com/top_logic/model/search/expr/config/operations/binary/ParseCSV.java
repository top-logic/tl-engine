/*
 * Copyright (c) 2024 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.model.search.expr.config.operations.binary;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.CSVReader;
import com.top_logic.basic.io.binary.BinaryDataSource;
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
 * {@link GenericMethod} parsing a CSV-File.
 */
public class ParseCSV extends GenericMethod {

	/**
	 * iso-8859-1 constant for reading Strings.
	 */
	public static final String ISO_8859 = "iso-8859-1";

	/**
	 * Creates a {@link ParseCSV}.
	 */
	protected ParseCSV(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ParseCSV(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object input = arguments[0];
		if (input == null) {
			return null;
		}


		try {
			BinaryDataSource data = (BinaryDataSource) input;

			MimeType mimeType = new MimeType(data.getContentType());
			String charset = mimeType.getParameter("charset");
			if (charset == null) {
				charset = ISO_8859;
			}

			try (InputStream in = data.toData().getStream()) {
				InputStreamReader streamReader = new InputStreamReader(in, charset);
				CSVReader csvReader = new CSVReader(streamReader, ';');
				return csvReader.readAllLines();
			}
		} catch (IOException | MimeTypeParseException ex) {
			throw new TopLogicException(I18NConstants.ERROR_CONVERSION_FAILED__MSG_EXPR.fill(ex.getMessage(), this),
				ex);
		}
	}


	/**
	 * {@link MethodBuilder} for {@link ParseCSV}.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<ParseCSV> {

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
		public ParseCSV build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new ParseCSV(getName(), args);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}
	}

}
