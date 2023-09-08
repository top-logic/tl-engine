/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.resource.NullSafeResourceProvider;
import com.top_logic.util.Resources;

/**
 * A {@link ResourceProvider} for {@link ComponentTile}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ComponentTileResourceProvider extends NullSafeResourceProvider {

	@Override
	public String getLabelNullSafe(Object object) {
		ResKey labelKey = ((ComponentTile) object).getTileLabel();
		return Resources.getInstance().getString(labelKey);
	}

	@Override
	public ThemeImage getImageNullSafe(Object object, Flavor flavor) {
		ComponentTile componentTile = (ComponentTile) object;
		TilePreview preview = componentTile.getPreview();
		if (!(preview instanceof LabelBasedPreview)) {
			return null;
		}
		LabelBasedPreview<?> labelBasedPreview = (LabelBasedPreview<?>) preview;
		return labelBasedPreview.icon(componentTile);
	}

}
