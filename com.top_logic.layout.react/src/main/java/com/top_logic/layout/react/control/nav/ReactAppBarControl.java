/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.nav;

import java.util.List;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ToolbarControl;

/**
 * A {@link ToolbarControl} that renders a top-level application bar via the {@code TLAppBar} React
 * component.
 *
 * <p>
 * Extends {@link ToolbarControl} so that command scopes can add toolbar buttons to the trailing
 * actions area. The {@code TLAppBar} React component reads actions from the {@code actions}
 * state key — which is aliased to the same list that {@link ToolbarControl} populates via
 * {@code toolbarButtons}.
 * </p>
 */
public class ReactAppBarControl extends ToolbarControl {

	private static final String REACT_MODULE = "TLAppBar";

	private static final String TITLE = "title";

	private static final String LEADING = "leading";

	private static final String ACTIONS = "actions";

	private static final String VARIANT = "variant";

	private static final String CHILDREN = "children";

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

	private final List<ReactControl> _children;

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
	 *        Initial trailing action controls (may be empty).
	 * @param children
	 *        Inline children rendered between the title and the actions area (e.g. a
	 *        {@code <slot>} placeholder for content projected by descendant views).
	 */
	public ReactAppBarControl(ReactContext context, String title, AppBarVariant variant,
			ReactControl leading, List<? extends ReactControl> actions,
			List<? extends ReactControl> children) {
		super(context, null, REACT_MODULE);
		_leading = leading;
		_children = new java.util.ArrayList<>(children);
		setTitle(title);
		putState(VARIANT, variant.getExternalName());
		if (leading != null) {
			putState(LEADING, leading);
		}

		// Alias the toolbarButtons list under "actions" so TLAppBar reads the same list.
		putState(ACTIONS, getState(TOOLBAR_BUTTONS));

		putState(CHILDREN, _children);

		for (ReactControl action : actions) {
			addToolbarButton(action);
		}
	}

	/**
	 * Creates an app bar without inline children.
	 */
	public ReactAppBarControl(ReactContext context, String title, AppBarVariant variant,
			ReactControl leading, List<? extends ReactControl> actions) {
		this(context, title, variant, leading, actions, List.of());
	}

	/**
	 * Creates a flat app bar with no leading control.
	 *
	 * @param title
	 *        The bar title.
	 * @param actions
	 *        Initial trailing action controls.
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
		cleanupToolbarButtons();
		for (ReactControl child : _children) {
			child.cleanupTree();
		}
	}

	@Override
	protected void propagateAttach() {
		super.propagateAttach();
		for (ReactControl child : _children) {
			child.attach();
		}
	}

	@Override
	protected void propagateDetach() {
		super.propagateDetach();
		for (ReactControl child : _children) {
			child.detach();
		}
	}

	@Override
	public void addToolbarButton(ReactControl button) {
		super.addToolbarButton(button);
		putState(ACTIONS, getState(TOOLBAR_BUTTONS));
	}

	@Override
	public boolean removeToolbarButton(ReactControl button) {
		boolean removed = super.removeToolbarButton(button);
		if (removed) {
			putState(ACTIONS, getState(TOOLBAR_BUTTONS));
		}
		return removed;
	}

}
