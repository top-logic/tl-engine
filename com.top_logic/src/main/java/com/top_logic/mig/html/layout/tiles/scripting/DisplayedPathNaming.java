/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.scripting;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.ObjectAspectNaming;
import com.top_logic.layout.scripting.recorder.ref.ObjectAspectNaming.ObjectAspectName0;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.RootTileComponent;
import com.top_logic.mig.html.layout.tiles.component.TileContainerComponent;

/**
 * {@link ObjectAspectName0} to get the {@link RootTileComponent#displayedPath() displayed path} of
 * given {@link RootTileComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface DisplayedPathNaming extends ObjectAspectName0<List<ComponentName>, RootTileComponent> {

	/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
	Lookup LOOKUP = MethodHandles.lookup();

	@Override
	default List<ComponentName> getAspect(RootTileComponent baseObject) {
		return baseObject.displayedPath().stream()
			.map(LayoutComponent::getName)
			.collect(Collectors.toList());
	}

	/**
	 * Creates a new {@link DisplayedPathNaming} for the given {@link TileContainerComponent}.
	 */
	static DisplayedPathNaming createName(RootTileComponent rootTile) {
		return ObjectAspectNaming.buildNameFromModelNames(DisplayedPathNaming.class,
			ModelResolver.buildModelName(rootTile));
	}

}

