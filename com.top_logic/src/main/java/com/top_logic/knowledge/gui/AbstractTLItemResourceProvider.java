/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelUtil;
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

	@Override
	public final String getTooltip(Object anObject) {
		if (anObject == null) {
			return null;
		}
		return Resources.getInstance().getString(getTooltipNonNull(anObject));
	}

	/**
	 * Implementation of {@link #getTooltip(Object)} for non-<code>null</code> values.
	 */
	protected ResKey getTooltipNonNull(Object object) {
		return I18NConstants.WRAPPER_TOOLTIP.fill(
			quote(object),
			quote(TLModelUtil.type(object)));
	}

	/**
	 * Quotes the given value for insertion into the arguments of a tool-tip {@link ResKey}.
	 * 
	 * <p>
	 * Note: A tool-tip is interpreted as HTML. Therefore, all dynamic values inserted into it must
	 * be explicitly quoted.
	 * </p>
	 */
	protected static Object quote(Object value) {
		return quote(MetaResourceProvider.INSTANCE.getLabel(value));
	}

	/**
	 * Quotes the given value for insertion into the arguments of a tool-tip {@link ResKey}.
	 * 
	 * <p>
	 * Note: A tool-tip is interpreted as HTML. Therefore, all dynamic values inserted into it must
	 * be explicitly quoted.
	 * </p>
	 */
	protected static String quote(String value) {
		return TagUtil.encodeXML(value);
	}

	/**
	 * Same as {@link #getLabel(Object)}.
	 */
	public static String getMetaElementLabel(TLType type) {
		return Resources.getInstance().getString(TLModelNamingConvention.getTypeLabelKey(type));
	}

}
