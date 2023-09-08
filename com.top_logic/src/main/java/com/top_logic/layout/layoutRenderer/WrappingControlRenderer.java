/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.layoutRenderer;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.structure.WrappingControl;

/**
 * {@link LayoutControlRenderer} displaying a {@link WrappingControl} by simply rendering its single
 * child control.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WrappingControlRenderer extends LayoutControlRenderer<WrappingControl<?>> {
	
	/**
	 * Singleton {@link WrappingControlRenderer} instance.
	 */
	public static final WrappingControlRenderer INSTANCE = new WrappingControlRenderer();

	private WrappingControlRenderer() {
		// Singleton constructor.
	}

	@Override
	protected void writeControlTagAttributes(DisplayContext context, TagWriter out, WrappingControl<?> control)
			throws IOException {
		super.writeControlTagAttributes(context, out, control);

		writeLayoutInformationAttribute(Orientation.VERTICAL, 100, out);
		control.writeLayoutSizeAttribute(out);
	}

	@Override
	protected void writeControlContents(DisplayContext context, TagWriter out, WrappingControl<?> control)
			throws IOException {
		LayoutControl child = control.getChildControl();
		if (child != null) {
			child.write(context, out);
		}
	}

}
