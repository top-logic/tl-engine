/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.scripting;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;

import com.top_logic.layout.scripting.recorder.ref.ObjectAspectNaming;
import com.top_logic.layout.scripting.recorder.ref.ObjectAspectNaming.ObjectAspectName0;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;
import com.top_logic.mig.html.layout.tiles.component.ContainerComponentTile;

/**
 * {@link ObjectAspectName0} to get {@link ContainerComponentTile#get() all tiles} of a
 * {@link ContainerComponentTile}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TileContentsNaming extends ObjectAspectName0<List<ComponentTile>, ContainerComponentTile> {

	/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
	Lookup LOOKUP = MethodHandles.lookup();

	@Override
	default List<ComponentTile> getAspect(ContainerComponentTile baseObject) {
		return baseObject.get();
	}

	/**
	 * Creates a new {@link TileContentsNaming} for the given {@link ContainerComponentTile}.
	 */
	static TileContentsNaming createName(ContainerComponentTile componentTile) {
		return ObjectAspectNaming.buildNameFromModelNames(TileContentsNaming.class, componentTile.getModelName());
	}

}

