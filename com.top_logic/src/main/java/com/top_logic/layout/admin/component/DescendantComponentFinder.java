/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.table.component.BuilderComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;

/**
 * @author     <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DescendantComponentFinder {

	/**
	 * Look up the first (button or grid) component which is a child of the given component.
	 * 
	 * @param aComponent
	 *        The component to start the search at.
	 * @return The found component or <code>null</code>.
	 */
	@SuppressWarnings("null")
	public static LayoutComponent findButtonOrDirectComponent(LayoutComponent aComponent, boolean acceptFormComp) {
		LayoutComponent theButton = aComponent.getButtonComponent();

		if (theButton == null) {
			if (aComponent instanceof LayoutContainer) {
				LayoutContainer theCont = (LayoutContainer) aComponent;
				for (LayoutComponent child : theCont.getChildList()) {
					if (child instanceof CompoundSecurityLayout) {
						theButton = null; // Ignore warning in Eclipse: this happens in the for loop
					} else if (child instanceof LayoutContainer) {
						theButton = findButtonOrDirectComponent(child, acceptFormComp);
					} else {
						if (child instanceof FormComponent
							|| child instanceof BuilderComponent) {
							if (acceptFormComp || child.getButtonComponent() != null) {
								theButton = child;
							}
						}
					}
					if (theButton != null) {
						break;
					}
				}
			}
		}

		return (theButton);
	}

}
