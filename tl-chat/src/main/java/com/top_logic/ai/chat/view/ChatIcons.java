/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.ReactImages;

/**
 * The form in which the chat's controls send a configured icon to the browser's {@code ThemeIcon}.
 *
 * <p>
 * The engine's {@link ReactImages} decides it, as for its own controls: an icon class travels in
 * its encoded form, an image file as the URL it is served at - context path and theme folder
 * included, which the encoded form of a file names neither of. The chat adds only that no icon is
 * the empty string, which {@code ThemeIcon} and the working indicator read as "draw nothing of
 * your own".
 * </p>
 */
final class ChatIcons {

	private ChatIcons() {
		// Utility.
	}

	/**
	 * The given icon as the chat's client renders it.
	 *
	 * @param context
	 *        The context of the control sending the icon, giving the application's context path.
	 * @param image
	 *        The configured icon, or <code>null</code>.
	 * @return The URL of an image file, the encoded form of an icon class, or the empty string for
	 *         no icon and for the invisible one.
	 */
	static String encode(ReactContext context, ThemeImage image) {
		if (image == null || image.resolve() == ThemeImage.none()) {
			return "";
		}
		String encoded = ReactImages.encode(context, image);
		return encoded == null ? "" : encoded;
	}
}
