/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.react.control.table.ReactResourceCellControl;

/**
 * Default {@link ReactControlProvider} that renders any object using its label and icon.
 *
 * <p>
 * Uses {@link MetaResourceProvider} to resolve label, icon, tooltip, and CSS class, and renders
 * them via {@link ReactResourceCellControl}.
 * </p>
 */
public class MetaResourceControlProvider implements ReactControlProvider {

	/** Singleton instance. */
	public static final MetaResourceControlProvider INSTANCE = new MetaResourceControlProvider();

	@Override
	public ReactControl createControl(Object model) {
		return new ReactResourceCellControl(model, MetaResourceProvider.INSTANCE, true, true, false);
	}
}
