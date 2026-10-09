/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.nav;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactToolbarControl;
import com.top_logic.layout.react.state.AppBarState;

/**
 * A {@link ReactControl} that renders a top-level application bar via the {@code TLAppBar} React
 * component.
 *
 * <p>
 * The state is described by {@link AppBarState}.
 * </p>
 *
 * <p>
 * The commands form one toolbar, so those that do not fit the bar fold into its overflow menu
 * rather than pushing the bar's content out of the window.
 * </p>
 */
public class ReactAppBarControl extends ReactControl {

	private static final String REACT_MODULE = "TLAppBar";

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
	 *        The toolbar of the commands placed in the bar; empty while there are none, and
	 *        filled in place as commands come and go.
	 * @param children
	 *        Inline children rendered between the title and the actions area (e.g. a
	 *        {@code <slot>} placeholder for content projected by descendant views).
	 * @param trailing
	 *        Optional control closing the bar, right of the actions area, or {@code null}.
	 */
	public ReactAppBarControl(ReactContext context, String title, AppBarVariant variant,
			ReactControl leading, ReactToolbarControl actions,
			List<? extends ReactControl> children, ReactControl trailing) {
		super(context, null, REACT_MODULE);
		setTitle(title);
		putState(AppBarState.VARIANT__PROP, variant.getExternalName());
		if (leading != null) {
			putState(AppBarState.LEADING__PROP, leading);
		}
		if (trailing != null) {
			putState(AppBarState.TRAILING__PROP, trailing);
		}
		putState(AppBarState.ACTIONS__PROP, actions);
		putState(AppBarState.CHILDREN__PROP, new ArrayList<>(children));
	}

	/**
	 * Updates the bar title.
	 */
	public void setTitle(String title) {
		putState(AppBarState.TITLE__PROP, title);
	}

	/**
	 * Rendering-only state keys, omitted from the headless projection.
	 */
	@Override
	protected Set<String> scriptingPresentationKeys() {
		return presentationKeys(super.scriptingPresentationKeys(), AppBarState.VARIANT__PROP);
	}
}
