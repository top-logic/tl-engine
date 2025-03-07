/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <code>info@top-logic.com</code>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.param;

/**
 * The alignment value indicates whether to force uniform scaling and, if so, the alignment method
 * to use in case the aspect ratio of the viewBox doesn't match the aspect ratio of the viewport.
 * xMidYMid is the default value.
 * 
 * @see ImageScale
 */
public enum ImageAlign {

	/**
	 * Forces uniform scaling. Align the midpoint X value of the element's viewBox with the midpoint
	 * X value of the viewport. Align the midpoint Y value of the element's viewBox with the
	 * midpoint Y value of the viewport. This is the default value.
	 */
	xMidYMid,

	/**
	 * Forces uniform scaling. Align the <code>min-x</code> of the element's viewBox with the
	 * smallest X value of the viewport. Align the <code>min-y</code> of the element's viewBox with
	 * the smallest Y value of the viewport.
	 */
	xMinYMin,

	/**
	 * Forces uniform scaling. Align the midpoint X value of the element's viewBox with the midpoint
	 * X value of the viewport. Align the <code>min-y</code> of the element's viewBox with the
	 * smallest Y value of the viewport.
	 */
	xMidYMin,

	/**
	 * Forces uniform scaling. Align the <code>min-x</code>+<code>width</code> of the element's
	 * viewBox with the maximum X value of the viewport. Align the <code>min-y</code> of the
	 * element's viewBox with the smallest Y value of the viewport.
	 */
	xMaxYMin,

	/**
	 * Forces uniform scaling. Align the <code>min-x</code> of the element's viewBox with the
	 * smallest X value of the viewport. Align the midpoint Y value of the element's viewBox with
	 * the midpoint Y value of the viewport.
	 */
	xMinYMid,

	/**
	 * Forces uniform scaling. Align the <code>min-x</code>+<code>width</code> of the element's
	 * viewBox with the maximum X value of the viewport. Align the midpoint Y value of the element's
	 * viewBox with the midpoint Y value of the viewport.
	 */
	xMaxYMid,

	/**
	 * Forces uniform scaling. Align the <code>min-x</code> of the element's viewBox with the
	 * smallest X value of the viewport. Align the <code>min-y</code>+<code>height</code> of the
	 * element's viewBox with the maximum Y value of the viewport.
	 */
	xMinYMax,

	/**
	 * Forces uniform scaling. Align the midpoint X value of the element's viewBox with the midpoint
	 * X value of the viewport. Align the <code>min-y</code>+<code>height</code> of the element's
	 * viewBox with the maximum Y value of the viewport.
	 */
	xMidYMax,

	/**
	 * Forces uniform scaling. Align the <code>min-x</code>+<code>width</code> of the element's
	 * viewBox with the maximum X value of the viewport. Align the
	 * <code>min-y</code>+<code>height</code> of the element's viewBox with the maximum Y value of
	 * the viewport.
	 */
	xMaxYMax,

	/**
	 * Does not force uniform scaling. Scale the graphic content of the given element non-uniformly
	 * if necessary such that the element's bounding box exactly matches the viewport rectangle.
	 * Note that if <code>align</code> is none, then the optional <code>meetOrSlice</code> value is
	 * ignored.
	 */
	none,

}
