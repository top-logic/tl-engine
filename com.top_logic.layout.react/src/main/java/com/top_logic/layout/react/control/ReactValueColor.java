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
 * A control names the color role of the value it displays under {@link #ROLE}, either in its own
 * state or in the descriptor of a single option or value. The client draws a value that has a role
 * as a pill of that role and one without as plain text, so a value the model gives no color is a
 * descriptor that names none. What travels is the external name of the {@link ValueColor}, never
 * a color value: how a role looks is the design system's, in the stylesheet.
 * </p>
 *
 * @see AnnotationValueColorProvider
 */
public final class ReactValueColor {

	/** Name of the state entry or descriptor field naming the color role of a value. */
	public static final String ROLE = "colorRole";

	private ReactValueColor() {
		// Static utility.
	}

	/**
	 * The color role the given value is displayed with.
	 *
	 * @param value
	 *        The value to display. May be <code>null</code>.
	 * @return The external name of the role, or <code>null</code> if the value has no color.
	 */
	public static String roleOf(Object value) {
		ValueColor color = AnnotationValueColorProvider.INSTANCE.colorOf(value);
		return color == null ? null : color.getExternalName();
	}

	/**
	 * Names the color role of the given value in the given descriptor under {@link #ROLE}, unless
	 * the value has no color.
	 *
	 * @param descriptor
	 *        The option or value descriptor sent to the client.
	 * @param value
	 *        The value the descriptor describes.
	 */
	public static void putRole(Map<String, Object> descriptor, Object value) {
		String role = roleOf(value);
		if (role != null) {
			descriptor.put(ROLE, role);
		}
	}

}
