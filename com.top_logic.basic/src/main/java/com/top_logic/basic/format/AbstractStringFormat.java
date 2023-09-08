/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

/**
 * Base class for {@link Format}s where source type is {@link String}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractStringFormat extends Format {

    @Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        return toAppendTo.append(obj == null ? "" : format(obj.toString()));
    }

    @Override
	public Object parseObject(String source, ParsePosition pos) {
        pos.setIndex(source.length());
        return format(source);
    }

	/**
	 * Processes the input string (same for format and parse).
	 */
    protected abstract String format(String input);
    
}
