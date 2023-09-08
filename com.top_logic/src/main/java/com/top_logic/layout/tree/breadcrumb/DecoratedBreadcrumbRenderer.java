/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.breadcrumb;

import java.io.IOException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.ContentDecorator;
import com.top_logic.layout.DisplayContext;

/**
 * The class {@link DecoratedBreadcrumbRenderer} is a {@link DefaultBreadcrumbRenderer} which is
 * decorated by some {@link ContentDecorator}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DecoratedBreadcrumbRenderer extends DefaultBreadcrumbRenderer {

	/**
	 * Configuration interface to configure {@link DecoratedBreadcrumbRenderer}
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public interface Config extends DefaultBreadcrumbRenderer.Config {
		/** Property name for the name of the root tag in the displayed breadcrumb */
		String DECORATOR_PROPERTY_NAME = "decorator";

		/** name of the root tag in the breadcrumb. */
		@Name(DECORATOR_PROPERTY_NAME)
		@InstanceFormat
		ContentDecorator getDecorator();
	}

	private ContentDecorator decorator;

	public DecoratedBreadcrumbRenderer(InstantiationContext context, Config config) {
		super(context, config);
		decorator = config.getDecorator();
	}

	@Override
	protected void writeControlContents(DisplayContext context, TagWriter out, BreadcrumbControl control)
			throws IOException {
		decorator.startDecoration(context, out, control);
		super.writeControlContents(context, out, control);
		decorator.endDecoration(context, out, control);
	}

}

