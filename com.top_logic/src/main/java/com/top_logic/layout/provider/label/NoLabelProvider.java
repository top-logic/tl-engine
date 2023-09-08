/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.label;

import com.top_logic.layout.LabelProvider;

/**
 * {@link LabelProvider} creating no labels.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NoLabelProvider implements LabelProvider {

	/**
	 * Singleton {@link NoLabelProvider} instance.
	 */
	public static final NoLabelProvider INSTANCE = new NoLabelProvider();

	private NoLabelProvider() {
		// Singleton constructor.
	}

	@Override
	public String getLabel(Object object) {
		return null;
	}

}
