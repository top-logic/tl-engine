/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.layoutRenderer;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.structure.LayoutControlAdapter;
import com.top_logic.layout.structure.OrientationAware.Orientation;

/**
 * The class {@link LayoutControlAdapterRenderer} is used to render the content of a
 * {@link LayoutControlAdapter}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LayoutControlAdapterRenderer extends LayoutControlRenderer<LayoutControlAdapter> {

	/**
	 * Singleton {@link LayoutControlAdapterRenderer} instance.
	 */
	public static final LayoutControlAdapterRenderer INSTANCE = new LayoutControlAdapterRenderer();


	/**
	 * Constructor for subclasses.
	 */
	protected LayoutControlAdapterRenderer() {
		// Singleton constructor.
	}

	@Override
	protected void writeControlTagAttributes(DisplayContext context, TagWriter out, LayoutControlAdapter control)
			throws IOException {
		super.writeControlTagAttributes(context, out, control);

		LayoutControlAdapter controlAdapter = control;
		controlAdapter.writeLayoutSizeAttribute(out);
		writeLayoutInformationAttribute(Orientation.HORIZONTAL, 100, out);
	}

	@Override
	protected void writeControlContents(DisplayContext context, TagWriter out, LayoutControlAdapter control)
			throws IOException {
		LayoutControlAdapter controlAdapter = control;
		controlAdapter.getWrappedView().write(context, out);
	}

}
