/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.proxy;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Flow;

/**
 * The answer of a {@link ProxyExchange} that is continued after the thread handling the request
 * has returned, see {@link ProxyExchange#startAsync()}.
 *
 * <p>
 * The exchange stays open until the answer is {@link #complete() completed} or its
 * {@link #stream(Flow.Publisher) streamed body} ends. No thread waits for the upstream in between.
 * </p>
 */
public interface AsyncAnswer {

	/**
	 * Passes the given body to the client as it arrives, after the status and headers were sent
	 * with {@link ProxyExchange#sendHeaders(int, long)}, and completes the exchange at its end.
	 *
	 * <p>
	 * The body is requested piece by piece, the next piece only when the client took the one
	 * before, so a slow client slows the upstream down instead of filling the memory. If the client
	 * goes away, the subscription is cancelled, which closes the connection to the upstream.
	 * </p>
	 */
	void stream(Flow.Publisher<List<ByteBuffer>> body);

	/**
	 * Completes the exchange with what was written to it, e.g. an error answer sent with
	 * {@link ProxyForwarding#sendText(ProxyExchange, int, String)}.
	 */
	void complete();

}
