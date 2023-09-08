/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery.scripting;

import java.util.Map;

import com.top_logic.layout.form.model.GalleryField;
import com.top_logic.layout.image.gallery.GalleryControl;
import com.top_logic.layout.image.gallery.GalleryModel;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorCommand;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorPluginFactory;
import com.top_logic.layout.scripting.recorder.gui.inspector.InspectorModel;

/**
 * {@link GuiInspectorCommand}, that opens an inspector GUI for image galleries.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class GalleryInspectorCommand extends GuiInspectorCommand<GalleryControl, GalleryModel> {

	/** Static INSTANCE of {@link GalleryInspectorCommand} */
	public static final GalleryInspectorCommand INSTANCE = new GalleryInspectorCommand();

	private GalleryInspectorCommand() {
		// Singleton
	}

	@Override
	protected GalleryModel findInspectedGuiElement(GalleryControl control, Map<String, Object> arguments)
			throws AssertionError {
		return control.getGalleryModel();
	}

	@Override
	protected void buildInspector(InspectorModel inspector, GalleryModel model) {
		if (model instanceof GalleryField) {
			GuiInspectorPluginFactory.createFieldAssertions(inspector, (GalleryField) model);
		}
	}

}
