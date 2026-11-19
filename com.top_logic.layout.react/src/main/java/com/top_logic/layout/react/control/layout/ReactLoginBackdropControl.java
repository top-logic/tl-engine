/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.Set;

import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.ReactImages;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A {@link ReactControl} that places its content in front of a halftone backdrop, through the
 * {@code TLLoginBackdrop} React component.
 *
 * <p>
 * The component always fills its container. It draws the backdrop on a canvas and shows the content
 * control in a column above it; an optional logo stands on a surface of its own above the content.
 * </p>
 */
public class ReactLoginBackdropControl extends ReactControl {

	private static final String REACT_MODULE = "TLLoginBackdrop";

	/** @see #ReactLoginBackdropControl(ReactContext, ReactControl) */
	public static final String CHILD = "child";

	/** @see #setLogo(String) */
	public static final String LOGO = "logo";

	/** @see #setLogoDark(String) */
	public static final String LOGO_DARK = "logoDark";

	/** @see #setLogoAlt(String) */
	public static final String LOGO_ALT = "logoAlt";

	/** State key for the name of the application. */
	public static final String APP_TITLE = "appTitle";

	private final ReactControl _child;

	/**
	 * Creates a new {@link ReactLoginBackdropControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param child
	 *        The content control shown in front of the backdrop.
	 */
	public ReactLoginBackdropControl(ReactContext context, ReactControl child) {
		super(context, null, REACT_MODULE);
		_child = child;
		putState(CHILD, child);
	}

	/**
	 * Sets the logo shown in light mode, and in dark mode where there is no
	 * {@link #setLogoDark(String) dark logo}.
	 *
	 * @param image
	 *        The {@link ReactImages#encode(ReactContext, ThemeImage) encoded form} of the logo, or
	 *        {@code null} to show no logo.
	 */
	public void setLogo(String image) {
		putState(LOGO, emptyToNull(image));
	}

	/**
	 * Sets the logo shown in dark mode.
	 *
	 * @param image
	 *        The {@link ReactImages#encode(ReactContext, ThemeImage) encoded form} of the logo, or
	 *        {@code null} to show the {@link #setLogo(String) logo} in both modes.
	 */
	public void setLogoDark(String image) {
		putState(LOGO_DARK, emptyToNull(image));
	}

	/**
	 * Sets what the logo shows, for a reader who cannot see it.
	 *
	 * @param alt
	 *        The description, or {@code null} to let the logo count as decoration, which is not
	 *        announced.
	 */
	public void setLogoAlt(String alt) {
		putState(LOGO_ALT, emptyToNull(alt));
	}

	/**
	 * Sets the name of the application, shown above the content where there is no
	 * {@link #setLogo(String) logo}, and describing a logo without a {@link #setLogoAlt(String)
	 * description of its own}.
	 *
	 * @param title
	 *        The name, or {@code null} for an application without one.
	 */
	public void setAppTitle(String title) {
		putState(APP_TITLE, emptyToNull(title));
	}

	private static String emptyToNull(String value) {
		return (value == null || value.isEmpty()) ? null : value;
	}

	/**
	 * Structural: this control only provides the backdrop around its content and is elided from the
	 * headless projection.
	 */
	@Override
	public boolean scriptingTransparent() {
		return true;
	}

	@Override
	protected Set<String> scriptingPresentationKeys() {
		return presentationKeys(super.scriptingPresentationKeys(), LOGO, LOGO_DARK, LOGO_ALT);
	}
}
