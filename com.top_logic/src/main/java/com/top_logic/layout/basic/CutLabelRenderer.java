/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.tag.SelectTag;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;

/**
 * {@link Renderer} that writes a length-limited label.
 * 
 * <p>
 * If the label is cut, the whole label is rendered as tooltip on the written text.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CutLabelRenderer implements Renderer<Object> {
	private final LabelProvider _labelProvider;

	private final int _maxLengthShown;

	/**
	 * Creates a {@link CutLabelRenderer}.
	 * 
	 * @param labelProvider
	 *        The {@link LabelProvider} that creates the label.
	 * @param maxLengthShown
	 *        The length limit.
	 */
	public CutLabelRenderer(LabelProvider labelProvider, int maxLengthShown) {
		_labelProvider = labelProvider;
		_maxLengthShown = maxLengthShown;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, Object value) throws IOException {
		String label = _labelProvider.getLabel(value);

		writeWithMaxLength(context, out, label);
	}

	private void writeWithMaxLength(DisplayContext context, TagWriter out, String label) throws IOException {
		out.beginBeginTag(SelectTag.SPAN);
		if (label.length() >= _maxLengthShown) {
			// Show complete value in tool tip, if no explicit tool tip is given.
			String valueAsTooltip = TagUtil.encodeXML(label);
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, valueAsTooltip);

			label = StringServices.minimizeString(label, _maxLengthShown, _maxLengthShown - 3);
		}

		out.endBeginTag();
		{
			out.writeText(label);
		}
		out.endTag(SelectTag.SPAN);
	}
}