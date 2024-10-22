/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout.tiles;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.Described;
import com.top_logic.bpe.bpml.model.Iconified;
import com.top_logic.bpe.bpml.model.Named;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;
import com.top_logic.mig.html.layout.tiles.component.LabelBasedPreview;

/**
 * {@link LabelBasedPreview} of {@link Collaboration} models.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BPMLTilePreview<C extends LabelBasedPreview.Config<?>> extends LabelBasedPreview<C> {

	/**
	 * Creates a {@link BPMLTilePreview}.
	 */
	public BPMLTilePreview(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected HTMLFragment labelContent(ComponentTile tile) {
		Object model = model(tile);
		HTMLFragment result = specificLabelContent(model);
		if (result != null) {
			return result;
		}

		return defaultLabelContent(tile);
	}

	/**
	 * Label for the given BPML model element.
	 */
	protected HTMLFragment specificLabelContent(Object model) {
		if (model instanceof Named) {
			String name = ((Named) model).getName();
			if (!StringServices.isEmpty(name)) {
				return Fragments.text(name);
			}
		}
		return null;
	}

	/**
	 * Fallback label for the given BPML model element.
	 * 
	 * @param tile
	 *        The tile to create the preview for.
	 */
	protected HTMLFragment defaultLabelContent(ComponentTile tile) {
		return Fragments.text(MetaResourceProvider.INSTANCE.getLabel(model(tile)));
	}

	@Override
	protected HTMLFragment descriptionContent(ComponentTile tile) {
		Object model = model(tile);
		HTMLFragment result = specificDescriptionContent(model);
		if (result != null) {
			return result;
		}
		return defaultDescriptionContent(tile);
	}

	/**
	 * Description for the given BPML model element.
	 */
	protected HTMLFragment specificDescriptionContent(Object model) {
		if (model instanceof Described) {
			I18NStructuredText html = ((Described) model).getDescription();
			if (html != null) {
				return Fragments.htmlSource(html.localizeSourceCode());
			}
		}
		return null;
	}

	/**
	 * Fallback description for the given BPML model element.
	 * 
	 * @param tile
	 *        The tile to create the preview for.
	 */
	protected HTMLFragment defaultDescriptionContent(ComponentTile tile) {
		return Fragments.empty();
	}

	@Override
	protected ThemeImage icon(ComponentTile tile) {
		Object model = model(tile);
		ThemeImage result = specificIcon(model);
		if (result != null) {
			return result;
		}

		return defaultIcon(tile);
	}

	/**
	 * Icon for the given BPML model element.
	 */
	protected ThemeImage specificIcon(Object model) {
		if (model instanceof Iconified) {
			ThemeImage icon = ((Iconified) model).getIcon();
			if (icon != null) {
				return icon;
			}
		}
		return null;
	}

	/**
	 * Fallback icon for the given BPML model element.
	 * 
	 * @param tile
	 *        The tile to create the preview for.
	 */
	protected ThemeImage defaultIcon(ComponentTile tile) {
		ThemeImage configuredIcon = super.icon(tile);
		if (configuredIcon != null) {
			return configuredIcon;
		}

		return MetaResourceProvider.INSTANCE.getImage(model(tile), Flavor.ENLARGED);
	}

	/**
	 * BPML model element of the given tile.
	 */
	protected Object model(ComponentTile tile) {
		return tile.getBusinessObject();
	}

}
