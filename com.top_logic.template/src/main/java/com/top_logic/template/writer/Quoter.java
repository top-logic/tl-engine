/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.writer;


/**
 * Quotes text according to the current "semantic position".
 * <p>
 * "Semantic position" means in XML for example whether the current text is an attribute value.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class Quoter {

	/** State of the document that is being written. */
	public enum State {

		/** Text within an attribute value. */
		ATTRIBUTE_VALUE,

		/** "Normal" text that needs to be quoted. In XML "normal" text means text between tags. */
		NORMAL_TEXT,

		/** Text should not be quoted. */
		NO_QUOTING

	}

	private State _state = State.NO_QUOTING;

	/** Creates a new {@link Quoter} with the given initial {@link State}. */
	public Quoter(State initialState) {
		_state = initialState;
	}

	/**
	 * Quotes the given text according to the current "semantic position".
	 * <p>
	 * "Semantic position" means in XML for example whether the current text is an attribute value.
	 * </p>
	 */
	public String quote(String unquoted) {
		switch (_state) {
			case ATTRIBUTE_VALUE: {
				StringBuilder resultBuilder = new StringBuilder();
				quoteAttributeValue(unquoted, resultBuilder);
				return resultBuilder.toString();
			}
			case NORMAL_TEXT: {
				StringBuilder resultBuilder = new StringBuilder();
				quoteNormalText(unquoted, resultBuilder);
				return resultBuilder.toString();
			}
			case NO_QUOTING: {
				return unquoted;
			}
			default: {
				throw new UnsupportedOperationException("Unknown state: " + _state);
			}
		}
	}

	/** Quotes the given text as attribute value. */
	protected abstract void quoteAttributeValue(String unquoted, StringBuilder resultBuilder);

	/** Quotes the given text as "normal" text. */
	protected abstract void quoteNormalText(String unquoted, StringBuilder resultBuilder);

	/** Setter for the {@link State}. */
	public void setState(State newState) {
		_state = newState;
	}

}
