/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.svg;

/**
 * Description of the visual dimensions of a printed line of text.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TextMetrics {

	/**
	 * The width of the measured text in user units.
	 */
	double getWidth();

	/**
	 * The height of the measured text in user units.
	 */
	double getHeight();

	/**
	 * Offset from the to of the text's bounding box to the text's baseline.
	 */
	double getBaseLine();

	/**
	 * Adds the given padding to this {@link TextMetrics}.
	 */
	default TextMetrics padding(double padding) {
		return padding(padding, padding, padding, padding);
	}

	/**
	 * Adds the given padding to this {@link TextMetrics}.
	 */
	default TextMetrics padding(double top, double left, double bottom, double right) {
		return new TextMetricsImpl(getWidth() + left + right, getHeight() + top + bottom, getBaseLine() + top);
	}

}
