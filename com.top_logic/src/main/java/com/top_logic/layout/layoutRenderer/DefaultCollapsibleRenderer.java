/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.layoutRenderer;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractControlRenderer;
import com.top_logic.layout.structure.CollapsibleControl;
import com.top_logic.mig.html.HTMLConstants;

/**
 * The class {@link DefaultCollapsibleRenderer} is a renderer which can be used to render
 * {@link CollapsibleControl collapsible controls}. It renders a {@link HTMLConstants#DIV div} with
 * an image to collapse it, additional to the actual content.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultCollapsibleRenderer extends AbstractControlRenderer<CollapsibleControl> {

	/**
	 * Singleton {@link DefaultCollapsibleRenderer} instance.
	 */
	public static final DefaultCollapsibleRenderer INSTANCE = new DefaultCollapsibleRenderer();

	private DefaultCollapsibleRenderer() {
		// Singleton constructor.
	}

	@Override
	public void write(DisplayContext context, TagWriter out, CollapsibleControl control) throws IOException {
		Icons.COLLAPSIBLE_TEMPLATE.get().write(context, out, control);
	}

}
