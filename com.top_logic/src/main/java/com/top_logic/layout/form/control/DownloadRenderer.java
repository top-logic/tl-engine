/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DefaultControlRenderer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;

/**
 * {@link DefaultControlRenderer} that renders an {@link IDownloadControl} as simple link
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DownloadRenderer extends DefaultControlRenderer<IDownloadControl> {

	/** Instance of {@link DownloadRenderer} */
	public static final DownloadRenderer INSTANCE = new DownloadRenderer();

	@Override
	protected String getControlTag(IDownloadControl control) {
		return ANCHOR;
	}

	@Override
	protected void writeControlTagAttributes(DisplayContext context, TagWriter out, IDownloadControl control)
			throws IOException {
		super.writeControlTagAttributes(context, out, control);
		out.writeAttribute(HREF_ATTR, HREF_EMPTY_LINK);
		BinaryDataSource dataItem = control.dataItem();
		if (dataItem != null) {
			String fileName = dataItem.getName();
			ResKey tooltip = I18NConstants.DOWNLOAD_TOOLTIP__FILENAME.fill(fileName);
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltip);
		} else {
			out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
		}
		writeOnClick(out, control);
	}

	private void writeOnClick(TagWriter out, Control control) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		DownloadCommand.INSTANCE.writeInvokeExpression(out, control);
		out.append(";return false;");
		out.endAttribute();
	}

	@Override
	protected void writeControlContents(DisplayContext context, TagWriter out, IDownloadControl control)
			throws IOException {
		IDownloadControl downloadControl = control;
		BinaryDataSource dataItem = downloadControl.dataItem();
		if (dataItem != null) {
			DownloadImageRenderer.writeImage(context, out, dataItem);
			out.writeText(dataItem.getName());
		} else {
			out.writeText(context.getResources().getString(downloadControl.noValueKey()));
		}
	}

}

