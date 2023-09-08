/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.scripting;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.SingleValueAssertionPlugin;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.ui.BreadcrumbStrings;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;
import com.top_logic.mig.html.layout.tiles.component.TileContainerComponent;

/**
 * {@link SingleValueAssertionPlugin} to check the currently
 * {@link TileContainerComponent#displayedLayout() displayed layout}.
 * 
 * @see TileContainerComponent#isTileAllowed(TileLayout)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TilePathAssertion extends SingleValueAssertionPlugin<TileContainerComponent, StringField> {

	/**
	 * Creates a new {@link TilePathAssertion}.
	 * 
	 * @param model
	 *        The currently inspected {@link ComponentTile}.
	 */
	public TilePathAssertion(TileContainerComponent model) {
		super(model, false, "tilePathAssertion");
	}

	@Override
	protected StringField createValueField(String name) {
		return FormFactory.newStringField(name);
	}

	@Override
	protected Object getInitialValue() {
		List<String> path = getModel().getTilePath(getModel().displayedLayout());
		return BreadcrumbStrings.INSTANCE.getSpecification(path);
	}

	@Override
	protected GuiAssertion buildAssertion() {
		List<String> value;
		try {
			value = BreadcrumbStrings.INSTANCE.getValue("value", getExpectedValueField().getAsString());
		} catch (ConfigurationException ex) {
			throw new RuntimeException(ex);
		}
		ModelName expected = ModelResolver.buildModelName(value);
		ModelName actualValue = TilePathNaming.createName(getModel());
		return ActionFactory.valueAssertion(expected, actualValue, getComment());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.TILE_PATH_ASSERTION;
	}

}
