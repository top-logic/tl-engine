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
 * Two constructs bind a variable in TL-Script:
 * </p>
 * <ul>
 * <li>A lambda <code>name -&gt; body</code> (referenced as <code>$name</code>): <code>name</code> is
 * in scope only within <code>body</code>, i.e. until the enclosing <code>,</code>, <code>;</code> or
 * closing bracket.</li>
 * <li>An assignment <code>name = expr;</code>: <code>name</code> is a local variable in scope in the
 * following statements of the same block, until the block's closing bracket (but not within its own
 * right-hand side).</li>
 * </ul>
 *
 * <p>
 * The in-scope set is determined from the source text up to the cursor by a single pass over the
 * token stream produced by the TL-Script lexer.
 * </p>
 */
public class TLScriptVariableScope {

	/**
	 * The variable bindings active at one bracket-nesting level.
	 */
	private static final class Frame {

		/**
		 * Lambda parameters bound in the statement/argument currently being scanned at this level;
		 * cleared at the next <code>,</code> or <code>;</code>.
		 */
		final Set<String> _lambdaVars = new LinkedHashSet<>();

		/**
		 * Variables assigned with <code>name = ...;</code> in the block at this level; they persist
		 * across the following <code>;</code>-separated statements until this frame is dropped.
		 */
		final Set<String> _assignVars = new LinkedHashSet<>();

		/**
		 * Name of a <code>name =</code> assignment whose defining statement has not yet ended, or
		 * <code>null</code>. Committed to {@link #_assignVars} when the terminating <code>;</code> is
		 * reached, so the variable is not offered within its own right-hand side.
		 */
		String _pendingAssign = null;
	}

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

		Deque<Frame> stack = new ArrayDeque<>();
		stack.push(new Frame());

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
						stack.push(new Frame());
						break;
					case ")":
					case "]":
					case "}":
						if (stack.size() > 1) {
							stack.pop();
						}
						break;
					case ",":
						stack.peek()._lambdaVars.clear();
						break;
					case ";":
						endStatement(stack.peek());
						break;
					case "->":
						String param = lambdaParameter(prev, prevPrev);
						if (param != null) {
							stack.peek()._lambdaVars.add(param);
						}
						break;
					case "=":
						if (prev != null && prev.kind == SearchExpressionParserConstants.NAME) {
							stack.peek()._pendingAssign = prev.image;
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
		for (Frame frame : stack) {
			result.addAll(frame._assignVars);
			result.addAll(frame._lambdaVars);
		}
		return new ArrayList<>(result);
	}

	/**
	 * Ends the current statement: commits a pending assignment to the block scope and drops the
	 * lambda parameters that were only in scope within the just-finished statement.
	 *
	 * <p>
	 * Called when a <code>;</code> is encountered. Committing the pending assignment only here (rather
	 * than when the <code>name =</code> is seen) ensures the assigned variable becomes visible for the
	 * following statements but not within its own right-hand side.
	 * </p>
	 *
	 * @param frame
	 *        The frame of the block in which the statement was terminated.
	 */
	private static void endStatement(Frame frame) {
		if (frame._pendingAssign != null) {
			frame._assignVars.add(frame._pendingAssign);
			frame._pendingAssign = null;
		}
		frame._lambdaVars.clear();
	}

	/**
	 * Determines the variable name bound by a lambda whose <code>-&gt;</code> token was just read.
	 *
	 * <p>
	 * Handles both the plain form <code>name -&gt; body</code> and the optional-parameter form used in
	 * tuple coordinates, <code>name? -&gt; body</code>.
	 * </p>
	 *
	 * @param prev
	 *        The token immediately preceding the <code>-&gt;</code>, or <code>null</code> if there is
	 *        none.
	 * @param prevPrev
	 *        The token before {@code prev}, or <code>null</code>. Needed to look past a <code>?</code>
	 *        in the optional-parameter form.
	 * @return The bound parameter name, or <code>null</code> if the tokens before the
	 *         <code>-&gt;</code> are not a valid lambda-parameter position.
	 */
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

	/**
	 * Removes the partially typed variable reference (<code>$</code> optionally followed by identifier
	 * characters) at the very end of the given text.
	 *
	 * <p>
	 * The text ends at the cursor, where the user is typing the <code>$name</code> to be completed.
	 * That incomplete reference must be dropped before tokenizing: a bare <code>$</code> is not a
	 * valid token and would otherwise cause a lexer error.
	 * </p>
	 *
	 * @param text
	 *        The script source up to the cursor.
	 * @return The text without its trailing <code>$...</code> fragment.
	 */
	private static String stripTrailingVariable(String text) {
		return text.replaceFirst("\\$\\w*\\z", "");
	}

}
