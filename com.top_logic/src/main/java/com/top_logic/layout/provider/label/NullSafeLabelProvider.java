/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.label;

import com.top_logic.layout.LabelProvider;

/**
 * A {@link LabelProvider} that returns null for the null value.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class NullSafeLabelProvider implements LabelProvider {

	@Override
	public final String getLabel(Object model) {
		if (model == null) {
			return null;
		}
		return getLabelNullSafe(model);
	}

	/**
	 * Is called by {@link #getLabel(Object)} when the model is not null.
	 * 
	 * @param model
	 *        Never null.
	 */
	protected String getLabelNullSafe(Object model) {
		return null;
	}

}
