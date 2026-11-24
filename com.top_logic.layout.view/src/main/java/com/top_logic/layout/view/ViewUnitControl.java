/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.react.control.ReactCompositeControl;
import com.top_logic.layout.react.control.ReactControl;

/**
 * The boundary of a unit of the view: a {@link ReactCompositeControl} that builds its single child
 * from a {@link Content} and shows a placeholder in its place when building fails.
 *
 * <p>
 * Units are the parts of a view that fail on their own: a referenced view file, the content of a
 * navigation item, of a tab, of a window or dialog. A {@link RuntimeException} thrown while the
 * content is built is logged and replaces only this unit with a placeholder (see
 * {@link ViewLoadError#createBuildErrorControl}), so the enclosing application keeps rendering. The
 * placeholder offers to build the unit again ({@link #rebuild()}). {@link Error}s are not caught.
 * </p>
 *
 * <p>
 * A content built on demand - when a tab is first shown, or anew after a channel changed - is built
 * through {@link #create(ViewContext, Content)}: a content that builds without failure is used as it
 * is, so the boundary adds nothing to the display of a working view; only a failing one is replaced
 * by a {@link ViewUnitControl} showing the placeholder.
 * </p>
 *
 * <p>
 * A unit showing a view file (see {@link #forView(ViewContext, String, ViewElement)}) also
 * implements {@link ViewReloadListener}: it is notified by {@link ViewContext#fireViewChanged(Set)}
 * after the View Designer saves modified files, and rebuilds itself from the re-parsed file when
 * its view path is among the changed paths. A view file that can no longer be loaded is shown as
 * the {@link ViewLoadError#createControl load error placeholder}.
 * </p>
 *
 * <p>
 * Uses the {@code "TLStack"} React module, which simply renders its children without adding any
 * wrapper markup. This makes the boundary invisible in the DOM.
 * </p>
 *
 * @see ReferenceElement
 * @see ViewServlet
 */
public class ViewUnitControl extends ReactCompositeControl implements ViewReloadListener {

	/**
	 * Builds the content of a {@link ViewUnitControl}.
	 */
	@FunctionalInterface
	public interface Content {

		/**
		 * Creates the control tree of the unit.
		 *
		 * @param context
		 *        The context of the unit.
		 * @return The control to display in the unit.
		 * @throws ConfigurationException
		 *         If the view the content is built from cannot be loaded.
		 */
		ReactControl build(ViewContext context) throws ConfigurationException;
	}

	private final String _viewPath;

	private final ViewContext _viewContext;

	private final Content _content;

	/**
	 * Creates a {@link ViewUnitControl} that is not backed by a view file.
	 *
	 * @param viewContext
	 *        The {@link ViewContext} the content is built in.
	 * @param content
	 *        Builds the content, initially and on every {@link #rebuild()}.
	 */
	public ViewUnitControl(ViewContext viewContext, Content content) {
		this(viewContext, null, content);
	}

	/**
	 * Creates a {@link ViewUnitControl}.
	 *
	 * @param viewContext
	 *        The {@link ViewContext} the content is built in.
	 * @param viewPath
	 *        The full path to the view file the content shows (e.g.
	 *        {@code /WEB-INF/views/app.view.xml}): the unit is rebuilt when the file changes.
	 *        {@code null} for a content not backed by a view file.
	 * @param content
	 *        Builds the content, initially and on every {@link #rebuild()}.
	 */
	public ViewUnitControl(ViewContext viewContext, String viewPath, Content content) {
		this(viewContext, viewPath, content, null);
	}

	/**
	 * Creates a {@link ViewUnitControl} whose first build may already have failed.
	 *
	 * @param failure
	 *        The failure of the first build, {@code null} to build the content now.
	 */
	private ViewUnitControl(ViewContext viewContext, String viewPath, Content content, Exception failure) {
		super(viewContext, null, "TLStack");
		_viewPath = viewPath;
		_viewContext = viewContext;
		_content = content;
		if (viewPath != null) {
			viewContext.addViewReloadListener(this);
			addCleanupAction(() -> _viewContext.removeViewReloadListener(this));
		}
		addChild(failure == null ? build() : placeholder(failure));
	}

