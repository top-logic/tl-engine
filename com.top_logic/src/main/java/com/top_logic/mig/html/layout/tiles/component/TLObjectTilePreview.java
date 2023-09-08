/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.provider.MetaResourceProvider;

/**
 * {@link LabelBasedPreview} for an {@link Object} Based on the icon and label of the
 * {@link MetaResourceProvider}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
public class TLObjectTilePreview extends LabelBasedPreview<TLObjectTilePreview.Config> {

	/**
	 * Configuration of a {@link TLObjectTilePreview}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends LabelBasedPreview.Config<TLObjectTilePreview> {

		@Override
		@Hidden
		ThemeImage getIcon();

	}

	/**
	 * Creates a new {@link TLObjectTilePreview}.
	 */
	public TLObjectTilePreview(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected ThemeImage icon(ComponentTile tile) {
		return MetaResourceProvider.INSTANCE.getImage(tile.getBusinessObject(), Flavor.ENLARGED);
	}

	@Override
	protected HTMLFragment labelContent(ComponentTile tile) {
		return nonNull(MetaResourceProvider.INSTANCE.getLabel(tile.getBusinessObject()));
	}

	private HTMLFragment nonNull(String content) {
		if (StringServices.isEmpty(content)) {
			return empty();
		} else {
			return text(content);
		}
	}

	@Override
	protected HTMLFragment descriptionContent(ComponentTile tile) {
		String tooltip = MetaResourceProvider.INSTANCE.getTooltip(tile.getBusinessObject());
		if (StringServices.isEmpty(tooltip)) {
			return empty();
		}
		try {
			SafeHTML.getInstance().check(tooltip);
		} catch (I18NException ex) {
			tooltip = TagUtil.encodeXMLAttribute(tooltip);
		}
		return Fragments.htmlSource(tooltip);
	}

}

