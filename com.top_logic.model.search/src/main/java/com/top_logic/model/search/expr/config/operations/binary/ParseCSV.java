/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.binary;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
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
	 * Position of the input csv file (for parsing) in arguments
	 */
	private static final int INPUT_DATA_INDEX = 0;

	/**
	 * Position of given parsers in arguments
	 */
	private static final int PARSERS_INDEX = 1;

	/**
	 * Position of selected column separator in arguments
	 */
	private static final int COLUMN_SEPARATOR_INDEX = 2;

	/**
	 * Position of selected line separator in arguments
	 */
	private static final int LINE_SEPARATOR_INDEX = 3;

	/**
	 * Position of selected quote char in arguments
	 */
	private static final int QUOTE_CHAR_INDEX = 4;

	/**
	 * Position of the Boolean, which indicates if the spaces around separator characters are to be
	 * automatically trimmed before being reported or not in arguments.
	 */
	private static final int TRIM_SPACES_INDEX = 5;

	/**
	 * Position of the Boolean, which indicates if the raw result of parsing is returned (as a List
	 * of Strings).
	 */
	private static final int RAW_INDEX = 6;

	/**
	 * Default separator for column.
	 */
	private static final char DEFAULT_COLUMN_SEPARATOR = ',';

	/**
	 * Default separator for lines.
	 */
	private static final String DEFAULT_LINE_SEPARATOR = "\n";

	/**
	 * Default separator for column.
	 */
	private static final char DEFAULT_QUOTE_CHAR = '"';

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
		Object input = arguments[INPUT_DATA_INDEX];
		if (input == null) {
			return null;
		}

		char columnSeparator = asChar(arguments[COLUMN_SEPARATOR_INDEX], DEFAULT_COLUMN_SEPARATOR);
		String lineSeparator = asString(arguments[LINE_SEPARATOR_INDEX], DEFAULT_LINE_SEPARATOR);
		char quoteChar = asChar(arguments[QUOTE_CHAR_INDEX], DEFAULT_QUOTE_CHAR);
		Boolean trimSpaces = asBoolean(arguments[TRIM_SPACES_INDEX]);

		List<List<String>> result = parse(input, columnSeparator, lineSeparator, quoteChar, trimSpaces);

		Boolean raw = asBoolean(arguments[RAW_INDEX]);
		if (raw == true) {
			return result;
		} else {
			List<Map<String, Object>> mapedResult = new ArrayList<>();
			Map<String, SearchExpression> parsers = createParseMap(arguments);
			List<String> header = result.get(0);
			for (int i = 1; i < result.size(); ++i) {
				List<String> row = result.get(i);
				Map<String, Object> maped = new HashMap<>();

				for (int j = 0; j < header.size(); ++j) {
					String key = header.get(j);
					if (j < row.size()) {
						String rawValue = row.get(j);
						if (parsers.containsKey(key)) {
							SearchExpression parser = parsers.get(key);
							maped.put(key, parser.eval(definitions, rawValue));

						} else {
							maped.put(key, rawValue);
						}
					} else {
						maped.put(key, "");
					}
				}
				mapedResult.add(maped);
			}
			return mapedResult;
		}
	}

	private Map<String, SearchExpression> createParseMap(Object[] arguments) {
		Map<String, SearchExpression> result = new HashMap<>();
		if (arguments[PARSERS_INDEX] != null) {
			Map<?, ?> parsers = asMap(arguments[PARSERS_INDEX]);
			for (Entry<?, ?> entry : parsers.entrySet()) {
				String column = asString(entry.getKey());
				SearchExpression parser = asSearchExpression(entry.getValue());
				result.put(column, parser);
			}
		}
		return result;
	}

	private List<List<String>> parse(Object input, char columnSeparator, String lineSeparator, char quoteChar,
			Boolean trimSpaces) {
		try {
			BinaryDataSource data = (BinaryDataSource) input;

			MimeType mimeType = new MimeType(data.getContentType());
			String charset = mimeType.getParameter("charset");
			if (charset == null) {
				charset = ISO_8859;
			}

			try (InputStream in = data.toData().getStream()) {
				CsvSchema csvSchema = createSchema(columnSeparator, lineSeparator, quoteChar);
				InputStreamReader streamReader = new InputStreamReader(in, charset);
				MappingIterator<List<String>> iterator = createIterator(streamReader, csvSchema, trimSpaces);
				List<List<String>> all = iterator.readAll();
				return all;
			}
		} catch (IOException | MimeTypeParseException ex) {
			throw new TopLogicException(I18NConstants.ERROR_CONVERSION_FAILED__MSG_EXPR.fill(ex.getMessage(), this),
				ex);
		}
	}

	private char asChar(Object value, char defaultValue) {
		String valueSting = asString(value, null);
		if (valueSting == null) {
			return defaultValue;
		} else {
			return valueSting.charAt(0);
		}
	}

	private CsvSchema createSchema(char columnSeparator, String lineSeparator, char quoteChar) {
		CsvSchema csvSchema = CsvSchema.emptySchema();

		csvSchema = csvSchema
			.withColumnSeparator(columnSeparator)
			.withLineSeparator(lineSeparator)
			.withQuoteChar(quoteChar);

		return csvSchema;
	}

	private MappingIterator<List<String>> createIterator(InputStreamReader streamReader, CsvSchema csvSchema,
			Boolean trimSpaces) {
		CsvMapper csvMapper = new CsvMapper();
		MappingIterator<List<String>> iterator = null;
		try {
			if (trimSpaces == true) {
				iterator = csvMapper
					.readerForListOf(String.class)
					.with(CsvParser.Feature.WRAP_AS_ARRAY)
					.with(CsvParser.Feature.TRIM_SPACES)
					.with(csvSchema)
					.readValues(streamReader);

			} else {
				iterator = csvMapper
					.readerForListOf(String.class)
					.with(CsvParser.Feature.WRAP_AS_ARRAY)
					.with(csvSchema)
					.readValues(streamReader);
			}

			return iterator;
		} catch (IOException ex) {
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
			.optional("parsers")
			.optional("columnSeparator", String.valueOf(DEFAULT_COLUMN_SEPARATOR))
			.optional("lineSeparator", DEFAULT_LINE_SEPARATOR)
			.optional("quoteChar", String.valueOf(DEFAULT_QUOTE_CHAR))
			.optional("trimSpaces", false)
			.optional("raw")
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
