/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.persistency.attribute.tempate;

import java.io.StringReader;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.dom.Expr.Html;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;
import com.top_logic.model.search.expr.parser.SearchExpressionParserConstants;
import com.top_logic.model.search.expr.parser.TokenMgrError;
import com.top_logic.model.search.persistency.attribute.AbstractExprMapping;

/**
 * {@link StorageMapping} that stores text content with embedded expressions.
 * 
 * <p>
 * The resulting {@link SearchExpression} is a function taking a model object and rendering the text
 * macro in the context of this model object. The source code is expected to be plain text with
 * embedded expressions (using the <code>{expr}</code> syntax. The embedded expression may reference
 * the model object using a variable called <code>model</code>.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTemplateStorageMapping extends AbstractExprMapping {

	private static final String MODEL_VAR = "model";

	@Override
	protected SearchExpression compile(String source) throws ConfigurationException {
		SearchExpressionParser parser = new SearchExpressionParser(new StringReader(source));
		parser.token_source.SwitchTo(SearchExpressionParserConstants.HTML);
		configureParser(parser);
		try {
			Html contents = parser.html();
			ExprFormat.checkFullyRead(null, source, parser);

			Expr fun = parser.getFactory().lambda(MODEL_VAR, contents);
			return compile(source, fun);
		} catch (I18NRuntimeException ex) {
			throw new ConfigurationException(ex.getErrorKey(), null, source, ex);
		} catch (TokenMgrError ex) {
			throw ExprFormat.handleSyntaxError(null, source, ex);
		} catch (ParseException ex) {
			throw ExprFormat.handleParseException(null, source, ex);
		}
	}

	/**
	 * Hook to configure the given parser.
	 */
	@SuppressWarnings("unused")
	protected void configureParser(SearchExpressionParser parser) {
		// Additional parser configuration.
	}

}
