/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.resource;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/**
 * {@link CharsetEncoder} that replaces characters that are not supported by the
 * target {@link Charset} with Java Unicode escape sequences.
 * 
 * <p>
 * The backslash character in the input is always escaped with another backslash
 * character.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UnicodeEscapeCharsetEncoder extends CharsetEncoder {
	
	private static final byte[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
	
	private final CharsetEncoder impl;

	private final boolean quoteBackslash;

	/**
	 * Creates a {@link UnicodeEscapeCharsetEncoder}.
	 * 
	 * @param cs
	 *        The base {@link Charset} to use. Only characters not supported
	 *        by the base {@link Charset} are excaped using Java Unicode
	 *        escape sequences.
	 * @param quoteBackslash
	 *        Whether backslash characters in the input should be quoted. If 
	 *        flag is set to <code>false</code>, backslash characters are 
	 *        directly passed to the output allowing applications to also 
	 *        explicitly generate escape sequences. 
	 */
	public UnicodeEscapeCharsetEncoder(Charset cs, boolean quoteBackslash) {
		super(cs, 1.5f, 6.0f /* \u1234 */);
		
		this.impl = cs.newEncoder();
		this.impl.onUnmappableCharacter(CodingErrorAction.REPORT);
		
		this.quoteBackslash = quoteBackslash;
	}

	@Override
	protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
		impl.onMalformedInput(malformedInputAction());

		int limit = in.limit();

		if (quoteBackslash) {
			// Find backslash characters and quote them before passing the input the
			// the underlying encoder.
			for (int n = 0; n < limit; n++) {
				if (in.get(n) == '\\') {
					if (n > 0) {
						in.limit(n);
						CoderResult result = encodeBaseCharset(in, out);
						in.limit(limit);
						if (result.isError()) {
							return result;
						}
					}
					in.get();
					out.put((byte) '\\');
					out.put((byte) '\\');
				}
			}
		}

		return encodeBaseCharset(in, out);
	}

	private CoderResult encodeBaseCharset(CharBuffer in, ByteBuffer out) {
		while (true) {
			CoderResult result = impl.encode(in, out, true);
			
			if (result.isUnmappable()) {
				for (int n = 0, cnt = result.length(); n < cnt; n++) {
					char ch = in.get();
					out.put((byte) '\\');
					out.put((byte) 'u');
					out.put(HEX[0x0F & (ch >>> 12)]);
					out.put(HEX[0x0F & (ch >>> 8)]);
					out.put(HEX[0x0F & (ch >>> 4)]);
					out.put(HEX[0x0F & ch]);
				}
			} else {
				return result;
			}
		}
	}
}