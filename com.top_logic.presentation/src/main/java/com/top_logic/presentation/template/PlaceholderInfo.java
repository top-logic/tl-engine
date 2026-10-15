/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.template;

import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.apache.poi.xslf.usermodel.XSLFShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;

/**
 * A placeholder of a slide layout or slide master of a presentation template.
 *
 * @param type
 *        The placeholder type as named in the file format, e.g. <code>title</code>,
 *        <code>ctrTitle</code>, <code>subTitle</code>, <code>body</code>, <code>dt</code>,
 *        <code>ftr</code>, <code>sldNum</code>, <code>pic</code>, <code>obj</code>.
 * @param idx
 *        The index identifying the placeholder among the placeholders of the same type. A slide
 *        shape fills the layout placeholder with the same type and index.
 * @param name
 *        The name of the placeholder shape.
 * @param bounds
 *        The effective position and size in points, inherited from the master if the placeholder
 *        does not define its own.
 * @param text
 *        The effective style of the first text level.
 * @param prompt
 *        The text shown in the empty placeholder.
 */
public record PlaceholderInfo(String type, long idx, String name, Rectangle2D bounds, TextStyle text,
		String prompt) {

	/**
	 * The data representation with the keys {@link TemplateData#TYPE}, {@link TemplateData#IDX},
	 * {@link TemplateData#NAME}, the bounds ({@link TemplateData#X}, {@link TemplateData#Y},
	 * {@link TemplateData#WIDTH}, {@link TemplateData#HEIGHT}), {@link TemplateData#TEXT} and
	 * {@link TemplateData#PROMPT}.
	 */
	public Map<String, Object> toData() {
		Map<String, Object> result = TemplateData.map();
		result.put(TemplateData.TYPE, type);
		result.put(TemplateData.IDX, Double.valueOf(idx));
		TemplateData.put(result, TemplateData.NAME, name);
		TemplateData.putBounds(result, bounds);
		TemplateData.put(result, TemplateData.TEXT, text == null ? null : text.toData());
		TemplateData.put(result, TemplateData.PROMPT, prompt);
		return result;
	}

	/**
	 * The placeholder type of the given shape as named in the file format, see {@link #type()}.
	 *
	 * @return The type, or <code>null</code> if the shape is no placeholder.
	 */
	public static String typeOf(XSLFShape shape) {
		CTPlaceholder ph = TemplateAnalyzer.placeholder(shape);
		return ph == null ? null : TemplateAnalyzer.placeholderType(ph);
	}

}
