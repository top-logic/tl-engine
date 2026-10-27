/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A request body of the given size, generated as it is read: after its first
 * {@link #HELD_BACK_AFTER} bytes it waits until the upstream has received some, so that a proxy
 * collecting the body before forwarding it fails.
 */
public final class HeldBackBody extends InputStream {

	/**
	 * The bytes the body gives before it waits for the upstream.
	 */
	public static final int HELD_BACK_AFTER = 1024 * 1024;

	private final long _size;

	private final CountDownLatch _upstreamReading;

	private long _read;

	/**
	 * Creates a {@link HeldBackBody}.
	 *
	 * @param size
	 *        The number of bytes of the body.
	 * @param upstreamReading
	 *        Counted down by the upstream when it has received a part of the body.
	 */
	public HeldBackBody(long size, CountDownLatch upstreamReading) {
		_size = size;
		_upstreamReading = upstreamReading;
	}

	@Override
	public int read() throws IOException {
		byte[] one = new byte[1];
		return read(one, 0, 1) < 0 ? -1 : one[0] & 0xFF;
	}

	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		if (_read >= _size) {
			return -1;
		}
		if (_read >= HELD_BACK_AFTER) {
			awaitUpstream();
		}
		long limit = _read < HELD_BACK_AFTER ? HELD_BACK_AFTER : _size;
		int count = (int) Math.min(length, limit - _read);
		Arrays.fill(buffer, offset, offset + count, (byte) 'x');
		_read += count;
		return count;
	}

	private void awaitUpstream() throws IOException {
		try {
			if (!_upstreamReading.await(30, TimeUnit.SECONDS)) {
				throw new IOException("The upstream got nothing of the body before the client sent all of it.");
			}
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IOException(ex);
		}
	}

}
