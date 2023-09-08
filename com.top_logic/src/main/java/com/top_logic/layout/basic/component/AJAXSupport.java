/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.component;



import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * @author    <a href=mailto:kha@top-logic.com>bhu</a>
 */
public interface AJAXSupport {

	/**
	 * Invoked from {@link LayoutComponent#beforeRendering(DisplayContext)} to indicate that a
	 * complete repaint follows.
	 */
	void startRendering();
	
	/**
	 * Check, whether incremental updates are available that bring the view in
	 * sync with the server model.
	 */
	boolean isRevalidateRequested();

	/**
	 * Implementations must add all
	 * {@link com.top_logic.base.services.simpleajax.ClientAction}s required to
	 * bring their client-side view in sync with their server model to the given
	 * list of actions.
	 * 
	 * <p>
	 * Called from {@link com.top_logic.mig.html.layout.RevalidationVisitor}
	 * during its visit of the component tree for each
	 * {@link LayoutComponent#isInvalid() invalid} {@link AJAXComponent}. After
	 * this method returns, {@link LayoutComponent#isInvalid()} must return
	 * <code>false</code> to indicate that the revalidation was successful. If
	 * {@link LayoutComponent#isInvalid()} still returns <code>true</code>,
	 * the component is being reloaded and all of its generated actions are
	 * dropped.
	 * </p>
	 * 
	 * @param context
	 *        The current {@link com.top_logic.layout.DisplayContext}.
	 * @param actions
	 *        List of
	 *        {@link com.top_logic.base.services.simpleajax.ClientAction}s that
	 *        must be appended by this components actions.
	 */
	void revalidate(DisplayContext context, UpdateQueue actions);

	/**
	 * This method must be called if the incremental updates of this {@link AJAXSupport} will not be
	 * used since a complete redraw is enforced.
	 */
	void invalidate();

}
