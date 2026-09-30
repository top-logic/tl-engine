/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A {@link ReactControl} rendering a link that scrolls the browser to the {@link AnchorControl
 * anchor} of a model object, via the {@code TLScrollLink} React component.
 *
 * <p>
 * The link is hidden while its target is {@code null}. Clicking it is a purely client-side viewport
 * action: the component locates the element carrying the matching {@code data-tl-anchor} attribute
 * and scrolls it into view. The target key is derived through {@link AnchorControl#anchorId(Object)},
 * the same helper the anchor uses.
 * </p>
 */
public class ScrollLinkControl extends ReactControl {

	private static final String REACT_MODULE = "TLScrollLink";

	/** State key for the anchor id to scroll to, or {@code null} to hide the link. */
	private static final String TARGET = "target";

	/** State key for the link text. */
	private static final String LABEL = "label";

	/**
	 * Creates a new {@link ScrollLinkControl}.
	 *
	 * @param context
	 *        The {@link ReactContext} for ID allocation and SSE registration.
	 * @param target
	 *        The object to scroll to, or {@code null} to hide the link.
	 * @param label
	 *        The link text.
	 */
	public ScrollLinkControl(ReactContext context, Object target, String label) {
		super(context, null, REACT_MODULE);
		setValue(target, label);
	}

	/**
	 * Updates the scroll target and link text.
	 *
	 * @param target
	 *        The object to scroll to, or {@code null} to hide the link.
	 * @param label
	 *        The link text.
	 */
	public void setValue(Object target, String label) {
		putState(TARGET, AnchorControl.anchorId(target));
		putState(LABEL, label);
	}

}
