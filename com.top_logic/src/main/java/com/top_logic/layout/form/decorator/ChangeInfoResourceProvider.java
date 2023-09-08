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
import com.top_logic.util.Resources;

/**
 * {@link ResourceProvider} for {@link ChangeInfo}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ChangeInfoResourceProvider extends AbstractResourceProvider {

	@Override
	public String getLabel(Object object) {
		if (object == null) {
			return super.getLabel(object);
		}
		ResKey i18nKey = asChangeInfo(object).getI18NKey();
		return Resources.getInstance().getString(i18nKey);
	}

	@Override
	public ThemeImage getImage(Object object, Flavor aFlavor) {
		if (object == null) {
			return super.getImage(object, aFlavor);
		}

		return asChangeInfo(object).getImage();
	}

	private ChangeInfo asChangeInfo(Object object) {
		return (ChangeInfo) object;
	}

}

