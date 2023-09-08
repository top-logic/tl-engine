/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.scripting;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;

import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.ObjectAspectNaming;
import com.top_logic.layout.scripting.recorder.ref.ObjectAspectNaming.ObjectAspectName0;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.component.TileContainerComponent;

/**
 * {@link ObjectAspectName0} to get path to currently displayed {@link TileLayout}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TilePathNaming extends ObjectAspectName0<List<String>, TileContainerComponent> {

	/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
	Lookup LOOKUP = MethodHandles.lookup();

	@Override
	default List<String> getAspect(TileContainerComponent baseObject) {
		return baseObject.getTilePath(baseObject.displayedLayout());
	}

	/**
	 * Creates a new {@link TilePathNaming} for the given {@link TileContainerComponent}.
	 */
	static TilePathNaming createName(TileContainerComponent tileComponent) {
		return ObjectAspectNaming.buildNameFromModelNames(TilePathNaming.class,
			ModelResolver.buildModelName(tileComponent));
	}

}

