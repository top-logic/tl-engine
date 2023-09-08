/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout.tiles;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.bpe.execution.model.impl.TokenImpl;
import com.top_logic.mig.html.layout.tiles.component.TilePreview;

/**
 * {@link TilePreview} for {@link Token} model elements.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TokenTilePreview<C extends BPMLTilePreview.Config<?>> extends BPMLTilePreview<C> {

	/**
	 * Creates a {@link TokenTilePreview} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TokenTilePreview(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected HTMLFragment specificLabelContent(Object model) {
		TokenImpl token = (TokenImpl) model;

		HTMLFragment fragment = token.calculateLabelFragment();
		if (fragment != null) {
			return fragment;
		}
		return super.specificLabelContent(model);
	}

}
