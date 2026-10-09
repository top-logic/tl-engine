/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import com.top_logic.layout.basic.ThemeImage;

/**
 * Encodes a {@link ThemeImage} for the state a React control sends to the client.
 *
 * <p>
 * The client renders an icon font class (<code>css:</code>, <code>colored:</code>) as it is, and
 * everything starting with a slash as the <code>src</code> of an image. The
 * {@link ThemeImage#toEncodedForm() encoded form} of an image file is its theme-local key, which
 * names neither the context path of the application nor the theme the file is taken from - the
 * browser would request it from the root of the server. An image file is therefore sent as the
 * URL it is served at, the same URL the classic UI writes for it.
 * </p>
 *
 * <p>
 * The image is resolved in the theme of the current user before it is encoded, so a reference - the
 * icon a theme configures for a type, say - reaches the client as the image it stands for; the
 * client has no theme to look a reference up in. The invisible image keeps its encoded form
 * <code>none</code>, which the client renders as nothing.
 * </p>
 *
 * <p>
 * Only an image the client displays is encoded this way. Where the encoded form is a value - the
 * icon a user picks in an icon chooser, say - it is sent unchanged, since a URL depends on the
 * theme and the deployment and cannot be stored.
 * </p>
 */
public class ReactImages {

	/**
	 * The form in which the given image is sent to the client for display.
	 *
	 * @param context
	 *        The context of the control sending the image, giving the context path of the
	 *        application. May be <code>null</code> where no context is at hand, in which case the
	 *        URL of an image file is relative to the server root.
	 * @param image
	 *        The image to display, or <code>null</code> for none.
	 * @return The URL of an image file, the encoded form of any other image, or <code>null</code>
	 *         when there is no image.
	 */
	public static String encode(ReactContext context, ThemeImage image) {
		if (image == null) {
			return null;
		}
		ThemeImage resolved = image.resolve();
		if (resolved instanceof ThemeImage.Img file) {
			String contextPath = context == null ? "" : context.getContextPath();
			return contextPath + file.getFileLink();
		}
		return resolved.toEncodedForm();
	}

}
