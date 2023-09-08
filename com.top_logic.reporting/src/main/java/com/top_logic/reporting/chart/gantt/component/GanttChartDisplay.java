/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.component;

import java.io.IOException;

import com.top_logic.base.chart.ImageControl;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.layoutRenderer.LayoutControlRenderer;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link ImageControl} display of {@link GanttComponent}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GanttChartDisplay implements HTMLFragment, HTMLConstants {
	private final GanttComponent _component;

	/**
	 * Creates a {@link GanttChartDisplay}.
	 */
	public GanttChartDisplay(GanttComponent component) {
		_component = component;
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		String imageId = _component.getName() + "_chart";
		ImageControl imageControl = new ImageControl(_component, null, imageId, null);
		imageControl.setUseWaitingSlider(true);
		imageControl.setRespectHorizontalScrollbar(false);
		imageControl.setRespectVerticalScrollbar(false);
		imageControl.fetchID(context.getExecutionScope().getFrameScope());

		out.beginBeginTag(DIV);
		writeLayoutingInformation(out, imageControl.getID());
		out.writeAttribute(STYLE_ATTR, "text-align:center");
		out.endBeginTag();

		imageControl.write(context, out);

		out.endTag(DIV);
	}

	private void writeLayoutingInformation(TagWriter out, String imageControlId) throws IOException {
		LayoutControlRenderer.writeLayoutConstraintInformation(100, 0, DisplayUnit.PERCENT, out);
		LayoutControlRenderer.writeLayoutResizeFunction(out,
			"(function(){var imageControlNode = document.getElementById('" + imageControlId
				+ "'); if(imageControlNode != null) {var resizeFunction = imageControlNode.resizeFunction; if(resizeFunction != null) {resizeFunction();}}})();");
	}
}