/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config;

import java.io.StringReader;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.dom.Expr.Wrapped;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;
import com.top_logic.model.search.expr.parser.SearchExpressionParserConstants;
import com.top_logic.model.search.expr.parser.TokenMgrError;

/**
 * {@link ConfigurationValueProvider} that allows configuring {@link Expr TL-Script} expressions
 * creating a string from a text with embedded expressions.
 * 
 * <p>
 * The source text may contain one pre-defined variable in embedded expressions, which is given in
 * {@link TextWithEmbeddedExpressionsFormat#TextWithEmbeddedExpressionsFormat(String)}.
 * </p>
 * 
 * @see MacroFormat
 */
public class TextWithEmbeddedExpressionsFormat extends AbstractConfigurationValueProvider<Expr> {

	/** Name of the TL-Script variable in the macro. */
	public static final String MODEL = "model";

	/**
	 * Singleton {@link TextWithEmbeddedExpressionsFormat} instance, using {@link #MODEL} as parameter name.
	 */
	public static final TextWithEmbeddedExpressionsFormat INSTANCE = new TextWithEmbeddedExpressionsFormat(MODEL);

	private final String _parameter;

	/**
	 * Creates a new {@link TextWithEmbeddedExpressionsFormat}.
	 * 
	 * @param parameter
	 *        The name of the pre-defined variable that can be accessed in embedded expressions.
	 */
	public TextWithEmbeddedExpressionsFormat(String parameter) {
		super(Expr.class);
		_parameter = parameter;
	}

	@Override
	protected Expr getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		String value = propertyValue.toString();

		SearchExpressionParser parser = new SearchExpressionParser(new StringReader(value));
		parser.token_source.SwitchTo(SearchExpressionParserConstants.TEXT);

		Expr text;
		try {
			text = parser.textWithEmbeddedExpressions();
			ExprFormat.checkFullyRead(propertyName, propertyValue, parser);
		} catch (ParseException ex) {
			throw ExprFormat.handleParseException(propertyName, propertyValue, ex);
		} catch (TokenMgrError ex) {
			throw ExprFormat.handleSyntaxError(propertyName, propertyValue, ex);
		}

		Expr expr = parser.getFactory().lambda(_parameter, text);

		Wrapped wrapped = TypedConfiguration.newConfigItem(Wrapped.class);
		wrapped.setSrc(value);
		wrapped.setExpr(expr);

		return wrapped;
	}

	@Override
	protected String getSpecificationNonNull(Expr configValue) {
		return ExprPrinter.toString(configValue);
	}
}