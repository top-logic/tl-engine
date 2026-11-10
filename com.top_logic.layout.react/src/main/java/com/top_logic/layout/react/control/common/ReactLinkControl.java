/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import java.util.Set;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.state.LinkState;

/**
 * A read-only text shown as a link opening an address: a web page, an e-mail to write, a number to
 * call.
 *
 * <p>
 * Renders as the {@code TLLink} React component, which draws the link with the part
 * {@code FieldValue}, so that a theme or a component library draws it as it draws any read-only
 * value pointing somewhere. The address is sent as written; the {@link #setKind(LinkKind) kind}
 * decides the link the client makes of it, as a read-only text field of that kind does. Without an
 * address the text is shown without a link.
 * </p>
 *
 * <p>
 * The state is described by {@link LinkState}.
 * </p>
 */
public class ReactLinkControl extends ReactControl {

	private static final String REACT_MODULE = "TLLink";

	/**
	 * Creates a {@link ReactLinkControl} opening a web address in a window of its own.
	 *
	 * @param context
	 *        The context for ID allocation and SSE registration.
	 * @param text
	 *        The shown text, or {@code null} to show the address.
	 * @param address
	 *        The address the link opens, or {@code null} for a text without a link.
	 */
	public ReactLinkControl(ReactContext context, String text, String address) {
		super(context, null, REACT_MODULE);
		putState(LinkState.TEXT__PROP, nonNull(text));
		putState(LinkState.ADDRESS__PROP, nonNull(address));
	}

	/**
	 * The shown text, empty where the {@link #getAddress() address} is shown.
	 */
	public String getText() {
		return (String) getState(LinkState.TEXT__PROP);
	}

	/**
	 * The address the link opens, empty for a text without a link.
	 */
	public String getAddress() {
		return (String) getState(LinkState.ADDRESS__PROP);
	}

	/**
	 * Updates the shown text, the address and the tooltip, in one patch.
	 *
	 * @param text
	 *        The shown text, or {@code null} to show the address.
	 * @param address
	 *        The address the link opens, or {@code null} for a text without a link.
	 * @param tooltip
	 *        The tooltip, plain text, or {@code null} for none.
	 */
	public void setLink(String text, String address, String tooltip) {
		Object tx = beginUpdate();
		putState(LinkState.TEXT__PROP, nonNull(text));
		putState(LinkState.ADDRESS__PROP, nonNull(address));
		setTooltip(tooltip);
		commitUpdate(tx);
	}

	/**
	 * Sets what the address is.
	 *
	 * @param kind
	 *        A web address (the default), an e-mail address or a telephone number.
	 */
	public void setKind(LinkKind kind) {
		putState(LinkState.KIND__PROP, kind.getExternalName());
	}

	/**
	 * Sets where the link opens its address.
	 *
	 * @param newWindow
	 *        Whether the address opens in a window of its own (the default) instead of the window
	 *        of the page.
	 */
	public void setNewWindow(boolean newWindow) {
		putState(LinkState.SAME_WINDOW__PROP, Boolean.valueOf(!newWindow));
	}

	/**
	 * Whether the link opens its address in a window of its own.
	 */
	public boolean isNewWindow() {
		return !Boolean.TRUE.equals(getState(LinkState.SAME_WINDOW__PROP));
	}

	/**
	 * Sets the tooltip of the link.
	 *
	 * @param tooltip
	 *        The tooltip, plain text, or {@code null} for none.
	 */
	public void setTooltip(String tooltip) {
		putState(LinkState.TOOLTIP__PROP, tooltip == null || tooltip.isEmpty() ? null : tooltip);
	}

	/**
	 * Rendering-only state keys, omitted from the headless projection.
	 */
	@Override
	protected Set<String> scriptingPresentationKeys() {
		return presentationKeys(super.scriptingPresentationKeys(), LinkState.SAME_WINDOW__PROP,
			LinkState.TOOLTIP__PROP);
	}

	private static String nonNull(String text) {
		return text == null ? "" : text;
	}
}
