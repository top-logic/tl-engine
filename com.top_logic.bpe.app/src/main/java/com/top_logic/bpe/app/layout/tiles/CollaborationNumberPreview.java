/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout.tiles;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.bpe.app.layout.ActiveTaskComponent.ActiveTasksListModelBuilder;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.layout.basic.fragments.Tag;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;
import com.top_logic.mig.html.layout.tiles.component.StaticPreview;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class CollaborationNumberPreview extends BPMLTilePreview<CollaborationNumberPreview.Config> {

	private String _cssClass;

	/**
	 * Configuration of a {@link NumberContentsPreview}.
	 */
	public interface Config extends StaticPreview.Config {

		/**
		 * the css-class to apply to the span with the number of elements.
		 */
		@StringDefault("circle-red")
		String getCssClass();
	}

	/**
	 * Creates a new {@link NumberContentsPreview}.
	 */
	public CollaborationNumberPreview(InstantiationContext context, Config config) {
		super(context, config);
		_cssClass = config.getCssClass();
	}

	@Override
	protected HTMLFragment image(ComponentTile componentTile) {
		HTMLFragment res = super.image(componentTile);
		Object bo = componentTile.getBusinessObject();
		if (bo instanceof Collaboration) {
			Collaboration collaboration = (Collaboration) bo;
			List<Token> activeTokensForCurrentUser =
				ActiveTasksListModelBuilder.getActiveTokensForCurrentUser(collaboration);
			int size = activeTokensForCurrentUser.size();
			if (size > 0) {
				Tag number = span(_cssClass, text(String.valueOf(size)));
				res = concat(number, res);
			}
		}

		return res;
	}

}

