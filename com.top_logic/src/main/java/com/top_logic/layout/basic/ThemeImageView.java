/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.View;

/**
 * {@link View} displaying an imag showing the given {@link ThemeImage}.
 * 
 * @see ResourceImageView A view for an internatiolized image.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ThemeImageView extends DefaultView {

	private final ThemeImage _image;

	private final ResKey _altResKey;

	/**
	 * Creates a new {@link ThemeImageView} with no alternative text.
	 * 
	 * @param image
	 *        The displayed image.
	 */
	public ThemeImageView(ThemeImage image) {
		this(image, null);
	}

	/**
	 * Creates a new {@link ThemeImageView} .
	 * 
	 * @param image
	 *        The displayed image.
	 * @param alternativeTextResourceKey
	 *        A resource key for the alternative text for the written image tag.
	 */
	public ThemeImageView(ThemeImage image, ResKey alternativeTextResourceKey) {
		_image = image;
		_altResKey = alternativeTextResourceKey;
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		ResKey altTextResKey = getAltTextResKey();
		ResKey altText;
		if (altTextResKey == null) {
			altText = ResKey.text("");
		} else {
			altText = altTextResKey;
		}

		getImage().writeWithPlainTooltip(context, out, altText);
	}

	/**
	 * Getter for the resource key of the alternative text or <code>null</code> if no one was set.
	 */
	public final ResKey getAltTextResKey() {
		return _altResKey;
	}

	/**
	 * Getter for the displayed image.
	 */
	public final ThemeImage getImage() {
		return _image;
	}

}
