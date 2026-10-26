/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.llm;

import java.time.Duration;

/**
 * The decision of the {@link LLMProxy} about the token of a request.
 *
 * @param status
 *        Whether the request is forwarded, and why not.
 * @param retryAfter
 *        For {@link Status#RATE_LIMITED}, how long the client has to wait before the next request
 *        is admitted; {@link Duration#ZERO} otherwise.
 */
public record LLMAdmission(Status status, Duration retryAfter) {

	/**
	 * Whether a request is forwarded, and why not.
	 */
	public enum Status {

		/**
		 * The token is valid; the request is forwarded.
		 */
		ADMITTED,

		/**
		 * The token is missing, unknown, revoked or expired, or its workspace no longer exists.
		 */
		INVALID_TOKEN,

		/**
		 * The token is valid but was first used from another address.
		 */
		FOREIGN_SOURCE,

		/**
		 * The token is valid but has made too many requests in the last minute.
		 */
		RATE_LIMITED,

	}

	/**
	 * The request is forwarded.
	 */
	public static final LLMAdmission ADMITTED = new LLMAdmission(Status.ADMITTED, Duration.ZERO);

	/**
	 * The request is refused for its token.
	 */
	public static final LLMAdmission INVALID_TOKEN = new LLMAdmission(Status.INVALID_TOKEN, Duration.ZERO);

	/**
	 * The request is refused for the address it comes from.
	 */
	public static final LLMAdmission FOREIGN_SOURCE = new LLMAdmission(Status.FOREIGN_SOURCE, Duration.ZERO);

	/**
	 * The request is refused until the given time has passed.
	 */
	public static LLMAdmission rateLimited(Duration retryAfter) {
		return new LLMAdmission(Status.RATE_LIMITED, retryAfter);
	}

}
