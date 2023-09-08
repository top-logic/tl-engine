/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import com.top_logic.basic.StringServices;

/**
 * Completion for the ACE code editor.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class CodeCompletion {

	private String _name = StringServices.EMPTY_STRING;

	private String _value = StringServices.EMPTY_STRING;

	private String _snippet = StringServices.EMPTY_STRING;

	private String _docHTML = StringServices.EMPTY_STRING;

	private int _score = 1;

	private Object _object;

	/**
	 * Creates a {@link CodeCompletion}.
	 */
	public CodeCompletion() {
		// Nothing to do.
	}

	/**
	 * Completion name
	 */
	public String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * Displayed value displayed in the code completion suggestions.
	 */
	public String getValue() {
		return _value;
	}

	/**
	 * @see #getValue()
	 */
	public void setValue(String value) {
		_value = value;
	}

	/**
	 * Inserted snippet if this code completion is used.
	 */
	public String getSnippet() {
		return _snippet;
	}

	/**
	 * @see #getSnippet()
	 */
	public void setSnippet(String snippet) {
		_snippet = snippet;
	}

	/**
	 * HTML Documentation for this code completion.
	 */
	public String getDocHTML() {
		return _docHTML;
	}

	/**
	 * @see #getDocHTML()
	 */
	public void setDocHTML(String docHTML) {
		_docHTML = docHTML;
	}

	/**
	 * Score after which all code completions are ordered.
	 */
	public int getScore() {
		return _score;
	}

	/**
	 * @see #getScore()
	 */
	public void setScore(int score) {
		_score = score;
	}

	/**
	 * Business object to this code completion.
	 */
	public Object getObject() {
		return _object;
	}

	/**
	 * @see #getObject()
	 */
	public void setObject(Object object) {
		_object = object;
	}
}
