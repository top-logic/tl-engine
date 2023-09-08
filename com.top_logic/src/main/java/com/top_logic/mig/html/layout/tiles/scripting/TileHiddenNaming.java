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
import com.top_logic.mig.html.layout.tiles.PersonalizedTile;
import com.top_logic.mig.html.layout.tiles.component.ContainerComponentTile;

/**
 * {@link ObjectAspectName0} to get {@link PersonalizedTile#isHidden()}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TileHiddenNaming extends ObjectAspectName0<Boolean, ContainerComponentTile> {

	/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
	Lookup LOOKUP = MethodHandles.lookup();

	@Override
	default Boolean getAspect(ContainerComponentTile baseObject) {
		return Boolean.valueOf(((PersonalizedTile) baseObject.getBusinessObject()).isHidden());
	}

	/**
	 * Creates a new {@link TileHiddenNaming} for the given {@link ContainerComponentTile}.
	 */
	static TileHiddenNaming createName(ContainerComponentTile componentTile) {
		assert componentTile.getBusinessObject() instanceof PersonalizedTile;
		return ObjectAspectNaming.buildNameFromModelNames(TileHiddenNaming.class, componentTile.getModelName());
	}

}

