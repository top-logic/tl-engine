/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} which hides all commands, except when the
 * {@link TileContainerComponent} currently displays a {@link TileLayout} with a specific
 * {@link TileLayout#getClass()}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
abstract class HasSpecificTileExecutability implements ExecutabilityRule {
	
	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (aComponent instanceof TileContainerComponent) {
			TileContainerComponent containerComponent = (TileContainerComponent) aComponent;
			if (tileLayoutClass().isInstance(containerComponent.displayedLayout())) {
				return ExecutableState.EXECUTABLE;
			}
		}
		return ExecutableState.NOT_EXEC_HIDDEN;
	}

	protected abstract Class<? extends TileLayout> tileLayoutClass();

}

