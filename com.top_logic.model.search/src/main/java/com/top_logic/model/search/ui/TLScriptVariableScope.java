/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.model.search.expr.parser.SearchExpressionParserConstants;
import com.top_logic.model.search.expr.parser.SearchExpressionParserTokenManager;
import com.top_logic.model.search.expr.parser.SimpleCharStream;
import com.top_logic.model.search.expr.parser.Token;
import com.top_logic.model.search.expr.parser.TokenMgrError;

/**
 * Computes the TL-Script variables that are in scope at a given cursor position.
 *
 * <p>
 * The only construct binding a variable in TL-Script is a lambda
 * (<code>name -&gt; body</code>, referenced as <code>$name</code>). A variable is in scope exactly
 * while the cursor is inside the body of the lambda that bound it. This analyzer determines that set
 * from the source text up to the cursor by a single pass over the token stream produced by the
 * TL-Script lexer.
 * </p>
 */
public class TLScriptVariableScope {

	/**
	 * The variable names (without the leading <code>$</code>) that are in scope at the end of the
	 * given text.
	 *
	 * @param textToCursor
	 *        The script source from the beginning up to (and including) the cursor position. A
	 *        trailing, partially typed variable reference (e.g. <code>$fo</code>) is ignored.
	 * @return The in-scope variable names, de-duplicated. Never <code>null</code>.
	 */
	public static List<String> inScopeVariables(String textToCursor) {
		String cleaned = stripTrailingVariable(textToCursor);

		Deque<Set<String>> stack = new ArrayDeque<>();
		stack.push(new LinkedHashSet<>());

		SimpleCharStream stream = new SimpleCharStream(new StringReader(cleaned));
		SearchExpressionParserTokenManager tokens = new SearchExpressionParserTokenManager(stream);

		Token prev = null;
		Token prevPrev = null;
		try {
			for (Token token = tokens.getNextToken(); token.kind != SearchExpressionParserConstants.EOF; token =
				tokens.getNextToken()) {
				switch (token.image) {
					case "(":
					case "[":
					case "{":
						stack.push(new LinkedHashSet<>());
						break;
					case ")":
					case "]":
					case "}":
						if (stack.size() > 1) {
							stack.pop();
						}
						break;
					case ",":
						stack.peek().clear();
						break;
					case "->":
						String name = lambdaParameter(prev, prevPrev);
						if (name != null) {
							stack.peek().add(name);
						}
						break;
					default:
						break;
				}
				prevPrev = prev;
				prev = token;
			}
		} catch (TokenMgrError error) {
			// Incomplete or invalid input while editing: use the bindings collected so far.
		}

		Set<String> result = new LinkedHashSet<>();
		for (Set<String> frame : stack) {
			result.addAll(frame);
		}
		return new ArrayList<>(result);
	}

	private static String lambdaParameter(Token prev, Token prevPrev) {
		if (prev == null) {
			return null;
		}
		if (prev.kind == SearchExpressionParserConstants.NAME) {
			return prev.image;
		}
		// Optional-parameter form used in tuple coordinates: "name? -> body".
		if ("?".equals(prev.image) && prevPrev != null
			&& prevPrev.kind == SearchExpressionParserConstants.NAME) {
			return prevPrev.image;
		}
		return null;
	}

	private static String stripTrailingVariable(String text) {
		return text.replaceFirst("\\$\\w*\\z", "");
	}

}
