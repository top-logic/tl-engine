/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;
import com.top_logic.util.Resources;

/**
 * {@link ResourceProvider} base class for {@link TLObject}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTLItemResourceProvider extends DefaultResourceProvider {

	private final LayoutComponent _gotoComponent;

	/**
	 * Creates a {@link AbstractTLItemResourceProvider}.
	 */
	public AbstractTLItemResourceProvider(LayoutComponent gotoComponent) {
		_gotoComponent = gotoComponent;
	}

	@Override
	public abstract String getLabel(Object object);

	/**
	 * Access to {@link DefaultResourceProvider#getLabel(Object)}
	 */
	public final String getDefaultLabel(Object object) {
		return super.getLabel(object);
	}

	@Override
	public String getLink(DisplayContext context, Object anObject) {
		return GotoHandler.getJSCallStatement(context, _gotoComponent, anObject);
	}

	/**
	 * Same as {@link #getLabel(Object)}.
	 */
	public static String getMetaElementLabel(TLType type) {
		return Resources.getInstance().getString(TLModelNamingConvention.getTypeLabelKey(type));
	}

}
