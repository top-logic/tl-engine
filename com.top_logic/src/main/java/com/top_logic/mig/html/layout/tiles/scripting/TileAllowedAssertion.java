/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.scripting;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.SingleValueAssertionPlugin;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;
import com.top_logic.mig.html.layout.tiles.component.ContainerComponentTile;
import com.top_logic.mig.html.layout.tiles.component.TileContainerComponent;

/**
 * {@link SingleValueAssertionPlugin} to check whether a {@link TileLayout} is allowed.
 * 
 * @see TileContainerComponent#isTileAllowed(TileLayout)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TileAllowedAssertion extends SingleValueAssertionPlugin<ContainerComponentTile, BooleanField> {

	/**
	 * Creates a new {@link TileAllowedAssertion}.
	 * 
	 * @param model
	 *        The currently inspected {@link ComponentTile}.
	 */
	public TileAllowedAssertion(ContainerComponentTile model) {
		super(model, false, "tileAllowed");
	}

	@Override
	protected BooleanField createValueField(String name) {
		return FormFactory.newBooleanField(name);
	}

	@Override
	protected Object getInitialValue() {
		return getModel().isAllowed();
	}

	@Override
	protected GuiAssertion buildAssertion() {
		ModelName expected = ModelResolver.buildModelName(getExpectedValueField().getAsBoolean());
		ModelName actualValue = TileAllowedNaming.createName(getModel());
		return ActionFactory.valueAssertion(expected, actualValue, getComment());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.TILE_ALLOWED_ASSERTION;
	}

}
