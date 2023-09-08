/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.io.IOException;

import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.control.DownloadControl;
import com.top_logic.layout.form.control.DownloadImageRenderer;

/**
 * Renderer that renders {@link BinaryDataSource}.
 * 
 * <p>
 * This renderer creates a {@link DownloadControl} for the rendered {@link BinaryDataSource} and
 * writes it using a {@link DownloadImageRenderer}.
 * </p>
 * 
 * <p>
 * <code>null</code> data are not rendered.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DocumentDownloadRenderer implements Renderer<BinaryDataSource> {

	/** Singleton instance of {@link DocumentDownloadRenderer} */
	public static final DocumentDownloadRenderer INSTANCE = new DocumentDownloadRenderer();

	private DocumentDownloadRenderer() {
		// singleton instance
	}

	@Override
	public void write(DisplayContext context, TagWriter out, BinaryDataSource value) throws IOException {
		if (value == null) {
			return;
		}

		writeDataItem(context, out, value);
	}

	private void writeDataItem(DisplayContext context, TagWriter out, BinaryDataSource dataItem) throws IOException {
		DownloadControl downloadControl = new DownloadControl(dataItem);
		downloadControl.setRenderer(DownloadImageRenderer.INSTANCE);
		downloadControl.write(context, out);
	}

}

