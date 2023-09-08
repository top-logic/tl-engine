/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.visit;

import com.top_logic.knowledge.gui.WrapperResourceProvider;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModelVisitor;
import com.top_logic.model.resources.TLPartResourceProvider;

/**
 * {@link TLModelVisitor} that resolves the {@link ResourceProvider#getLink(DisplayContext, Object)
 * link} for a model element.
 * 
 * @see TLPartResourceProvider#getLink(DisplayContext, Object)
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LinkVisitor extends DefaultTLModelVisitor<String, DisplayContext> {

	/**
	 * Creates a new {@link LinkVisitor}.
	 * 
	 * @param resourceProvider
	 *        The {@link ResourceProvider} for which this {@link LinkVisitor} creates a link.
	 */
	public LinkVisitor(ResourceProvider resourceProvider) {
		// singleton instance
	}

	@Override
	protected String visitModelPart(TLModelPart model, DisplayContext arg) {
		return WrapperResourceProvider.INSTANCE.getLink(arg, model);
	}

}