	/**
	 * Builds a content on demand, within a unit boundary.
	 *
	 * @param viewContext
	 *        The {@link ViewContext} the content is built in.
	 * @param content
	 *        Builds the content.
	 * @return The built content itself, or a {@link ViewUnitControl} showing the placeholder if
	 *         building failed. The placeholder builds the content again on request.
	 */
	public static ReactControl create(ViewContext viewContext, Content content) {
		return create(viewContext, null, content);
	}

	/**
	 * Builds a content on demand, within a unit boundary.
	 *
	 * @param viewContext
	 *        The {@link ViewContext} the content is built in.
	 * @param viewPath
	 *        The full path to the view file the content shows, {@code null} for a content not
	 *        backed by a view file. A placeholder shown for the content is rebuilt when the file
	 *        changes.
	 * @param content
	 *        Builds the content.
	 * @return The built content itself, or a {@link ViewUnitControl} showing the placeholder if
	 *         building failed. The placeholder builds the content again on request.
	 */
	public static ReactControl create(ViewContext viewContext, String viewPath, Content content) {
		try {
			return content.build(viewContext);
		} catch (ConfigurationException | RuntimeException ex) {
			return new ViewUnitControl(viewContext, viewPath, content, ex);
		}
	}

	/**
	 * Creates a {@link ViewUnitControl} showing a view file.
	 *
	 * @param viewContext
	 *        The {@link ViewContext} the view is built in.
	 * @param viewPath
	 *        The full path to the view file (e.g. {@code /WEB-INF/views/app.view.xml}).
	 * @param view
	 *        The view loaded from the file, built initially. A {@link #rebuild()} parses the file
	 *        anew.
	 * @return The unit displaying the view.
	 */
	public static ViewUnitControl forView(ViewContext viewContext, String viewPath, ViewElement view) {
		return new ViewUnitControl(viewContext, viewPath, new ViewFileContent(viewPath, view));
	}

	@Override
	public void viewChanged(Set<String> changedPaths) {
		if (!changedPaths.contains(_viewPath)) {
			return;
		}
		Logger.debug(() -> "Reloading view " + _viewPath + ".", ViewUnitControl.class);
		rebuild();
	}

	/**
	 * Disposes the displayed control tree and builds the content anew.
	 *
	 * <p>
	 * Used both when the view file of the unit changed and when the user asks to try again after
	 * building failed.
	 * </p>
	 */
	public void rebuild() {
		cleanupChildren();
		replaceChildren(List.of(build()));
	}

	/**
	 * Builds the content, or the placeholder reporting why that failed.
	 */
	private ReactControl build() {
		try {
			return _content.build(_viewContext);
		} catch (ConfigurationException | RuntimeException ex) {
			return placeholder(ex);
		}
	}

	/**
	 * Logs the given failure of building the content and creates the placeholder reporting it.
	 *
	 * @param failure
	 *        A {@link ConfigurationException} for a view that could not be loaded, a
	 *        {@link RuntimeException} for a content that could not be built.
	 */
	private ReactControl placeholder(Exception failure) {
		if (failure instanceof ConfigurationException) {
			Logger.error("Rendering a placeholder for the view that could not be loaded: " + _viewPath, failure,
				ViewUnitControl.class);
			return ViewLoadError.createControl(_viewContext, _viewPath, failure);
		}
		Logger.error("Rendering a placeholder for the view unit that could not be built"
			+ (_viewPath == null ? "." : ": " + _viewPath), failure, ViewUnitControl.class);
		return ViewLoadError.createBuildErrorControl(_viewContext, _viewPath, new Date(), failure,
			context -> rebuild());
	}

	/**
	 * Structural: this control is an invisible unit boundary and is elided from the headless
	 * projection.
	 */
	@Override
	public boolean scriptingTransparent() {
		return true;
	}

	/**
	 * {@link Content} of a view file: builds the view loaded in advance first, and parses the file
	 * anew for every later build.
	 */
	private static final class ViewFileContent implements Content {

		private final String _path;

		private ViewElement _initial;

		ViewFileContent(String path, ViewElement initial) {
			_path = path;
			_initial = initial;
		}

		@Override
		public ReactControl build(ViewContext context) throws ConfigurationException {
			ViewElement view = _initial;
			if (view == null) {
				view = ViewLoader.loadView(_path);
			} else {
				_initial = null;
			}
			return (ReactControl) view.createControl(context);
		}
	}
}
