/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.rendering;

import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.Resources;

/**
 * Labels with timing info
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class LayoutComponentNodeLabelProvider extends DefaultResourceProvider {

	/** Singleton {@link LayoutComponentNodeLabelProvider} instance. */
	@SuppressWarnings("hiding")
	public static final LayoutComponentNodeLabelProvider INSTANCE = new LayoutComponentNodeLabelProvider();

	/**
	 * Creates a new {@link LayoutComponentNodeLabelProvider}.
	 */
	protected LayoutComponentNodeLabelProvider() {
		// singleton instance
	}

    /** 
     * @see com.top_logic.layout.ResourceProvider#getImage(java.lang.Object, Flavor)
     */
    @Override
	public ThemeImage getImage(Object aNode, Flavor aFlavor) {
		return com.top_logic.layout.Icons.V;
    }

    /** 
     * @see com.top_logic.layout.ResourceProvider#getLabel(java.lang.Object)
     */
    @Override
	public String getLabel(Object aNode) {
		LayoutComponentNode theComp = (LayoutComponentNode) aNode;
		LayoutComponent layoutComponent = theComp.getComponent();
		Resources theRes = Resources.getInstance();
		String theName = theRes.getString(layoutComponent.getTitleKey(), null);

        if (theName == null) {
			theName = layoutComponent.getName().qualifiedName();
        }

		return (theName + " (" + theComp.getMaxLocalTime() + ", " + theComp.getMaxSubTime() + ", "
			+ theComp.getSumTimes() + ")");
    }
}