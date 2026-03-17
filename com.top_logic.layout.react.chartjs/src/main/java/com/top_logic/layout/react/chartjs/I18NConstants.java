/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.chartjs;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N constants for the Chart.js React integration.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Invalid chart configuration: {0}
	 */
	public static ResKey1 ERROR_INVALID_CHART_CONFIG__DETAIL;

	/**
	 * @en Non-primitive object found at path {0} outside of datasets[].metadata.
	 */
	public static ResKey1 ERROR_OBJECT_OUTSIDE_METADATA__PATH;

	/**
	 * @en Unknown handler reference ''{0}''. Check that a handler with this name is configured.
	 */
	public static ResKey1 ERROR_UNKNOWN_HANDLER__NAME;

	/**
	 * @en Unknown tooltip reference ''{0}''. Check that a tooltip with this name is configured.
	 */
	public static ResKey1 ERROR_UNKNOWN_TOOLTIP__NAME;

	/**
	 * @en No data available.
	 */
	public static ResKey NO_DATA;

	static {
		initConstants(I18NConstants.class);
	}
}
