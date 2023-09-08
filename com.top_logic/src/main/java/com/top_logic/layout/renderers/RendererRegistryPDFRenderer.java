/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.renderers;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.tool.export.pdf.PDFRenderer;

/**
 * PDF renderer that uses the configured renderer for the value to get {@link Renderer} for PDF
 * export.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RendererRegistryPDFRenderer implements PDFRenderer {

	/** Singleton {@link RendererRegistryPDFRenderer} instance. */
	public static final RendererRegistryPDFRenderer INSTANCE = new RendererRegistryPDFRenderer();

	/**
	 * Creates a new {@link RendererRegistryPDFRenderer}.
	 */
	protected RendererRegistryPDFRenderer() {
		// singleton instance
	}

	@Override
	public void write(DisplayContext context, TagWriter out, Object contextObject, Object value) throws IOException {
		if (value == null) {
			return;
		}
		LabelProviderService.getInstance().getRenderer(value).write(context, out, value);
	}

}

