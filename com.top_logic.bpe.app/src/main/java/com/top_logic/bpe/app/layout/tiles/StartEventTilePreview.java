/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout.tiles;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.StartEvent;
import com.top_logic.bpe.execution.engine.GuiEngine;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.layout.tiles.component.TilePreview;

/**
 * {@link TilePreview} for {@link StartEvent}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StartEventTilePreview<C extends BPMLTilePreview.Config<?>> extends BPMLTilePreview<C> {

	/**
	 * Creates a {@link StartEventTilePreview} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public StartEventTilePreview(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected HTMLFragment specificLabelContent(Object model) {
		HTMLFragment result = super.specificLabelContent(model);
		if (result != null) {
			return result;
		}

		return super.specificLabelContent(startNode(model));
	}

	@Override
	protected HTMLFragment specificDescriptionContent(Object model) {
		HTMLFragment result = super.specificDescriptionContent(model);
		if (result != null) {
			return result;
		}

		return super.specificDescriptionContent(startNode(model));
	}

	@Override
	protected ThemeImage specificIcon(Object model) {
		ThemeImage result = super.specificIcon(model);
		if (result != null) {
			return result;
		}
		return super.specificIcon(startNode(model));
	}

	private Node startNode(Object model) {
		return GuiEngine.getInstance().getNextNode(((StartEvent) model));
	}

}
