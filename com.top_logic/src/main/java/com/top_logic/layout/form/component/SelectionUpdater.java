/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import com.top_logic.layout.component.Selectable;

/**
 * Plugin to update the selection of a {@link Selectable}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface SelectionUpdater {
	
	/**
	 * Updates the selection in the given {@link Selectable}.
	 */
	void updateSelection(Selectable selectable);

}
