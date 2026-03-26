/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.nav;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A {@link ReactControl} that renders a top-level application bar via the {@code TLAppBar} React
 * component.
 */
public class ReactAppBarControl extends ReactControl {

	private static final String REACT_MODULE = "TLAppBar";

	private static final String TITLE = "title";

	private static final String LEADING = "leading";

	private static final String ACTIONS = "actions";

	private static final String VARIANT = "variant";

	/**
	 * Visual variant of the app bar.
	 */
	public enum AppBarVariant implements ExternallyNamed {

		/** Flat bar without elevation. */
		FLAT("flat"),

		/** Elevated bar with shadow. */
		ELEVATED("elevated");

		private final String _externalName;

		AppBarVariant(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	private ReactControl _leading;

	private final List<ReactControl> _actions;

	/**
	 * Creates an app bar with full configuration.
	 *
	 * @param title
	 *        The bar title.
	 * @param variant
	 *        The visual variant.
	 * @param leading
	 *        Optional leading control, or {@code null}.
	 * @param actions
	 *        Trailing action controls.
	 */
	public ReactAppBarControl(ReactContext context, String title, AppBarVariant variant,
			ReactControl leading, List<? extends ReactControl> actions) {
		super(context, null, REACT_MODULE);
		_leading = leading;
		_actions = new ArrayList<>(actions);
		setTitle(title);
		putState(VARIANT, variant.getExternalName());
		if (leading != null) {
			putState(LEADING, leading);
		}
		putState(ACTIONS, _actions);
	}

	/**
	 * Creates a flat app bar with no leading control.
	 *
	 * @param title
	 *        The bar title.
	 * @param actions
	 *        Trailing action controls.
	 */
	public ReactAppBarControl(ReactContext context, String title, List<? extends ReactControl> actions) {
		this(context, title, AppBarVariant.FLAT, null, actions);
	}

	/**
	 * Updates the bar title.
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
