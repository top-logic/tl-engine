/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.nav;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.react.ReactControl;

/**
 * A {@link ReactControl} that renders a top-level application bar via the {@code TLAppBar} React
 * component.
 *
 * <p>
 * The app bar provides a Material Design top app bar pattern with a leading slot (back/menu
 * button), a title, and trailing action icons.
 * </p>
 *
 * <p>
 * State:
 * </p>
 * <ul>
 * <li>{@code title} - the bar title</li>
 * <li>{@code leading} - optional leading control (e.g. hamburger button)</li>
 * <li>{@code actions} - trailing action controls</li>
 * <li>{@code variant} - "flat" or "elevated"</li>
 * <li>{@code color} - "primary" or "surface"</li>
 * </ul>
 */
public class ReactAppBarControl extends ReactControl {

	private static final String REACT_MODULE = "TLAppBar";

	private static final String TITLE = "title";

	private static final String LEADING = "leading";

	private static final String ACTIONS = "actions";

	private static final String VARIANT = "variant";

	private static final String COLOR = "color";

	private ReactControl _leading;

	private final List<ReactControl> _actions;

	/**
	 * Creates an app bar with full configuration.
	 *
	 * @param title
	 *        The bar title.
	 * @param variant
	 *        "flat" or "elevated".
	 * @param color
	 *        "primary" or "surface".
	 * @param leading
	 *        Optional leading control, or {@code null}.
	 * @param actions
	 *        Trailing action controls.
	 */
	public ReactAppBarControl(String title, String variant, String color,
			ReactControl leading, List<? extends ReactControl> actions) {
		super(null, REACT_MODULE);
		_leading = leading;
		_actions = new ArrayList<>(actions);
		putState(TITLE, title);
		putState(VARIANT, variant);
		putState(COLOR, color);
		if (leading != null) {
			putState(LEADING, leading);
		}
		putState(ACTIONS, _actions);
	}

	/**
	 * Creates a primary flat app bar with no leading control.
	 *
	 * @param title
	 *        The bar title.
	 * @param actions
	 *        Trailing action controls.
	 */
	public ReactAppBarControl(String title, List<? extends ReactControl> actions) {
		this(title, "flat", "primary", null, actions);
	}

	/**
	 * Updates the bar title.
	 *
	 * @param title
	 *        The new title.
	 */
	public void setTitle(String title) {
		putState(TITLE, title);
	}

	@Override
	protected void cleanupChildren() {
		if (_leading != null) {
			_leading.cleanupTree();
		}
		for (ReactControl action : _actions) {
			action.cleanupTree();
		}
	}

}
