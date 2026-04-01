/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.List;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.react.control.ReactCompositeControl;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A {@link ReactCompositeControl} that wraps a single child control at a view file boundary and
 * rebuilds its subtree when the corresponding view file changes.
 *
 * <p>
 * Implements {@link ViewReloadListener} so that it can be notified by
 * {@link ViewContext#fireViewChanged(Set)} after the View Designer saves modified files. When its
 * view path is among the changed paths, the old child tree is cleaned up and a fresh control tree is
 * built from the re-parsed view file.
 * </p>
 *
 * <p>
 * Uses the {@code "TLStack"} React module, which simply renders its children without adding any
 * wrapper markup. This makes the reload boundary invisible in the DOM.
 * </p>
 *
 * @see ReferenceElement
 * @see ViewServlet
 */
public class ReloadableControl extends ReactCompositeControl implements ViewReloadListener {

	private final String _viewPath;

	private final ViewContext _viewContext;

	/**
	 * Creates a new {@link ReloadableControl}.
	 *
	 * @param viewPath
	 *        The full path to the view file (e.g. {@code /WEB-INF/views/app.view.xml}).
	 * @param viewContext
	 *        The {@link ViewContext} used to create the child control tree.
	 * @param child
	 *        The initial child control built from the view.
	 */
	public ReloadableControl(String viewPath, ViewContext viewContext, ReactControl child) {
		super(viewContext, null, "TLStack", List.of(child));
		_viewPath = viewPath;
		_viewContext = viewContext;
		viewContext.addViewReloadListener(this);
		addCleanupAction(() -> _viewContext.removeViewReloadListener(this));
	}

	@Override
	public void viewChanged(Set<String> changedPaths) {
		if (!changedPaths.contains(_viewPath)) {
			return;
		}

		// Clean up old children.
		cleanupChildren();

		// Re-load the view from disk (ViewLoader checks timestamps and re-parses).
		ViewElement freshView;
		try {
			freshView = ViewLoader.loadView(_viewPath);
		} catch (ConfigurationException ex) {
			Logger.error("Failed to reload view: " + _viewPath, ex, ReloadableControl.class);
			return;
		}

		// Build a new control tree using the existing ViewContext.
		ReactControl newChild = (ReactControl) freshView.createControl(_viewContext);
		replaceChildren(List.of(newChild));
	}
}
