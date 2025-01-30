/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.layout.component.Selectable;

/**
 * Resets the selection to the default selection, if one exists.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
public class ResetSelection implements SelectionUpdater {

	@Override
	public void updateSelection(Selectable selectable) {
		selectable.clearSelection();
	}

}
