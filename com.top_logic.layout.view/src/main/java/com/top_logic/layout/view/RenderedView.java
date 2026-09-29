/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.react.window.WindowEntry;

/**
 * Which view a browser tab currently renders, so that a reload can tell whether the tree the tab
 * still holds may be rendered again instead of being replaced.
 *
 * <p>
 * A {@link com.top_logic.layout.react.control.ReactControl} tolerates being rendered more than once,
 * and the tree itself lives in the tab's {@link WindowEntry} together with the window's update queue
 * and model scope. What a rebuild would throw away is everything the tree holds that is neither in a
 * channel nor in the personalization: the scroll position and expansion of a table, the input of a
 * form, the position of a pager.
 * </p>
 *
 * <p>
 * Reusing it is only correct for the same view, which is what this record establishes: a different
 * view path, or a view file that {@link ViewLoader} has re-read after an edit, yields a different
 * {@link ViewElement} instance and hence a rebuild. Kept in the browser tab's
 * {@link SubSessionContext}, which dies together with the window registry when
 * {@link com.top_logic.layout.view.login.PendingSessionAction a login or logout} replaces the
 * session - so a tree is never reused for a different user.
 * </p>
 *
 * @param viewPath
 *        The path of the rendered {@code .view.xml}.
 * @param source
 *        The {@link ViewElement} the tree was built from, compared by identity to detect a view file
 *        that has been edited in the meantime.
 */
public record RenderedView(String viewPath, ViewElement source) {

	private static final Property<RenderedView> CURRENT =
		TypedAnnotatable.property(RenderedView.class, "renderedView");

	/**
	 * What the given browser tab renders, or {@code null} if it renders nothing yet.
	 */
	public static RenderedView lookup(SubSessionContext subSession) {
		return subSession == null ? null : subSession.get(CURRENT);
	}

	/**
	 * Remembers what the browser tab renders, replacing any previous entry.
	 *
	 * <p>
	 * Does nothing when there is no sub-session to remember it in; the tree is then simply rebuilt on
	 * the next request.
	 * </p>
	 */
	public static void store(SubSessionContext subSession, RenderedView view) {
		if (subSession != null) {
			subSession.set(CURRENT, view);
		}
	}

	/**
	 * Whether a tree built for this view can be rendered again for the given request.
	 *
	 * @param requestedPath
	 *        The requested view path.
	 * @param loaded
	 *        The {@link ViewElement} currently loaded for that path.
	 */
	public boolean matches(String requestedPath, ViewElement loaded) {
		return viewPath.equals(requestedPath) && source == loaded;
	}

}
