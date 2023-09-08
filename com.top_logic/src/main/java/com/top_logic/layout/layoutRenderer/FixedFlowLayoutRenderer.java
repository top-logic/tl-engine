/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.layoutRenderer;

import java.io.IOException;
import java.util.List;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.structure.ContainerControl;
import com.top_logic.layout.structure.FixedFlowLayoutControl;
import com.top_logic.layout.structure.LayoutControl;

/**
 * The class {@link FixedFlowLayoutRenderer} is used to render the content of a {@link FixedFlowLayoutControl}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FixedFlowLayoutRenderer extends FlowLayoutRenderer {

	/**
	 * Singleton {@link FixedFlowLayoutRenderer} instance.
	 */
	public static final FixedFlowLayoutRenderer INSTANCE = new FixedFlowLayoutRenderer();

	private FixedFlowLayoutRenderer() {
		// Singleton constructor.
	}

	@Override
	protected void writeLayoutChildren(DisplayContext context, TagWriter out,
			ContainerControl flowLayout)
			throws IOException {
		List<? extends LayoutControl> layoutChildren = flowLayout.getChildren();
		for (int index = 0, size = layoutChildren.size(); index < size; index++) {
			LayoutControl theChildLayout = layoutChildren.get(index);
			theChildLayout.write(context, out);
		}
	}
}
