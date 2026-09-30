/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui;

import com.top_logic.layout.ResourceProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;

/**
 * {@link ResourceProvider} for {@link TLObject} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WrapperResourceProvider extends AbstractTLItemResourceProvider {

	/**
	 * Singleton {@link WrapperResourceProvider} instance.
	 */
	@SuppressWarnings("hiding")
	public static final WrapperResourceProvider INSTANCE = new WrapperResourceProvider();

	/**
	 * Creates a new {@link WrapperResourceProvider}. Links will be created to "defaultFor"
	 * component.
	 */
    protected WrapperResourceProvider() {
        this(null);
    }

	/**
	 * Creates a new {@link WrapperResourceProvider}. Links will be created to the given component.
	 */
	public WrapperResourceProvider(LayoutComponent gotoComponent) {
		super(gotoComponent);
    }

}
