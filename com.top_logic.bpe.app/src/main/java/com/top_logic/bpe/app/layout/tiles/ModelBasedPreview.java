/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout.tiles;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;
import com.top_logic.mig.html.layout.tiles.component.TilePreview;

/**
 * {@link TilePreview} takes information from some model in the component tree.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ModelBasedPreview<C extends ModelBasedPreview.Config> extends NumberContentsPreview<C> {

	/**
	 * Configuration options for {@link ModelBasedPreview}.
	 */
	public interface Config extends NumberContentsPreview.Config {

		/**
		 * Reference to some model in the component tree.
		 */
		ModelSpec getModel();

	}

	private ChannelLinking _model;

	/**
	 * Creates a {@link ModelBasedPreview}.
	 */
	public ModelBasedPreview(InstantiationContext context, C config) {
		super(context, config);
		_model = context.getInstance(config.getModel());
	}

	/**
	 * The model to retrieve information from.
	 */
	protected Object getModel(ComponentTile tile) {
		return ChannelLinking.eval(tile.getTileComponent(), _model);
	}

}
