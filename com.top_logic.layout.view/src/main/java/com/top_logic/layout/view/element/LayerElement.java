/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.layout.LayerAnchor;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * Content of an {@link OverlayElement} together with the position it takes over the base.
 *
 * <p>
 * A layer is written inside an {@code overlay} wherever its content needs a position of its own - a
 * caption at the bottom edge of a picture, a badge in a corner of an avatar. Content that covers the
 * whole base needs no layer around it: an element written directly in the overlay is such a layer
 * already.
 * </p>
 *
 * <p>
 * Elsewhere a layer is a plain group of its children: the position is what the surrounding overlay
 * reads from it, and where there is none, there is nothing to position against.
 * </p>
 */
@InApp
public class LayerElement extends ContainerElement {

	/**
	 * Configuration for {@link LayerElement}.
	 */
	@TagName("layer")
	public interface Config extends ContainerElement.Config {

		/** Configuration name for {@link #getPosition()}. */
		String POSITION = "position";

		@Override
		@ClassDefault(LayerElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Where the content sits over the base of the surrounding overlay: covering it as a whole,
		 * or anchored to one of its edges, corners or its middle.
		 */
		@Name(POSITION)
		LayerAnchor getPosition();
	}

	private final LayerAnchor _position;

	private final String _cssClass;

	/**
	 * Creates a new {@link LayerElement} from configuration.
	 */
	@CalledByReflection
	public LayerElement(InstantiationContext context, Config config) {
		super(context, config);
		_position = config.getPosition();
		_cssClass = config.getCssClass();
	}

	/**
	 * Where this layer sits over the base of the surrounding overlay.
	 */
	public LayerAnchor getPosition() {
		return _position;
	}

	/**
	 * The CSS class this layer is displayed with, or {@code null} for none.
	 *
	 * @see Config#getCssClass()
	 */
	public String getCssClass() {
		return _cssClass;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return ContentControls.combine(context, createChildControls(context));
	}
}
