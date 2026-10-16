/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.revision;

import com.top_logic.basic.config.annotation.Label;

/**
 * The kind of range in which the transaction monitor displays the revisions.
 * 
 * @see ChangeSetSelector#RANGE_MODE_FIELD
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public enum RangeMode {

	/**
	 * The revisions created within a time range are displayed.
	 */
	@Label("Date")
	TIME_RANGE,

	/**
	 * The revisions within a range of commit numbers are displayed.
	 */
	@Label("Revision")
	REVISION_RANGE;

}
