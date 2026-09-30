/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import com.top_logic.layout.component.InAppSelectable.InAppSelectableConfig;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * Special {@link CommandHandler} extension to configure in "in app" templates to execute after
 * selection change.
 * 
 * @see InAppSelectableConfig
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface OnSelectionChangeHandler extends CommandHandler {

	// Marker interface

}
