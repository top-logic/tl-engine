/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.col.Filter;

/**
 * {@link Filter} that accepts {@link LayoutComponent} that are opened as dialog.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ComponentOpenedAsDialog implements Filter<LayoutComponent> {

	/** Singleton {@link ComponentOpenedAsDialog} instance. */
	public static final ComponentOpenedAsDialog INSTANCE = new ComponentOpenedAsDialog();

	private ComponentOpenedAsDialog() {
		// Reduce visibility to ensure its a singleton
	}

	@Override
	public boolean accept(LayoutComponent component) {
		return component.openedAsDialog();
	}

}
