/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import com.top_logic.basic.io.BinaryContent;

/**
 * Base class for {@link Format} implementations creating binary literals.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractBlobFormat extends Format {

	private static final char[] OCTETS = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	};

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		appendStartQuote(toAppendTo);
		appendContent(toAppendTo, obj);
		appendStopQuote(toAppendTo);
		return toAppendTo;
	}

	/**
	 * Append the quoting start sequence to the given buffer.
	 */
	protected abstract void appendStopQuote(StringBuffer buffer);

	/**
	 * Append the quoting stop sequence to the given buffer.
	 */
	protected abstract void appendStartQuote(StringBuffer buffer);

	private void appendContent(StringBuffer buffer, Object obj) {
		if (obj instanceof byte[]) {
			for (byte val : ((byte[]) obj)) {
				appendByte(buffer, val);
			}
		} else if (obj instanceof BinaryContent) {
			try {
				InputStream in = ((BinaryContent) obj).getStream();
				appendStream(buffer, in);
			} catch (IOException ex) {
				throw new IOError(ex);
			}
		} else if (obj instanceof Blob) {
			try {
				InputStream in = ((Blob) obj).getBinaryStream();
				appendStream(buffer, in);
			} catch (IOException ex) {
				throw new IOError(ex);
			} catch (SQLException ex) {
				throw new IOError(ex);
			}
		} else {
			throw new IllegalArgumentException("Not a blob literal: " + obj);
		}
	}

	private void appendStream(StringBuffer buffer, InputStream in) throws IOException {
		try {
			int value;
			while ((value = in.read()) >= 0) {
				appendByte(buffer, value);
			}
		} finally {
			in.close();
		}
	}

	private void appendByte(StringBuffer buffer, int val) {
		buffer.append(octet((val >>> 4) & 0x0F));
		buffer.append(octet(val & 0x0F));
	}

	private char octet(int val) {
		return OCTETS[val];
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		throw new UnsupportedOperationException();
	}

}
