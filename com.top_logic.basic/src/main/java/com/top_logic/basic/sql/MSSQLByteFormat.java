/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.Types;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

/**
 * Format for MSSQL to format {@link Byte} values.
 * 
 * <p>
 * Byte values are stored in {@link Types#TINYINT tinyint} datatype. This has range 0-255 in MSSQL
 * whereas {@link Byte} in java has range -128 - 127.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MSSQLByteFormat extends Format {

	/** Singleton {@link MSSQLByteFormat} instance. */
	public static final MSSQLByteFormat INSTANCE = new MSSQLByteFormat();

	private MSSQLByteFormat() {
		// singleton instance
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		if (!(obj instanceof Number)) {
			throw new IllegalArgumentException("Only numbers can be parsed: " + obj.getClass());
		}
		int byteValue = ((Number) obj).byteValue();
		/* This implementation is due to the MSSQL in driver implementation. When setting a byte the
		 * driver internally converted to an integer (MSSQL DB has range 0 - 255 instead of -128 -
		 * 127). This is done by doing &0xff which actually adds 256 to negative numbers. */
		byteValue = byteValue & 0xff;
		return toAppendTo.append(byteValue);
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		throw new UnsupportedOperationException();
	}

}

