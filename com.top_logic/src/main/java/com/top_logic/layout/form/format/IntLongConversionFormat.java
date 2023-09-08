/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.format;

import java.text.Format;

/**
 * Format that adapts a {@link Format} working on {@link Integer} objects as a
 * {@link Format} working on {@link Long} objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IntLongConversionFormat extends FormatConversion {

	/**
	 * Constructs a new {@link IntLongConversionFormat} for the given target format. 
	 */
	public IntLongConversionFormat(Format targetFormat) {
		super(targetFormat);
	}

	@Override
	protected Object convertFromTargetFormat(Object parseObject) {
		if (parseObject == null) {
			// Parse error.
			return null;
		}
		return Long.valueOf(((Integer) parseObject).longValue());
	}

	@Override
	protected Object convertToTargetFormat(Object obj) {
		return Integer.valueOf(((Long) obj).intValue());
	}

}
