/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.layoutRenderer;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.structure.CockpitControl;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.OrientationAware.Orientation;

/**
 * The class {@link CockpitRenderer} is used to render the content of a {@link CockpitControl}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CockpitRenderer extends LayoutControlRenderer<CockpitControl> {

	/**
	 * Singleton {@link CockpitRenderer} instance.
	 */
	public static final CockpitRenderer INSTANCE = new CockpitRenderer();

	private CockpitRenderer() {
		// Singleton constructor.
	}

	@Override
	protected void writeControlTagAttributes(DisplayContext context, TagWriter out, CockpitControl control)
			throws IOException {
		super.writeControlTagAttributes(context, out, control);

		CockpitControl cockpitControl = control;
		cockpitControl.writeLayoutSizeAttribute(out);
		writeLayoutInformationAttribute(Orientation.VERTICAL, 100, out);
	}

	@Override
	public void writeControlContents(DisplayContext context, TagWriter out, CockpitControl value) throws IOException {
		CockpitControl cockpitControl = value;
		LayoutControl theMaximizedControl = cockpitControl.getMaximizedControl();
		if (theMaximizedControl == null) {
			cockpitControl.getChildControl().write(context, out);
		} else {
			theMaximizedControl.write(context, out);
		}
	}

}
