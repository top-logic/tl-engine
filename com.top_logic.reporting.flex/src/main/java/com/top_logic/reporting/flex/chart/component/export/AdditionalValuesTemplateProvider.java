/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component.export;

import java.util.List;

/**
 * Provider of an additional values template, depending on the export columns.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface AdditionalValuesTemplateProvider {

	/**
	 * Returns the template for the given list of columns.
	 */
	String getTemplate(List<String> columns);

}
