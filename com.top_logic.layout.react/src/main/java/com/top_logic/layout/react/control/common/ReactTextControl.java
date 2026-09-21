/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.TooltipContent;
import com.top_logic.layout.react.TooltipProvider;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ReactValueColor;

/**
 * A simple read-only control that displays a text value as a {@code <span>}.
 *
 * <p>
 * Renders as a {@code TLText} React component. Text longer than the available width is wrapped
 * onto several lines, or truncated on a single one, as {@link #setOverflow(TextOverflow)} says.
 * </p>
 *
 * <p>
 * How the text is drawn is stated as roles rather than as font and color values: a
 * {@link #setVariant(TextVariant) typographic role}, a {@link #setTone(TextTone) color role} and
 * the {@link #setAppearance(TextAppearance) shape} it takes. Each role is written as a class on the
 * rendered element and filled from the design tokens of the active theme.
 * </p>
 *
 * <p>
 * The caller is responsible for converting application objects to display strings, e.g. by using a
 * {@link com.top_logic.layout.LabelProvider}.
 * </p>
 */
public class ReactTextControl extends ReactControl implements TooltipProvider {

	private static final String TEXT = "text";

	/** @see #setOverflow(TextOverflow) */
	private static final String OVERFLOW = "overflow";

	/** @see #setVariant(TextVariant) */
	private static final String VARIANT = "variant";

	/** @see #setTone(TextTone) */
	private static final String TONE = "tone";

	/** @see #setAppearance(TextAppearance) */
	private static final String APPEARANCE = "appearance";

	private static final String ROLE = "role";

	private static final String HAS_TOOLTIP = "hasTooltip";

	/** Key expected by {@link #getTooltipContent(String)}. */
	private static final String TOOLTIP_KEY = "tooltip";

	private String _tooltipHtml;

	private String _tooltipCaption;

	private boolean _tooltipInteractive;

	/**
	 * Creates a {@link ReactTextControl} without an extra CSS class.
	 */
	public ReactTextControl(ReactContext context, String text) {
		this(context, text, null);
	}

	/**
	 * Creates a {@link ReactTextControl}.
	 *
	 * @param text
	 *        The text to display, or {@code null}.
	 * @param cssClass
	 *        Additional CSS class to append to the default {@code tlText} class, or {@code null}.
	 */
	public ReactTextControl(ReactContext context, String text, String cssClass) {
		super(context, null, "TLText");
		putState(TEXT, text != null ? text : "");
		setCssClass(cssClass);
	}

	/**
	 * The displayed text.
	 */
	public String getText() {
		return (String) getState(TEXT);
	}

	/**
	 * Updates the displayed text.
	 */
	public void setText(String text) {
		putState(TEXT, text != null ? text : "");
	}

	/**
	 * Updates the displayed text and the color it is displayed with, in one patch.
	 *
	 * @param text
	 *        The text to display, or {@code null}.
	 * @param cssColor
	 *        The CSS the color is applied with, or {@code null} for text without a color.
	 *
	 * @see ReactValueColor#cssColorOf(Object)
	 */
	public void setText(String text, String cssColor) {
		Object tx = beginUpdate();
		setText(text);
		setColor(cssColor);
		commitUpdate(tx);
	}

	/**
	 * Updates the color the text is displayed with.
	 *
	 * <p>
	 * Text with a color is displayed as a pill in that color, text without one as plain text.
	 * </p>
	 *
	 * @param cssColor
	 *        The CSS the color is applied with, or {@code null} for text without a color.
	 *
	 * @see ReactValueColor#cssColorOf(Object)
	 */
	public void setColor(String cssColor) {
		putState(ReactValueColor.COLOR, cssColor);
	}

	/**
	 * Sets how text longer than the available width is displayed.
	 *
	 * @param overflow
	 *        Whether the text wraps onto several lines or is truncated on a single one.
	 */
	public void setOverflow(TextOverflow overflow) {
		putState(OVERFLOW, overflow.getExternalName());
	}

	/**
	 * Sets the typographic role the text is displayed in.
	 *
	 * @param variant
	 *        What the text is for: running text, a heading, the name of a value, a remark.
	 */
	public void setVariant(TextVariant variant) {
		putState(VARIANT, variant.getExternalName());
	}

	/**
	 * Sets the color role the text is displayed in.
	 *
	 * @param tone
	 *        What the color of the text means: the color it is read in, a lesser weight, an
	 *        explanation, an outcome.
	 */
	public void setTone(TextTone tone) {
		putState(TONE, tone.getExternalName());
	}

	/**
	 * Sets the shape the text is drawn in.
	 *
	 * @param appearance
	 *        Plain text, or a pill in the color of the value or of the {@link #setTone(TextTone)
	 *        tone}.
	 */
	public void setAppearance(TextAppearance appearance) {
		putState(APPEARANCE, appearance.getExternalName());
	}

	/**
	 * Sets the ARIA role of the rendered {@code <span>}.
	 *
	 * <p>
	 * For text that carries meaning beyond being read where it stands - {@code "alert"} for a
	 * message that appears without the reader having gone looking for it, say, the way the React
	 * form field and panel mark their own error areas.
	 * </p>
	 *
	 * @param role
	 *        The ARIA role, or {@code null} for none.
	 */
	public void setRole(String role) {
		putState(ROLE, role);
	}

	/**
	 * Sets a rich tooltip shown on hover.
	 *
	 * @param html
	 *        Sanitized tooltip HTML, or {@code null} to clear.
	 * @param caption
	 *        Optional caption, or {@code null}.
	 * @param interactive
	 *        When {@code true}, the popover remains open while the pointer hovers over it so the
	 *        user can select and copy content.
	 */
	public void setTooltip(String html, String caption, boolean interactive) {
		_tooltipHtml = (html == null || html.isEmpty()) ? null : html;
		_tooltipCaption = caption;
		_tooltipInteractive = interactive;
		putState(HAS_TOOLTIP, _tooltipHtml != null);
	}

	@Override
	public TooltipContent getTooltipContent(String key) {
		if (!TOOLTIP_KEY.equals(key) || _tooltipHtml == null) {
			return null;
		}
		return new TooltipContent(_tooltipHtml, _tooltipCaption, _tooltipInteractive);
	}

	/**
	 * Rendering-only state keys, omitted from the headless projection.
	 */
	@Override
	protected java.util.Set<String> scriptingPresentationKeys() {
		return presentationKeys(super.scriptingPresentationKeys(), OVERFLOW, VARIANT, TONE, APPEARANCE,
			ReactValueColor.COLOR);
	}
}
