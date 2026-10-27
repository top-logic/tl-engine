/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.ci;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * A build of a {@link CIJob}, as reported by its {@link CIProvider}.
 *
 * @param number
 *        The number of the build within its job, counted from {@code 1}.
 * @param state
 *        Whether the build runs, and how it has ended.
 * @param webUrl
 *        The page of the build in the web interface of the CI server.
 * @param startTime
 *        When the build started.
 * @param duration
 *        How long the build took; for a {@link CIBuildState#RUNNING running} build, how long it
 *        runs so far.
 */
public record CIBuild(int number, CIBuildState state, String webUrl, Instant startTime, Duration duration) {

	/**
	 * Creates a {@link CIBuild}.
	 */
	public CIBuild {
		Objects.requireNonNull(state, "state");
		Objects.requireNonNull(webUrl, "webUrl");
		Objects.requireNonNull(startTime, "startTime");
		Objects.requireNonNull(duration, "duration");
	}

}
