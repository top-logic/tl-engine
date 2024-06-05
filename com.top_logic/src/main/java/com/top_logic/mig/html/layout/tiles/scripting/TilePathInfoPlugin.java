/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.scripting;

import java.util.List;

import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.TextInputControlProvider;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.debuginfo.AbstractStaticInfoPlugin;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.component.ContainerComponentTile;
import com.top_logic.util.Resources;

/**
 * {@link AbstractStaticInfoPlugin} creating information about the path of a {@link TileLayout} in
 * the root {@link TileLayout}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TilePathInfoPlugin extends AbstractStaticInfoPlugin<ContainerComponentTile> {

	/**
	 * Creates a new {@link TileAllowedAssertion}.
	 * 
	 * @param model
	 *        The inspected model.
	 */
	public TilePathInfoPlugin(ContainerComponentTile model) {
		super(model, I18NConstants.TILE_PATH_INFO, "tilePath");
	}

	@Override
	protected FormMember createValueField(ContainerComponentTile model, String fieldName) {
		StringField field = FormFactory.newStringField(fieldName, FormFactory.IMMUTABLE);
		TextInputControlProvider cp = new TextInputControlProvider();
		cp.setMultiLine(true);
		field.setControlProvider(cp);
		field.initializeField(getFieldValue(model));
		return field;
	}

	private Object getFieldValue(ContainerComponentTile model) {
		List<String> path = model.getModelName().getTilePath();
		if (path.isEmpty()) {
			return Resources.getInstance().getString(I18NConstants.ROOT_TILE);
		}
		StringBuilder out = new StringBuilder();
		path.forEach(element -> out.append("\n * ").append(element));
		return out.toString();
	}

}
