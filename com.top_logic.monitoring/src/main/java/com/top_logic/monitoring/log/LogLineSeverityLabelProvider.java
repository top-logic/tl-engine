/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.log;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.label.NullSafeLabelProvider;

/**
 * {@link LabelProvider} for {@link LogLineSeverity}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class LogLineSeverityLabelProvider extends NullSafeLabelProvider {

	/** The {@link LogLineSeverityLabelProvider} instance. */
	public static final LogLineSeverityLabelProvider INSTANCE = new LogLineSeverityLabelProvider();

	@Override
	protected String getLabelNullSafe(Object model) {
		return ((LogLineSeverity) model).getName();
	}

}
