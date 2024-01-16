/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;


import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.resource.NullSafeResourceProvider;
import com.top_logic.util.Resources;

/**
 * A {@link LabelProvider} for {@link LayoutComponent}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class LayoutComponentLabelProvider extends NullSafeResourceProvider {

	/** Returns the {@link LayoutComponentLabelProvider} instance. */
	public static final LayoutComponentLabelProvider INSTANCE = new LayoutComponentLabelProvider();

	@Override
	public String getLabelNullSafe(Object object) {
		LayoutComponent component = (LayoutComponent) object;
		return Resources.getInstance().getString(component.getTitleKey());
	}

}
