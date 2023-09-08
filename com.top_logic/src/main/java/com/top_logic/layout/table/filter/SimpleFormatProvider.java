/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.text.Format;

import com.top_logic.layout.table.model.ColumnConfiguration;

/**
 * {@link FormatProvider}, that is a pure wrapper for a given {@link Format}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class SimpleFormatProvider implements FormatProvider {

	private Format _format;

	/**
	 * Create a new {@link SimpleFormatProvider}.
	 */
	public SimpleFormatProvider(Format format) {
		_format = format;
	}

	@Override
	public Format getFormat(ColumnConfiguration column) {
		return _format;
	}

}
