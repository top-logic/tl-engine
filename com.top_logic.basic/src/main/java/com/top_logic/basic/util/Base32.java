/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import java.util.Arrays;

/**
 * Scheme for serializing bit streams to strings that can be typed in manually e.g. for an
 * application license key.
 * 
 * <p>
 * The generated serialization is case insensitive and does not use letters with ambiguous images
 * (like 'O' and '0', or 'I' and '1').
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Base32 {

	private static final int BITS_PER_BYTE = 8;

	private static final int BITS_PER_CHAR = 5;

	private static char[] ALPHABET = {
		'A', 'B', 'C', 'D', 'E',
		'F', 'G', 'H', 'J', 'K',
		'L', 'M', 'N', 'P', 'Q',
		'R', 'S', 'T', 'U', 'V',
		'W', 'X', 'Y', 'Z', '2',
		'3', '4', '5', '6', '7',
		'8', '9'
	};
	
	private static final int[] BITS_BY_CHAR;

	static {
		BITS_BY_CHAR = new int[255];
		Arrays.fill(BITS_BY_CHAR, -1);
		for (int n = 0, cnt = ALPHABET.length; n < cnt; n++) {
			char ch = ALPHABET[n];
			BITS_BY_CHAR[ch] = n;
			BITS_BY_CHAR[Character.toLowerCase(ch)] = n;
		}
	}
	
	/**
	 * Encodes the given plain bytes into a string of characters.
	 */
	public static char[] encodeBase32(byte[] plain) {
		int plainLength = plain.length;
		int bits = plainLength * BITS_PER_BYTE;
		int chars = (bits + BITS_PER_CHAR - 1) / BITS_PER_CHAR;
		
		char[] result = new char[chars];
		
		int resultPos = 0;
		int inputPos = 0;
		int buffer = 0;
		int bufferBits = 0;
		while (true) {
			if (bufferBits < BITS_PER_CHAR) {
				if (inputPos >= plainLength) {
					if (bufferBits == 0) {
						break;
					} else {
						int paddingBits = BITS_PER_CHAR - bufferBits;
						buffer = buffer << paddingBits;
						bufferBits += paddingBits;
					}
				} else {
					buffer = (buffer << BITS_PER_BYTE) | (0xFF & plain[inputPos++]);
					bufferBits += BITS_PER_BYTE;
				}
			}
			
			int index = 0x1F & buffer >>> (bufferBits - BITS_PER_CHAR);
			bufferBits -= BITS_PER_CHAR;
			result[resultPos++] = ALPHABET[index];
		}
		assert resultPos == result.length;
		
		return result;
	}

	/**
	 * Decodes the given characters to a plain byte array.
	 * 
	 * @throws IllegalArgumentException
	 *         If the input contains invalid characters or is otherwise not well-formed according to
	 *         the {@link Base32} format.
	 */
	public static byte[] decodeBase32(char[] encoded) throws IllegalArgumentException {
		int encodedLength = encoded.length;
		int size = (encodedLength * BITS_PER_CHAR) / BITS_PER_BYTE;
		byte[] result = new byte[size];
		
		int resultPos = 0;
		int inputPos = 0;
		int buffer = 0;
		int bufferBits = 0;
		decode:
		while (true) {
			while (bufferBits < BITS_PER_BYTE) {
				if (inputPos >= encodedLength) {
					if (bufferBits > 0) {
						// Check padding.
						int paddingMask = ~((~0) << bufferBits);
						int paddingValue = buffer & paddingMask;
						if (paddingValue != 0) {
							throw new IllegalArgumentException("Invalid input padding.");
						}
					}
					break decode;
				}
				
				char ch = encoded[inputPos++];
				int inputBits = BITS_BY_CHAR[ch];
				if (inputBits < 0) {
					throw new IllegalArgumentException("Invalid character: '" + ch + "'.");
				}
				
				buffer = (buffer << BITS_PER_CHAR) | inputBits;
				bufferBits += BITS_PER_CHAR;
			}
			
			byte value = (byte) (buffer >>> (bufferBits - BITS_PER_BYTE));
			bufferBits -= BITS_PER_BYTE;
			result[resultPos++] = value;
		}
		assert resultPos == result.length;
		
		return result;
	}

}
