/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import java.util.Map;

import com.top_logic.model.annotate.ui.AnnotationValueColorProvider;
import com.top_logic.model.annotate.ui.ValueColor;

/**
 * The color a value is displayed with, as the React client receives it.
 *
 * <p>
 * A control names the color of the value it displays under {@link #COLOR}, either in its own state
 * or in the descriptor of a single option or value. The client draws a value that has a color as a
 * pill in that color and one without as plain text, so a value the model gives no color is a
 * descriptor that names none.
 * </p>
 *
 * @see AnnotationValueColorProvider
 */
public final class ReactValueColor {

	/** Name of the state entry or descriptor field naming the color of a value. */
	public static final String COLOR = "color";

	private ReactValueColor() {
		// Static utility.
	}

	/**
	 * The CSS color the given value is displayed with.
	 *
	 * @param value
	 *        The value to display. May be <code>null</code>.
	 * @return The CSS the color is applied with (a color value, or a reference to a theme design
	 *         token), or <code>null</code> if the value has no color.
	 */
	public static String cssColorOf(Object value) {
		ValueColor color = AnnotationValueColorProvider.INSTANCE.colorOf(value);
		return color == null ? null : color.cssValue();
	}

	/**
	 * Names the color of the given value in the given descriptor under {@link #COLOR}, unless the
	 * value has no color.
	 *
	 * @param descriptor
	 *        The option or value descriptor sent to the client.
	 * @param value
	 *        The value the descriptor describes.
	 */
	public static void putColor(Map<String, Object> descriptor, Object value) {
		String css = cssColorOf(value);
		if (css != null) {
			descriptor.put(COLOR, css);
		}
	}

}
