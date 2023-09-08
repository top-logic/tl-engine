/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.writer;


/**
 * A {@link Quoter} that does quote nothing.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class PlainTextQuoter extends Quoter {

	/** Creates a new {@link PlainTextQuoter}. */
	public PlainTextQuoter() {
		super(State.NO_QUOTING);
	}

	@Override
	protected void quoteAttributeValue(String unquoted, StringBuilder resultBuilder) {
		resultBuilder.append(unquoted);
	}

	@Override
	protected void quoteNormalText(String unquoted, StringBuilder resultBuilder) {
		resultBuilder.append(unquoted);
	}

}
