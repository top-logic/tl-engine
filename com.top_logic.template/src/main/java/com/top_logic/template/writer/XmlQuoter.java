/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.writer;

import com.top_logic.basic.xml.TagUtil;

/**
 * A {@link Quoter} that quotes text that should be written into XML documents.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class XmlQuoter extends Quoter {

	/** Creates a new {@link XmlQuoter} with the given initial {@link Quoter.State}. */
	public XmlQuoter(State initialState) {
		super(initialState);
	}

	@Override
	protected void quoteAttributeValue(String unquoted, StringBuilder resultBuilder) {
		TagUtil.writeAttributeText(resultBuilder, unquoted);
	}

	@Override
	protected void quoteNormalText(String unquoted, StringBuilder resultBuilder) {
		TagUtil.writeText(resultBuilder, unquoted);
	}

}
