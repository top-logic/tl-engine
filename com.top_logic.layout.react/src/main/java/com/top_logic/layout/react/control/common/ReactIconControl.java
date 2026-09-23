/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A read-only control that displays a single {@link ThemeImage} as a {@code <span>}.
 *
 * <p>
 * Renders as a {@code TLIcon} React component, which draws a font icon or a picture resource
 * according to the encoded form of the image. An icon without a tooltip decorates what it sits
 * beside; one with a tooltip carries that text as its name.
 * </p>
 */
public class ReactIconControl extends ReactControl {

	private static final String REACT_MODULE = "TLIcon";

	/** State key holding the encoded form of the displayed image. */
	private static final String IMAGE = "image";

	/** State key holding the text shown on hover, which also names the icon. */
	private static final String TOOLTIP = "tooltip";

	/** State key holding the CSS classes appended to the default {@code tlIcon} class. */
	private static final String CSS_CLASSES = "cssClasses";

	/**
	 * Creates a {@link ReactIconControl}.
	 *
	 * @param image
	 *        The image to display, or {@code null} to display none.
	 */
	public ReactIconControl(ReactContext context, ThemeImage image) {
		super(context, null, REACT_MODULE);
		putImageState(image);
	}

	/**
	 * Updates the displayed image.
	 *
	 * @param image
	 *        The image to display, or {@code null} to display none.
	 */
	public void setImage(ThemeImage image) {
		putImageState(image);
	}

	private void putImageState(ThemeImage image) {
		putState(IMAGE, image == null ? null : image.resolve().toEncodedForm());
	}

	/**
	 * Updates the text shown on hover.
	 *
	 * @param tooltip
	 *        The text, or {@code null} for an icon that decorates and is not announced.
	 */
	public void setTooltip(String tooltip) {
		putState(TOOLTIP, (tooltip == null || tooltip.isEmpty()) ? null : tooltip);
	}

	/**
	 * Updates the additional CSS classes.
	 *
	 * @param cssClasses
	 *        The classes to append, or {@code null} to clear.
	 */
	public void setCssClasses(String cssClasses) {
		putState(CSS_CLASSES, cssClasses);
	}
}
