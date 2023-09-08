/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.provider.resource.NullSafeResourceProvider;

/**
 * A {@link ResourceProvider} with a different output than the normal {@link MetaResourceProvider}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DemoResourceProvider extends NullSafeResourceProvider {

	/** The {@link DemoResourceProvider} instance. */
	public static final DemoResourceProvider INSTANCE = new DemoResourceProvider();

	@Override
	protected String getLabelNullSafe(Object model) {
		return "Label: " + model;
	}

	@Override
	protected String getTooltipNullSafe(Object model) {
		return "Tooltip: " + model;
	}

	@Override
	protected String getLinkNullSafe(DisplayContext context, Object model) {
		return "https://en.wikipedia.org/wiki/" + model;
	}

	@Override
	protected ThemeImage getImageNullSafe(Object model, Flavor flavor) {
		return Icons.DEMO_RESOURCE_PROVIDER_ICON;
	}

}
