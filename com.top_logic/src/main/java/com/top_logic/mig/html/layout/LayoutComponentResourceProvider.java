/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.LabelProvider;
import com.top_logic.util.Resources;

/**
 * A {@link LabelProvider} for {@link LayoutComponent}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class LayoutComponentResourceProvider extends AbstractResourceProvider {

	/** Returns the {@link LayoutComponentResourceProvider} instance. */
	public static final LayoutComponentResourceProvider INSTANCE = new LayoutComponentResourceProvider();

	@Override
	public String getLabel(Object object) {
		LayoutComponent component = (LayoutComponent) object;
		if (component == null) {
			return null;
		}
		return Resources.getInstance().getString(component.getTitleKey());
	}

	@Override
	public String getTooltip(Object object) {
		LayoutComponent component = (LayoutComponent) object;
		if (component == null) {
			return null;
		}
		return Resources.getInstance().getString(component.getTitleKey().tooltip(), null);
	}

}
