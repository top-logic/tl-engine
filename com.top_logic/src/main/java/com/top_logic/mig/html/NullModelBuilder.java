/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * A ModelBuilder that does nothing (use for demo / testing).
 * 
 * @author     <a href="mailto:kha@top-logic.com">kha</a>
 */
public class NullModelBuilder implements ModelBuilder {

	/**
	 * Singleton {@link NullModelBuilder} instance.
	 */
	public static final NullModelBuilder INSTANCE = new NullModelBuilder();

	private NullModelBuilder() {
		// Singleton constructor.
	}

    /**
     * Our model is always null as the name of this class implies.
     */
    @Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
        return null;
    }

    /**
     * We support null models only.
     */
    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
        return aModel == null;
    }

}
