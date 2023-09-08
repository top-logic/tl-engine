/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.writer;

import com.top_logic.basic.StringServices;

/**
 * Simple implementation of the {@link TemplateWriter} interface that writes to a
 * {@link StringBuilder}.
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class TemplateStringBufferWriter extends TemplateWriter {
	
	private StringBuilder buffer;

	/**
	 * Creates a new {@link TemplateStringBufferWriter}.
	 */
	public TemplateStringBufferWriter() {
		this.buffer = new StringBuilder();
	}
	
	/**
	 * Returns the string representation of this writers internal {@link StringBuilder}.
	 * 
	 * @return the internal StringBuilder as a {@link String} 
	 */
	public String getBufferAsString() {
		return buffer.toString();
	}

	@Override
	public void writeLiteral(String aLiteral) {
		buffer.append(quote(aLiteral));

	}

	@Override
	public void writeValue(Object aValue) {
		buffer.append(quote(StringServices.toString(aValue)));
	}

}
