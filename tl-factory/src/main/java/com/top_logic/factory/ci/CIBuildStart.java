/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.ci;

import java.util.Objects;

/**
 * A build {@link CIProvider#startBuild(String) started} by the control application.
 *
 * <p>
 * The CI server may keep a build in its queue until an executor is free. Until then, the build has
 * no {@link #number() number}, only its {@link #queueReference() place in the queue}.
 * </p>
 *
 * @param number
 *        The number of the build, {@code null} while it waits in the queue of the CI server.
 * @param queueReference
 *        The reference of the request in the queue of the CI server, e.g. the ID of a Jenkins
 *        queue item; {@code null} if the server has none.
 * @param webUrl
 *        The page of the build in the web interface of the CI server; while it waits in the queue,
 *        the page of its job.
 */
public record CIBuildStart(Integer number, String queueReference, String webUrl) {

	/**
	 * Creates a {@link CIBuildStart}.
	 */
	public CIBuildStart {
		Objects.requireNonNull(webUrl, "webUrl");
	}

	/**
	 * Whether the build waits in the queue of the CI server and has no number yet.
	 */
	public boolean isQueued() {
		return number == null;
	}

}
