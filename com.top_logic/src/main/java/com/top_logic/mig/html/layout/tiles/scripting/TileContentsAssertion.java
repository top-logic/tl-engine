/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.scripting;

import java.util.List;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.SelectionControlProvider;
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
public class TileContentsAssertion extends SingleValueAssertionPlugin<ContainerComponentTile, SelectField> {

	/**
	 * Creates a new {@link TileContentsAssertion}.
	 * 
	 * @param model
	 *        The currently inspected {@link ComponentTile}.
	 */
	public TileContentsAssertion(ContainerComponentTile model) {
		super(model, false, "tileContents");
	}

	@Override
	protected SelectField createValueField(String name) {
		SelectField selectField =
			FormFactory.newSelectField(name, getContents(), FormFactory.MULTIPLE, !FormFactory.IMMUTABLE);
		selectField.setCustomOrder(true);
		selectField.setControlProvider(SelectionControlProvider.SELECTION_INSTANCE);
		return selectField;
	}

	@Override
	protected Object getInitialValue() {
		return getContents();
	}

	private List<ComponentTile> getContents() {
		return getModel().get();
	}

	@Override
	protected GuiAssertion buildAssertion() {
		ModelName expected = ModelResolver.buildModelName(getExpectedValueField().getSelection());
		ModelName actualValue = TileContentsNaming.createName(getModel());
		return ActionFactory.valueAssertion(expected, actualValue, getComment());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.TILE_CONTENTS_ASSERTION;
	}

}
