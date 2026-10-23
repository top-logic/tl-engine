/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.workspace.docker;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/**
 * Decodes a byte stream that arrives in chunks into text chunks, keeping a character that is split
 * between two byte chunks for the next one.
 */
final class TextChunkDecoder {

	private final CharsetDecoder _decoder = StandardCharsets.UTF_8.newDecoder()
		.onMalformedInput(CodingErrorAction.REPLACE)
		.onUnmappableCharacter(CodingErrorAction.REPLACE);

	private ByteBuffer _pending = ByteBuffer.allocate(0);

	/**
	 * Decodes the next chunk of bytes.
	 *
	 * @return The text of all characters completed by this chunk, possibly empty.
	 */
	String decode(byte[] chunk) {
		ByteBuffer input = ByteBuffer.allocate(_pending.remaining() + chunk.length);
		input.put(_pending).put(chunk).flip();
		CharBuffer output = CharBuffer.allocate(input.remaining() + 1);
		_decoder.decode(input, output, false);
		_pending = input.slice();
		return output.flip().toString();
	}

	/**
	 * Decodes the bytes left at the end of the stream.
	 *
	 * @return The text of the bytes left, a replacement character for an incomplete character.
	 */
	String finish() {
		CharBuffer output = CharBuffer.allocate(_pending.remaining() + 1);
		_decoder.decode(_pending, output, true);
		_decoder.flush(output);
		_pending = ByteBuffer.allocate(0);
		_decoder.reset();
		return output.flip().toString();
	}

}
