/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.boundsec.manager.coverage;

/**
 * Summary of the {@link SecurityCoverageAnalysis} result for a single type.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum CoverageStatus {

	/**
	 * The access definition of the type is complete: its objects have a role source and at least
	 * one role may read them, or the type is excluded from access control.
	 */
	COVERED,

	/**
	 * The access definition of the type raised at least one {@link CoverageFinding}.
	 */
	INCOMPLETE;

}
