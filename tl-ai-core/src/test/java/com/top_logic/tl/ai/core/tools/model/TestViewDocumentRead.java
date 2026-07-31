/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import java.io.IOException;
import java.io.OutputStream;

import junit.framework.TestCase;

import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.tl.ai.core.tools.perception.BinaryPerception;

/**
 * Tests that {@link ViewDocumentTool} bounds what it reads into memory.
 *
 * <p>
 * The object to read is named by a model-supplied TID, and the read check that should govern which
 * objects those may be is still a framework-wide placeholder returning {@code true}. So the size of
 * the payload is an attacker-influenced input: without a bound, one tool call pointed at a large
 * stored binary is an {@link OutOfMemoryError} - and the cap that existed was applied only
 * <em>after</em> the whole thing had been materialised.
 * </p>
 *
 * <p>
 * Needs no knowledge base: the bound sits between the tool and {@link BinaryDataSource}, which is
 * what the fixture below plays.
 * </p>
 */
public class TestViewDocumentRead extends TestCase {

	/** Content within the bound is read exactly as before. */
	public void testContentWithinTheBoundIsRead() throws IOException {
		byte[] data = readOf(1024, 4096);

		assertEquals(1024, data.length);
	}

	/** Everything up to the bound is still read; the bound itself is not off by one. */
	public void testContentExactlyAtTheBoundIsRead() throws IOException {
		assertEquals(4096, readOf(4096, 4096).length);
	}

	/**
	 * <b>The bound holds while reading</b>, not from the declared size: {@link BinaryDataSource#getSize()}
	 * is documented as {@code -1} when unknown, so a size-less binary of any length would otherwise be
	 * buffered whole before anything looked at it.
	 */
	public void testOversizedContentIsRefusedWhileReading() {
		try {
			readOf(4097, 4096);
			fail("expected the read to stop at the bound");
		} catch (IOException expected) {
			assertTrue("names the limit: " + expected.getMessage(), expected.getMessage().contains("4096"));
		}
	}

	/**
	 * The bound is enforced against what is <em>delivered</em>, whatever the source claims about itself:
	 * a source reporting an unknown size (or lying about it) is still stopped.
	 */
	public void testUnknownSizeDoesNotDefeatTheBound() {
		try {
			readOf(new SizeLyingSource(1_000_000, -1), 4096);
			fail("expected the read to stop at the bound");
		} catch (IOException expected) {
			// Refused, which is the point.
		}
	}

	/** The production bound is the largest payload any perception path could deliver. */
	public void testTheBoundMatchesWhatCanBeDelivered() {
		assertEquals(BinaryPerception.MAX_PDF_BYTES, BinaryPerception.MAX_SOURCE_BYTES);
	}

	private static byte[] readOf(int sourceBytes, int maxBytes) throws IOException {
		return readOf(new SizeLyingSource(sourceBytes, sourceBytes), maxBytes);
	}

	private static byte[] readOf(BinaryDataSource source, int maxBytes) throws IOException {
		return ViewDocumentTool.readBytes(source, maxBytes);
	}

	/**
	 * A {@link BinaryDataSource} delivering {@code deliveredBytes} bytes while reporting
	 * {@code declaredSize} as its size - the two are separate on purpose, so a source that does not
	 * know (or does not tell) its size can be exercised.
	 */
	private static final class SizeLyingSource implements BinaryDataSource {

		private final int _deliveredBytes;

		private final long _declaredSize;

		SizeLyingSource(int deliveredBytes, long declaredSize) {
			_deliveredBytes = deliveredBytes;
			_declaredSize = declaredSize;
		}

		@Override
		public String getName() {
			return "fixture.bin";
		}

		@Override
		public long getSize() {
			return _declaredSize;
		}

		@Override
		public String getContentType() {
			return CONTENT_TYPE_OCTET_STREAM;
		}

		@Override
		public void deliverTo(OutputStream out) throws IOException {
			byte[] chunk = new byte[512];
			int remaining = _deliveredBytes;
			while (remaining > 0) {
				int length = Math.min(chunk.length, remaining);
				out.write(chunk, 0, length);
				remaining -= length;
			}
		}
	}
}
