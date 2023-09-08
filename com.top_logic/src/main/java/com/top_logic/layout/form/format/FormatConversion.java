/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

/**
 * {@link Format} that applies a conversion to objects formatted and parsed by a
 * target format.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class FormatConversion extends Format {

	private final Format targetFormat;
	
	/**
	 * Construct a new {@link FormatConversion} for the given target {@link Format}.
	 */
	public FormatConversion(Format targetFormat) {
		this.targetFormat = targetFormat;
	}
	
	@Override
	public final StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		return targetFormat.format(convertToTargetFormat(obj), toAppendTo, pos);
	}

	@Override
	public final Object parseObject(String source, ParsePosition pos) {
		return convertFromTargetFormat(targetFormat.parseObject(source, pos));
	}

	protected abstract Object convertToTargetFormat(Object obj);
	protected abstract Object convertFromTargetFormat(Object parseObject);

}
