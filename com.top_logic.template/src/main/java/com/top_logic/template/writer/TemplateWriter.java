/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.writer;

import com.top_logic.template.model.ExpansionModel;

/**
 * This interface describes the methods needed to write an expanded template.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public abstract class TemplateWriter {

	private Quoter _quoter = null;

	/**
	 * Writes a literal. A literal is a plain {@link String} that was found in a parsed template.
	 * 
	 * @param literal the literal to write
	 */
	public abstract void writeLiteral(String literal);
	
	/**
	 * Writes the given {@link Object}. The object was retrieved from the model using an
	 * {@link ExpansionModel}.
	 * 
	 * @param value an {@link Object} to write
	 */
	public abstract void writeValue(Object value);

	/** Getter for the {@link Quoter}. */
	public Quoter getQuoter() {
		return _quoter;
	}

	/** Setter for the {@link Quoter}. */
	public void setQuoter(Quoter quoter) {
		_quoter = quoter;
	}

	/** Quotes the given text if the {@link #_quoter} is set. Returns the text unquoted otherwise. */
	protected String quote(String unquoted) {
		if (_quoter == null) {
			return unquoted;
		} else {
			return _quoter.quote(unquoted);
		}
	}

}
