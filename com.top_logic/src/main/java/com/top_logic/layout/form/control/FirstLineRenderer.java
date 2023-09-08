/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;

/**
 * {@link Renderer} writing a shot representation of a value with a tool-tip showing the complete
 * content.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class FirstLineRenderer implements Renderer<Object> {

	private static final String CONTENT_CSS_CLASS = "content";

	/**
	 * {@link FirstLineRenderer} with default configuration.
	 */
	public static final FirstLineRenderer DEFAULT_INSTANCE =
		new FirstLineRenderer(FirstLineLabelProvider.DEFAULT_INSTANCE, WikiTextRenderer.DEFAULT_INSTANCE);

	private final LabelProvider _labelProvider;

	private final Renderer<Object> _tooltipRenderer;

	/**
	 * Creates a {@link FirstLineRenderer} from configuration.
	 */
	public FirstLineRenderer(LabelProvider labelProvider, Renderer<Object> tooltipRenderer) {
		_labelProvider = labelProvider;
		_tooltipRenderer = tooltipRenderer;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, Object value) throws IOException {
		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, FormConstants.FLEXIBLE_CSS_CLASS);
		if (value != null && _tooltipRenderer != null) {
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out,
				Fragments.rendered(_tooltipRenderer, value));
		}
		out.endBeginTag();
		{
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, CONTENT_CSS_CLASS);
			out.endBeginTag();

			if (value != null) {
				out.writeText(_labelProvider.getLabel(value));
			}

			out.endTag(SPAN);
		}
		out.endTag(SPAN);
	}

}
