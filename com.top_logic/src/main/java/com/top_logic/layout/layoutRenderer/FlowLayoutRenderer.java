/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.layoutRenderer;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.structure.ContainerControl;
import com.top_logic.layout.structure.FlowLayoutControl;
import com.top_logic.layout.structure.OrientationAware.Orientation;

/**
 * Base class to render content of a {@link FlowLayoutControl}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public abstract class FlowLayoutRenderer extends LayoutControlRenderer<ContainerControl<?>> {

	@Override
	protected void writeControlTagAttributes(DisplayContext context, TagWriter out, ContainerControl<?> control)
			throws IOException {
		super.writeControlTagAttributes(context, out, control);

		ContainerControl<?> flowLayout = control;
		flowLayout.writeLayoutSizeAttribute(out);
		internalWriteLayoutInformation(flowLayout, out);
	}

	@Override
	public void writeControlContents(DisplayContext context, TagWriter out, ContainerControl<?> value)
			throws IOException {
		ContainerControl<?> flowLayout = value;
		writeLayoutChildren(context, out, flowLayout);
	}

	private void internalWriteLayoutInformation(ContainerControl<?> flowLayout, TagWriter out) throws IOException {
		Orientation orientation = flowLayout.getOrientation();
		int maxSize = orientation.isHorizontal() ?
				flowLayout.getConstraint().getMaxWidth() : flowLayout.getConstraint().getMaxHeight();
		writeLayoutInformationAttribute(orientation, maxSize, out);
	}


	@SuppressWarnings("javadoc")
	protected abstract void writeLayoutChildren(DisplayContext context, TagWriter out, ContainerControl<?> flowLayout)
			throws IOException;

}
