/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A {@link ReactControl} that displays a person or object as a circular initials avatar via the
 * {@code TLAvatar} React component.
 *
 * <p>
 * The client derives the initials and a stable background color from the display name.
 * </p>
 */
public class ReactAvatarControl extends ReactControl {

	private static final String REACT_MODULE = "TLAvatar";

	/** State key for the display name the avatar represents, or {@code null} for no value. */
	private static final String NAME = "name";

	/**
	 * Creates a new {@link ReactAvatarControl}.
	 *
	 * @param context
	 *        The {@link ReactContext} for ID allocation and SSE registration.
	 * @param name
	 *        The initial display name, or {@code null} for no value.
	 */
	public ReactAvatarControl(ReactContext context, String name) {
		super(context, null, REACT_MODULE);
		setName(name);
	}

	/**
	 * Updates the displayed name.
	 *
	 * @param name
	 *        The new display name, or {@code null} to display nothing.
	 */
	public void setName(String name) {
		putState(NAME, name);
	}

}
