/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout.tiles;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.basic.fragments.Tag;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;
import com.top_logic.mig.html.layout.tiles.component.StaticPreview;

/**
 * {@link NumberContentsPreview} displaying the number of elements in the underlaying
 * {@link TableComponent}
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public abstract class NumberContentsPreview<C extends NumberContentsPreview.Config> extends StaticPreview<C> {

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
	public NumberContentsPreview(InstantiationContext context, C config) {
		super(context, config);
		_cssClass = config.getCssClass();
	}

	@Override
	protected HTMLFragment image(ComponentTile componentTile) {
		HTMLFragment res = super.image(componentTile);
		int size = computeNumber(componentTile);
		if (size != 0) {
			Tag number = span(_cssClass, text(String.valueOf(size)));
			res = concat(number, res);
		}
		return res;
	}

	/**
	 * The number to be displayed on the tile icon.
	 */
	protected abstract int computeNumber(ComponentTile componentTile);

}

