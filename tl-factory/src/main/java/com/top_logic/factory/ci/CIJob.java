/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.ci;

import java.util.Objects;

/**
 * The CI job of a project, as {@link CIProvider#ensureJob(com.top_logic.factory.git.GitRepository,
 * String) ensured} by its {@link CIProvider}.
 *
 * @param name
 *        The name the provider identifies the job by in its other operations, e.g. the full name of
 *        a Jenkins job with its folders, {@code tl-factory/my-org/my-app}.
 * @param webUrl
 *        The page of the job in the web interface of the CI server.
 */
public record CIJob(String name, String webUrl) {

	/**
	 * Creates a {@link CIJob}.
	 */
	public CIJob {
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(webUrl, "webUrl");
	}

}
