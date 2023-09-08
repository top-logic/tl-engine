/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.model;


import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.template.I18NConstants;
import com.top_logic.template.parser.ParseException;
import com.top_logic.template.parser.TemplateParser;
import com.top_logic.template.parser.Token;
import com.top_logic.template.parser.TokenMgrError;
import com.top_logic.template.tools.CheckUtils;
import com.top_logic.util.Resources;

/**
 * A TemplateProblem describes any kind of problem that occurred during the parsing and checking
 * process of a template.
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class TemplateProblem {
	
	private final ResKey resKey;
	private final int      colBegin;
	private final int      colEnd;
	private final int      rowBegin;
	private final int      rowEnd;
	private final String   name;
	private final Type     type;
	
	public enum Type {
		/**
		 * A SYNTAX_ERROR denotes a fatal problem in the given template.
		 */
		SYNTAX_ERROR,
		
		/**
		 * A SYNTAX_WARNING denotes a problem in the template. A warning always occurs as a follow
		 * up of a SYNTAX_ERROR.
		 */
		SYNTAX_WARNING,
		
		/**
		 * A MODEL_ERROR denotes a fatal problem in the logic the underlying model is used in a
		 * template.
		 */
		MODEL_ERROR,
		
		/**
		 * A MODEL_WARNING denotes a problem in the logic the underlying model is used in a
		 * template. Even though it will still work, the results might be unexpected or not the ones
		 * intended.
		 */
		MODEL_WARNING,
		
		/**
		 * A TOK_MGR_ERROR denotes an error that occurred during the parsing process of
		 * the {@link TemplateParser} and was caused by a {@link TokenMgrError}.
		 */
		TOK_MGR_ERROR,
		
		/**
		 * A PARSE_ERROR denotes a problem during the parsing process of the {@link TemplateParser}
		 * and was caused by a {@link ParseException}.
		 */
		PARSE_ERROR
	}
	
	/**
	 * C'tor
	 * 
	 * @param resKey
	 *        Resource key filled with <code>name</code>, <code>rowBegin</code> and
	 *        <code>colBegin</code>.
	 * 
	 * @see #TemplateProblem(ResKey, String, Type, int, int, int, int)
	 */
	public TemplateProblem(ResKey3 resKey, String name, Type type, int rowBegin, int colBegin, int rowEnd, int colEnd) {
		this(resKey.fill(name, rowBegin, colBegin), name, type, rowBegin, colBegin, rowEnd, colEnd);
	}

	/**
	 * C'tor
	 * 
	 * @param resKey
	 *        resource key without the typed prefix
	 * @param name
	 *        the name of the node the problem occurred
	 * @param type
	 *        the {@link Type} of the problem
	 * @param rowBegin
	 *        the row, the problem occurred in
	 * @param colBegin
	 *        the column, the problem occurred in
	 */
	public TemplateProblem(ResKey resKey, String name, Type type, int rowBegin, int colBegin, int rowEnd, int colEnd) {
		this.resKey = resKey;
		this.name = name;
		this.type = type;
		this.rowBegin = rowBegin;
		this.colBegin = colBegin;
		this.rowEnd = rowEnd;
		this.colEnd = colEnd;
	}
	
	/** 
	 * Create a new TemplateProblem from a caught {@link ParseException}
	 * 
	 * @param aPE a {@link ParseException}
	 */
	public TemplateProblem(ParseException aPE) {
		this.type     = Type.PARSE_ERROR;
		
		Token nextToken = aPE.currentToken.next;
		
		this.rowBegin = nextToken.beginLine;
		this.colBegin = nextToken.beginColumn;
		this.colEnd   = nextToken.endColumn;
		this.rowEnd   = nextToken.endLine;
		this.name     = nextToken.image;

		String[] theExpectedTokens = CheckUtils.getProblemTokens(aPE);
		
		this.resKey =
			I18NConstants.PARSER_ERROR__EXPECTED_ENCOUNTERED_ROW_COL.fill(theExpectedTokens[0],
				theExpectedTokens[1], aPE.currentToken.next.beginLine, aPE.currentToken.next.beginColumn);
	}

	/** 
	 * Create a new TemplateProblem from a caught {@link TokenMgrError}.
	 * 
	 * @param err a {@link TokenMgrError}
	 */
	public TemplateProblem(TokenMgrError err) {
		this.type     = Type.TOK_MGR_ERROR;
		this.rowBegin = err.getErrorLine();
		this.colBegin = err.getErrorColumn();
		this.colEnd   = -1;
		this.rowEnd   = -1;
		this.name     = "";
		this.resKey = Resources.encodeLiteralText(err.getMessage());
	}

	/**
	 * Returns the (internationalized) description of this problem.
	 */
	public final ResKey getErrorDescription() {
		switch (this.type) {
			case PARSE_ERROR:
				return this.resKey;
			case TOK_MGR_ERROR:
				return this.resKey;
			default:
				return this.resKey;
		}
	}
	
	public final ResKey getResKey() {
		return this.resKey;
	}

	/** 
	 * Returns the first column of the template where the problem occurred.
	 * 
	 * @return the column number (1-based) or -1 if not set.
	 */
	public final int getColumnBegin() {
		return this.colBegin;
	}

	/** 
	 * Returns the last column of the template where the problem occurred.
	 * 
	 * @return the column number (1-based) or -1 if not set.
	 */
	public final int getColumnEnd() {
		return this.colEnd;
	}

	/** 
	 * Returns the first row of the template where the problem occurred.
	 * 
	 * @return the row number (1-based) or -1 if not set.
	 */
	public final int getRowBegin() {
		return this.rowBegin;
	}

	
	/** 
	 * Returns the last row of the template where the problem occurred.
	 * 
	 * @return the row number (1-based) or -1 if not set.
	 */
	public final int getRowEnd() {
		return this.rowEnd;
	}

	public final String getName() {
		return this.name;
	}
	
	/** 
	 * Returns the {@link Type} of the problem.
	 */
	public final Type getType() {
		return this.type;
	}
}
