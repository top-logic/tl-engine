/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.log.entry;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.label.NullSafeLabelProvider;

/**
 * {@link LabelProvider} for {@link LogEntrySeverity}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class LogEntrySeverityLabelProvider extends NullSafeLabelProvider {

	/** The {@link LogEntrySeverityLabelProvider} instance. */
	public static final LogEntrySeverityLabelProvider INSTANCE = new LogEntrySeverityLabelProvider();

	@Override
	protected String getLabelNullSafe(Object model) {
		return ((LogEntrySeverity) model).getName();
	}

}
