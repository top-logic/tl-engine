/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.StringServices;

/**
 * {@link LabelProvider} returning {@link StringServices#EMPTY_STRING} for each object.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class EmptyLabelProvider implements LabelProvider {

	/** Singleton {@link EmptyLabelProvider} instance. */
	public static final EmptyLabelProvider INSTANCE = new EmptyLabelProvider();

	private EmptyLabelProvider() {
		// singleton instance
	}

	@Override
	public String getLabel(Object object) {
		return StringServices.EMPTY_STRING;
	}

}

