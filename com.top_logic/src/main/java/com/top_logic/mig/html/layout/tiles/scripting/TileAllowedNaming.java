/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.scripting;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import com.top_logic.layout.scripting.recorder.ref.ObjectAspectNaming;
import com.top_logic.layout.scripting.recorder.ref.ObjectAspectNaming.ObjectAspectName0;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.component.ContainerComponentTile;
import com.top_logic.mig.html.layout.tiles.component.TileContainerComponent;

/**
 * {@link ObjectAspectName0} to get the {@link TileContainerComponent#isTileAllowed(TileLayout)
 * allow-state} of a {@link TileLayout} in a {@link TileContainerComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TileAllowedNaming extends ObjectAspectName0<Boolean, ContainerComponentTile> {

	/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
	Lookup LOOKUP = MethodHandles.lookup();

	@Override
	default Boolean getAspect(ContainerComponentTile baseObject) {
		return Boolean.valueOf(baseObject.isAllowed());
	}

	/**
	 * Creates a new {@link TileAllowedNaming} for the given {@link ContainerComponentTile}.
	 */
	static TileAllowedNaming createName(ContainerComponentTile componentTile) {
		return ObjectAspectNaming.buildNameFromModelNames(TileAllowedNaming.class, componentTile.getModelName());
	}

}

