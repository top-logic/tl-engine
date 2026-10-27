/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.ci;

import com.top_logic.basic.util.ResKey;

/**
 * The state of a {@link CIBuild build} of a CI job.
 */
public enum CIBuildState {

	/**
	 * The build runs.
	 */
	RUNNING(I18NConstants.BUILD_RUNNING),

	/**
	 * The build has ended successfully.
	 */
	SUCCESS(I18NConstants.BUILD_SUCCESS),

	/**
	 * The build has failed.
	 */
	FAILURE(I18NConstants.BUILD_FAILURE),

	/**
	 * The build has ended, but with problems that do not fail it, e.g. failed tests.
	 */
	UNSTABLE(I18NConstants.BUILD_UNSTABLE),

	/**
	 * The build was stopped before it ended.
	 */
	ABORTED(I18NConstants.BUILD_ABORTED),

	/**
	 * The build has ended without building, e.g. because an earlier stage stopped it.
	 */
	NOT_BUILT(I18NConstants.BUILD_NOT_BUILT),

	;

	private final ResKey _label;

	CIBuildState(ResKey label) {
		_label = label;
	}

	/**
	 * The name of the state for a user.
	 */
	public ResKey label() {
		return _label;
	}

	/**
	 * Whether the build has ended.
	 */
	public boolean isDone() {
		return this != RUNNING;
	}

}
