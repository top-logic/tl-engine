/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component.export;

import java.util.List;

/**
 * Factory class to create {@link SlideReplacerBuilder.TextCutter}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TextCutterFactory {

	/**
	 * Creates a {@link SlideReplacerBuilder.TextCutter} for the given list of columns.
	 * 
	 * @param columns
	 *        The export columns.
	 */
	SlideReplacerBuilder.TextCutter createTextCutter(List<String> columns);
}
