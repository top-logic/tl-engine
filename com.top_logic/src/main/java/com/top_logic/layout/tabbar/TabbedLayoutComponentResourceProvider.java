/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tabbar;

import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.resource.NullSafeResourceProvider;
import com.top_logic.mig.html.layout.Card;

/**
 * A {@link ResourceProvider} for the {@link Card} with {@link TabInfo} as
 * {@link Card#getCardInfo()}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TabbedLayoutComponentResourceProvider extends NullSafeResourceProvider {

	@Override
	public String getLabelNullSafe(Object object) {
		return tabInfo(object).getLabel();
	}

	@Override
	public ThemeImage getImageNullSafe(Object object, Flavor flavor) {
		return tabInfo(object).getImage();
	}

	private TabInfo tabInfo(Object card) {
		return (TabInfo) ((Card) card).getCardInfo();
	}

}
