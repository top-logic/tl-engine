/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config;

import java.io.StringReader;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.dom.Expr.Wrapped;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;
import com.top_logic.model.search.expr.parser.SearchExpressionParserConstants;
import com.top_logic.model.search.expr.parser.Token;
import com.top_logic.model.search.expr.parser.TokenMgrError;

/**
 * {@link ConfigurationValueProvider} that allows configuring {@link Expr TL-Script} expressions.
 * 
 * @see SearchExpressionParser
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExprFormat extends AbstractConfigurationValueProvider<Expr> {

	/**
	 * Singleton {@link ExprFormat} instance.
	 */
	public static final ExprFormat INSTANCE = new ExprFormat();

	private ExprFormat() {
		super(Expr.class);
	}

	@Override
	protected Expr getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		String value = propertyValue.toString();
		SearchExpressionParser parser =
			new SearchExpressionParser(new StringReader(value));

		Expr result;
		try {
			result = parser.expr();
			checkFullyRead(propertyName, propertyValue, parser);
		} catch (ParseException ex) {
			throw handleParseException(propertyName, propertyValue, ex);
		} catch (TokenMgrError ex) {
			throw handleSyntaxError(propertyName, propertyValue, ex);
		}

		Wrapped wrapped = TypedConfiguration.newConfigItem(Wrapped.class);
		wrapped.setSrc(value);
		wrapped.setExpr(result);

		return wrapped;
	}

	/**
	 * Translates {@link ParseException} into a {@link ConfigurationException}.
	 *
	 * @param propertyName
	 *        See {@link #getValueNonEmpty(String, CharSequence)}.
	 * @param propertyValue
	 *        See {@link #getValueNonEmpty(String, CharSequence)}
	 * @param ex
	 *        The thrown exception from the parser.
	 * @throws ConfigurationException
	 *         The new exception.
	 */
	public static ConfigurationException handleParseException(String propertyName, CharSequence propertyValue,
			ParseException ex)
			throws ConfigurationException {
		throw new ConfigurationException(
			withLocation(ex.currentToken.next, I18NConstants.ERROR_EXPRESSION_SYNTAX__DETAILS.fill(ex.getMessage())),
			propertyName, propertyValue, ex);
	}

	private static ResKey withLocation(Token token, ResKey msg) {
		return com.top_logic.basic.xml.log.I18NConstants.AT_LOCATION__LINE_COL_DETAIL.fill(
			token.beginLine, token.beginColumn, msg);
	}

	/**
	 * Translates {@link TokenMgrError} into a {@link ConfigurationException}.
	 *
	 * @param propertyName
	 *        See {@link #getValueNonEmpty(String, CharSequence)}.
	 * @param propertyValue
	 *        See {@link #getValueNonEmpty(String, CharSequence)}
	 * @param ex
	 *        The thrown exception from the parser.
	 * @throws ConfigurationException
	 *         The new exception.
	 */
	public static ConfigurationException handleSyntaxError(String propertyName, CharSequence propertyValue,
			TokenMgrError ex) throws ConfigurationException {
		throw new ConfigurationException(
			I18NConstants.ERROR_EXPRESSION_SYNTAX__DETAILS.fill(ex.getMessage()),
			propertyName, propertyValue, ex);
	}

	/**
	 * Check that all input has been consumed by the parser.
	 *
	 * @param propertyName
	 *        See {@link #getValueNonEmpty(String, CharSequence)}
	 * @param propertyValue
	 *        See {@link #getValueNonEmpty(String, CharSequence)}
	 * @param parser
	 *        The parser that has finished parsing.
	 * @throws ConfigurationException
	 *         If an error is announced.
	 */
	public static void checkFullyRead(String propertyName, CharSequence propertyValue, SearchExpressionParser parser)
			throws ConfigurationException {
		Token currentToken = parser.token;
		if (currentToken.kind != SearchExpressionParserConstants.EOF) {
			currentToken = parser.getNextToken();
		}
		if (currentToken.kind != SearchExpressionParserConstants.EOF) {
			throw new ConfigurationException(withLocation(currentToken, I18NConstants.ERROR_TAILING_GARBAGE),
				propertyName, propertyValue);
		}
	}

	@Override
	protected String getSpecificationNonNull(Expr configValue) {
		return ExprPrinter.toString(configValue);
	}
}