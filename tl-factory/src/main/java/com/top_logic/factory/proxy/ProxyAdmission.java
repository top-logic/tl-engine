/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.proxy;

import java.time.Duration;
import java.util.Objects;

/**
 * The decision of the {@link WorkspaceProxy} about the token of a request.
 *
 * @param status
 *        Whether the request is passed to its {@link ProxyRoute route}, and why not.
 * @param workspaceId
 *        For {@link Status#ADMITTED}, the workspace the token was issued for; {@code null}
 *        otherwise.
 * @param retryAfter
 *        For {@link Status#RATE_LIMITED}, how long the client has to wait before the next request
 *        is admitted; {@link Duration#ZERO} otherwise.
 */
public record ProxyAdmission(Status status, String workspaceId, Duration retryAfter) {

	/**
	 * Whether a request is passed to its route, and why not.
	 */
	public enum Status {

		/**
		 * The token is valid; the request is passed to its route.
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
	 * Creates a {@link ProxyAdmission}.
	 */
	public ProxyAdmission {
		Objects.requireNonNull(status, "status");
		Objects.requireNonNull(retryAfter, "retryAfter");
	}

	/**
	 * The request is refused for its token.
	 */
	public static final ProxyAdmission INVALID_TOKEN = new ProxyAdmission(Status.INVALID_TOKEN, null, Duration.ZERO);

	/**
	 * The request is refused for the address it comes from.
	 */
	public static final ProxyAdmission FOREIGN_SOURCE = new ProxyAdmission(Status.FOREIGN_SOURCE, null, Duration.ZERO);

	/**
	 * The request is passed to its route on behalf of the given workspace.
	 */
	public static ProxyAdmission admitted(String workspaceId) {
		return new ProxyAdmission(Status.ADMITTED, Objects.requireNonNull(workspaceId, "workspaceId"), Duration.ZERO);
	}

	/**
	 * The request is refused until the given time has passed.
	 */
	public static ProxyAdmission rateLimited(Duration retryAfter) {
		return new ProxyAdmission(Status.RATE_LIMITED, null, retryAfter);
	}

	/**
	 * Whether the request is passed to its route.
	 */
	public boolean isAdmitted() {
		return status == Status.ADMITTED;
	}

}
