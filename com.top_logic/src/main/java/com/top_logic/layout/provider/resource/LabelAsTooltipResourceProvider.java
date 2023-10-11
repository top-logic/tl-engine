/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.resource;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.provider.ConfiguredProxyResourceProvider;

/**
 * A {@link ResourceProvider} which uses the label as tooltip.
 * <p>
 * This is useful for example in table columns which contain a lot of text that might not fit
 * completely in the cell. There is no generic solution for that in the tables themselves, as the
 * cells might also contain fields or buttons which could not be displayed in a tooltip.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class LabelAsTooltipResourceProvider extends
		ConfiguredProxyResourceProvider<ConfiguredProxyResourceProvider.Config<LabelAsTooltipResourceProvider>> {

	/** {@link TypedConfiguration} constructor for {@link LabelAsTooltipResourceProvider}. */
	public LabelAsTooltipResourceProvider(InstantiationContext context, Config<LabelAsTooltipResourceProvider> config) {
		super(context, config);
	}

	@Override
	public String getTooltip(Object object) {
		return getLabel(object);
	}

}
