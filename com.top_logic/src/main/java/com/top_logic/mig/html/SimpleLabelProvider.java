/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import com.top_logic.layout.LabelProvider;

/**
 * {@link LabelProvider} returning the {@link String#valueOf(Object) string representation} of the
 * given object.
 */
public class SimpleLabelProvider implements LabelProvider {

	/**
	 * Singleton {@link SimpleLabelProvider} instance.
	 */
	public static final SimpleLabelProvider INSTANCE = new SimpleLabelProvider();

	private SimpleLabelProvider() {
		// Singleton constructor.
	}

	@Override
	public String getLabel(Object object) {
		return (object == null) ? "" : object.toString();
	}

}
