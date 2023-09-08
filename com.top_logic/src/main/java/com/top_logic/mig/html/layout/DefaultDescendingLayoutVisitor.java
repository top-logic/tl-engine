/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.layout.basic.component.AJAXComponent;

/**
 * Default implementation of {@link LayoutComponentVisitor} for visiting and
 * descending a component.
 * 
 * <p>
 * The default implementation is to decide to descend into all children of the
 * component tree.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultDescendingLayoutVisitor implements LayoutComponentVisitor {

	@Override
	public boolean visitLayoutComponent(LayoutComponent aComponent) {
		return true;
	}

    @Override
	public boolean visitAJAXComponent(AJAXComponent aComponent) {
        // Fallback to LayoutComponent, if no special handling for 
        // AJAXComponent is implemented.
        return this.visitLayoutComponent(aComponent);
    }
	@Override
	public boolean visitLayoutContainer(LayoutContainer aComponent) {
		// Fallback to AJAXComponent, if no special handling for
		// LayoutContainer is implemented.
		return this.visitAJAXComponent(aComponent);
	}

	@Override
	public boolean visitMainLayout(MainLayout aComponent) {
		// Fallback to LayoutContainer, if no special handling for MainLayouts is
		// implemented.
		return this.visitLayoutContainer(aComponent);
	}
}
