/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout.tiles;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.bpe.app.layout.ActiveTasksListModelBuilder;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;
import com.top_logic.mig.html.layout.tiles.component.TilePreview;

/**
 * {@link TilePreview} for the "active tasks" using the {@link Collaboration#getMyTasksTitle()
 * configured title} from the {@link Collaboration}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ActiveTasksTilePreview extends ModelBasedPreview<ModelBasedPreview.Config> {

	/**
	 * Creates a {@link ActiveTasksTilePreview}.
	 */
	public ActiveTasksTilePreview(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected ThemeImage icon(ComponentTile tile) {
		ThemeImage result = collaboration(tile).getMyTasksIcon();
		if (result != null) {
			return result;
		}
		return super.icon(tile);
	}

	@Override
	protected HTMLFragment labelContent(ComponentTile tile) {
		ResKey result = collaboration(tile).getMyTasksTitle();
		if (result != null) {
			return Fragments.message(result);
		}
		return super.labelContent(tile);
	}

	@Override
	protected HTMLFragment descriptionContent(ComponentTile tile) {
		I18NStructuredText result = collaboration(tile).getMyTasksDescription();
		if (result != null) {
			return Fragments.htmlSource(result.localizeSourceCode());
		}
		return super.descriptionContent(tile);
	}

	private Collaboration collaboration(ComponentTile tile) {
		return (Collaboration) getModel(tile);
	}

	@Override
	protected int computeNumber(ComponentTile tile) {
		return ActiveTasksListModelBuilder.getActiveTokensForCurrentUser(collaboration(tile)).size();
	}

}
