/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import com.top_logic.layout.basic.ThemeImage;

/**
 * {@link Format} parsing the path of a theme image into the corresponding {@link ThemeImage}
 * object.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class ThemeImageFormat extends Format {

	/**
	 * Singleton {@link ThemeImageFormat} instance.
	 */
	public static final ThemeImageFormat INSTANCE = new ThemeImageFormat();

	private ThemeImageFormat() {
		// Singleton constructor.
	}

	/**
	 * Returns the {@link StringBuffer} toAppendTo lenghtened by the path of the given theme image.
	 * 
	 * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer,
	 *      java.text.FieldPosition)
	 */
	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		if (toAppendTo == null || pos == null) {
			throw new NullPointerException("\"pos\" and \"toAppendTo\" have to be non-null");
		} else {
			ThemeImage img;
			try {
				img = (ThemeImage) obj;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException("The object \"obj\" has to be a ThemeImage object");
			}
			String path = pathValue(img);
			return toAppendTo.append(path);
		}
	}

	/**
	 * Returns the theme image coded by the path in the source beginning from the index of the
	 * ParsePosition. If the source is to short, the errorIndex of the ParsePosition is set to the
	 * length of the source
	 * 
	 * @see java.text.Format#parseObject(java.lang.String, java.text.ParsePosition)
	 */
	@Override
	public Object parseObject(String source, ParsePosition pos) {
		if (pos == null)
			throw new NullPointerException("pos is null");
		int start = pos.getIndex();
		// checks if the source is to short
		if (source.length() <= start) {
			pos.setErrorIndex(source.length());
			return null;
		}
		// The potential path
		String path = source.substring(start, source.length());

		ThemeImage c = themeImageValue(path);
		pos.setIndex(start + path.length());
		return c;
	}

	// returns the path of the given theme image
	private String pathValue(ThemeImage configValue) {
		return configValue.toEncodedForm();
	}

	// returns the theme image corresponding to image path
	private ThemeImage themeImageValue(String imagePath) {
		if (!imagePath.startsWith("/") && imagePath.indexOf(':') < 0 && !ThemeImage.NONE_ICON.equals(imagePath)) {
			imagePath = "/" + imagePath;
		}
		return ThemeImage.internalDecode(imagePath);
	}

}
