/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout.tiles;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.bpe.app.layout.ProcessExecutionListModelBuilder;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;
import com.top_logic.mig.html.layout.tiles.component.TilePreview;

/**
 * {@link TilePreview} for the "own processes" using the .
 * {@link Collaboration#getMyProcessesTitle() configured title} in the {@link Collaboration}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class OwnProcessesTilePreview extends ModelBasedPreview<ModelBasedPreview.Config> {

	/**
	 * Creates a {@link OwnProcessesTilePreview}.
	 */
	public OwnProcessesTilePreview(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected ThemeImage icon(ComponentTile tile) {
		ThemeImage result = collaboration(tile).getMyProcessesIcon();
		if (result != null) {
			return result;
		}
		return super.icon(tile);
	}

	@Override
	protected HTMLFragment labelContent(ComponentTile tile) {
		String result = collaboration(tile).getMyProcessesTitle();
		if (result != null && !result.isEmpty()) {
			return Fragments.text(result);
		}
		return super.labelContent(tile);
	}

	@Override
	protected HTMLFragment descriptionContent(ComponentTile tile) {
		StructuredText result = collaboration(tile).getMyProcessesDescription();
		if (result != null && !result.getSourceCode().isEmpty()) {
			return Fragments.htmlSource(result.getSourceCode());
		}
		return super.descriptionContent(tile);
	}

	private Collaboration collaboration(ComponentTile tile) {
		return (Collaboration) getModel(tile);
	}

	@Override
	protected int computeNumber(ComponentTile tile) {
		return ProcessExecutionListModelBuilder.getAllProcessExecutionsForCurrentUser(collaboration(tile)).size();
	}

}
