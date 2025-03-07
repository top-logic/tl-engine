/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.param;

/**
 * Controls how to scale an image after alignment.
 * 
 * @see ImageAlign
 */
public enum ImageScale {

	/**
	 * Scales the graphic such that:
	 * <ul>
	 * <li>The aspect ratio is preserved.</li>
	 * <li>The entire viewBox is visible within the viewport.</li>
	 * <li>The viewBox is scaled up as much as possible, while still meeting the other
	 * criteria.</li>
	 * </ul>
	 * 
	 * <p>
	 * In this case, if the aspect ratio of the graphic does not match the viewport, some of the
	 * viewport will extend beyond the bounds of the viewBox (i.e., the area into which the viewBox
	 * will draw will be smaller than the viewport). meet
	 * </p>
	 */
	meet,

	/**
	 * Scales the graphic such that:
	 * 
	 * <ul>
	 * <li>The aspect ratio is preserved.</li>
	 * <li>The entire viewport is covered by the viewBox.</li>
	 * <li>The viewBox is scaled down as much as possible, while still meeting the other
	 * criteria.</li>
	 * </ul>
	 * 
	 * <p>
	 * In this case, if the aspect ratio of the viewBox does not match the viewport, some of the
	 * viewBox will extend beyond the bounds of the viewport (i.e., the area into which the viewBox
	 * will draw is larger than the viewport). slice
	 * </p>
	 */
	slice,

}
