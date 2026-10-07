/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;

/**
 * The form in which a {@link ThemeImage} reaches the client of a React control.
 *
 * <p>
 * Every image a control sends in its state - the icon of a button, of a menu entry, of an option, of
 * a table cell - is written through {@link #encode(ReactContext, ThemeImage)}. The client renders it
 * with its {@code ThemeIcon} component:
 * </p>
 *
 * <ul>
 * <li>An icon-font image is sent in its encoded form, {@code css:<classes>} or
 * {@code colored:<classes>}, and rendered as an {@code <i>} element carrying the classes.</li>
 * <li>An image resource (a PNG, GIF or SVG file of the theme) is sent as the URL the browser loads
 * it from: the context path of the application followed by the image's file link in the current
 * theme, e.g. {@code /my-app/themes/core/mimetypes/tl/TLProperty.png}. It always starts with a
 * slash and is rendered as an {@code <img>} element.</li>
 * <li>The invisible image is sent as {@code none} and renders nothing.</li>
 * </ul>
 *
 * <p>
 * The image is resolved in the theme of the current user before it is written, so a reference - the
 * icon a theme configures for a type, say - reaches the client as the image it stands for; the
 * client has no theme to look a reference up in.
 * </p>
 */
public final class ClientImage {

	private ClientImage() {
		// Utility class.
	}

	/**
	 * The client form of the given image.
	 *
	 * @param context
	 *        The context of the control sending the image, providing the context path an image
	 *        resource is served under. May be <code>null</code> where no context is at hand, in
	 *        which case an image resource URL is relative to the server root.
	 * @param image
	 *        The image to send, or <code>null</code> for none.
	 * @return The client form of the resolved image, or <code>null</code> if no image was given.
	 */
	public static String encode(ReactContext context, ThemeImage image) {
		if (image == null) {
			return null;
		}
		ThemeImage resolved = image.resolve();
		if (resolved instanceof ThemeImage.Img resource) {
			String contextPath = context == null ? "" : context.getContextPath();
			return contextPath + resource.getFileLink();
		}
		return resolved.toEncodedForm();
	}

}
