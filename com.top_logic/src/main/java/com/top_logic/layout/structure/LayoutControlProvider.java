/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The class {@link LayoutControlProvider} creates {@link LayoutControl}s for
 * {@link LayoutComponent}s
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface LayoutControlProvider {
	
	/**
	 * Access to the global strategy for transforming {@link LayoutComponent}s into
	 * {@link LayoutControl}s.
	 */
	interface Strategy extends LayoutFactory {
		/**
		 * Algorithm to create the default layout defined by the global factory within a custom
		 * {@link LayoutControlProvider}.
		 * 
		 * @param businessComponent
		 *        The {@link LayoutComponent} to create a control for.
		 * @return The default layout for the given {@link LayoutComponent}.
		 */
		LayoutControl createDefaultLayout(LayoutComponent businessComponent);

		/**
		 * Applies standard decorations to a layout being created.
		 * 
		 * <p>
		 * This method must be called from custom {@link LayoutControlProvider}s from their
		 * {@link LayoutControlProvider#createLayoutControl(Strategy, LayoutComponent)}
		 * implementation, if they do not use {@link #createDefaultLayout(LayoutComponent)} from
		 * this strategy, but create the main view of the given component.
		 * </p>
		 * 
		 * @param component
		 *        The business component the given layout was created for.
		 * @param layouting
		 *        The algorithm to create the {@link LayoutControl} to decorate, usually implemented
		 *        by the calling {@link LayoutControlProvider}.
		 * @return The decorated layout to use.
		 */
		LayoutControl decorate(LayoutComponent component, Layouting layouting);
	}

	/**
	 * Algorithm for creating a plain {@link LayoutControl} from a {@link LayoutComponent} without
	 * any decorations.
	 * 
	 * @see Strategy#decorate(LayoutComponent, Layouting)
	 */
	public interface Layouting {

		/**
		 * Creates a {@link LayoutControl} for the given component.
		 * 
		 * @param strategy
		 *        The {@link Strategy} to create {@link LayoutControl} for child components of the
		 *        given component.
		 * @param component
		 *        The component to display.
		 * 
		 * @return The {@link LayoutControl} that displays the content of the given component.
		 */
		LayoutControl mkLayout(Strategy strategy, LayoutComponent component);
		
	}
	
	/**
	 * This method returns a {@link LayoutControl} for rendering the given {@link LayoutComponent}.
	 * 
	 * @param strategy
	 *        The global transformation {@link Strategy} to use for e.g. child components.
	 * @param component
	 *        the {@link LayoutComponent} the {@link LayoutControl} is built for.
	 * 
	 * @return a LayoutControl for rendering the given {@link LayoutComponent}.
	 */
	public LayoutControl createLayoutControl(Strategy strategy, LayoutComponent component);

}
