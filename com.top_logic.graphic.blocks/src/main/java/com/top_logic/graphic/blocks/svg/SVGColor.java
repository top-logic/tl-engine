/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.svg;

/**
 * A color value.
 * 
 * @implNote The color implementation for Java AWT cannot be used, since it is not compatible with
 *           GWT.
 */
public class SVGColor {

	private final int _value;

	/**
	 * Creates a {@link SVGColor}.
	 */
	public SVGColor(int value) {
		_value = value;
	}

	/**
	 * The red color component.
	 */
	public int getRed() {
		return (_value & 0xFF0000) >>> 16;
	}

	/**
	 * The green color component.
	 */
	public int getGreen() {
		return (_value & 0x00FF00) >>> 8;
	}

	/**
	 * The blue color component.
	 */
	public int getBlue() {
		return (_value & 0x0000FF) >>> 0;
	}

	/**
	 * The alpha channel.
	 */
	public int getAlpha() {
		return (_value & 0xFF000000) >>> 24;
	}

}
