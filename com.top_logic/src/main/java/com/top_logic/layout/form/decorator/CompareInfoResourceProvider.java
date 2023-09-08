/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.util.Resources;

/**
 * {@link ResourceProvider} for {@link ChangeInfo}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompareInfoResourceProvider extends AbstractResourceProvider {

	@Override
	public String getLabel(Object object) {
		if (object == null) {
			return super.getLabel(object);
		}
		return MetaResourceProvider.INSTANCE.getLabel(asCompareInfo(object).getChangeInfo());
	}

	@Override
	public String getTooltip(Object object) {
		if (object == null) {
			return super.getTooltip(object);
		}
		ResKey i18nKey = asCompareInfo(object).getTooltip();
		return Resources.getInstance().getString(i18nKey);
	}

	@Override
	public ThemeImage getImage(Object object, Flavor aFlavor) {
		if (object == null) {
			return super.getImage(object, aFlavor);
		}

		ThemeImage icon = asCompareInfo(object).getChangedIcon();
		if (icon == null) {
			return null;
		}
		return icon;
	}

	private CompareInfo asCompareInfo(Object object) {
		return (CompareInfo) object;
	}

}

