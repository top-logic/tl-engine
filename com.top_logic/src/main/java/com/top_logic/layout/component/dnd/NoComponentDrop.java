/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.dnd;

import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.dnd.I18NConstants;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ComponentDropTarget} taht prevents drops by default.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NoComponentDrop implements ComponentDropTarget {

	/**
	 * Singleton {@link NoComponentDrop} instance.
	 */
	public static final NoComponentDrop INSTANCE = new NoComponentDrop();

	private NoComponentDrop() {
		// Singleton constructor.
	}

	@Override
	public boolean dropEnabled(LayoutComponent component) {
		return false;
	}

	@Override
	public void handleDrop(ComponentDropEvent event) {
		InfoService.showInfo(I18NConstants.DROP_NOT_POSSIBLE);
	}

}
