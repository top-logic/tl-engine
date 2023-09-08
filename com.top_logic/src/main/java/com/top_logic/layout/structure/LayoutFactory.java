/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.layout.structure.LayoutControlProvider.Strategy;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.mig.html.layout.DialogComponent;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Global algorithm for transforming {@link LayoutComponent}s into {@link LayoutControl}s.
 * 
 * <p>
 * This global algorithm can be customized by attaching {@link LayoutControlProvider}s to certain
 * {@link LayoutComponent}s, see {@link LayoutComponent#getComponentControlProvider()}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface LayoutFactory {

	/**
	 * Transforms the {@link LayoutComponent} hierarchy rooted at the given component into a
	 * {@link LayoutControl} hierarchy.
	 * 
	 * <p>
	 * Note: This must <b>not</b> be called from within a {@link LayoutControlProvider} for the
	 * passed component. This method may only be used to create layouts for child components of the
	 * component passed to a {@link LayoutControlProvider}. To create the <i>default</i> layout for
	 * the component passed to a {@link LayoutControlProvider},
	 * {@link Strategy#createDefaultLayout(LayoutComponent)} must be used instead!
	 * </p>
	 * 
	 * @param component
	 *        The component root to transform.
	 * @return The {@link LayoutControl} used to render the given component.
	 */
	LayoutControl createLayout(LayoutComponent component);

	/**
	 * Creates a new {@link LayoutControl} for the given component and the given context toolbar.
	 * 
	 * <p>
	 * Note: This must <b>not</b> be called from within a {@link LayoutControlProvider} for the
	 * passed component. This method may only be used to create layouts for child components of the
	 * component passed to a {@link LayoutControlProvider}. To create the <i>default</i> layout for
	 * the component passed to a {@link LayoutControlProvider},
	 * {@link Strategy#createDefaultLayout(LayoutComponent)} must be used instead!
	 * </p>
	 * 
	 * <p>
	 * A call with <code>null</code> <code>contextToolbar</code> is <b>not</b> equivalent with
	 * {@link #createLayout(LayoutComponent)}.
	 * </p>
	 * 
	 * @param component
	 *        The component root to transform.
	 * @param contextToolbar
	 *        {@link ToolBar} of the context to use for the given component. If
	 *        <code>contextToolbar</code> is <code>null</code>, the heuristic starts to detect the
	 *        {@link ToolBar} for the given component is applied.
	 * 
	 * @return The {@link LayoutControl} used to render the given component.
	 */
	LayoutControl createLayout(LayoutComponent component, ToolBar contextToolbar);

	/**
	 * Creates a window control for the given dialog.
	 * 
	 * @param dialog
	 *        The component root to transform.
	 * @return The {@link DialogWindowControl} used to render the given component.
	 */
	DialogWindowControl createDialogLayout(DialogComponent dialog);

}