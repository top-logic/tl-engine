/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A {@link ReactControl} that stacks content over a base control via the {@code TLOverlay} React
 * component.
 *
 * <p>
 * The base defines the size of the overlay; every layer is placed over it, anchored to an edge, a
 * corner or the middle of that area. A caption over a picture, a badge in the corner of an avatar
 * and a scrim over a card are all the same composition.
 * </p>
 *
 * <p>
 * A layer passes the pointer through wherever it shows nothing, so that the base stays usable below
 * the free space of a layer that only anchors a small control.
 * </p>
 */
public class ReactOverlayControl extends ReactControl {

	private static final String REACT_MODULE = "TLOverlay";

	/** State key for the control the layers are placed over. */
	private static final String BASE = "base";

	/** State key for the layers, outermost last. */
	private static final String LAYERS = "layers";

	/** State key for the control a layer shows. */
	private static final String CONTENT = "control";

	/** State key for the position a layer is anchored at. */
	private static final String ANCHOR = "anchor";

	/** @see #setCssClass(String) */
	private static final String CSS_CLASS = "cssClass";

	/**
	 * Content placed over the base of a {@link ReactOverlayControl}.
	 *
	 * @param content
	 *        The control shown over the base.
	 * @param anchor
	 *        Where the content sits over the base.
	 * @param cssClass
	 *        An additional CSS class of the layer, or {@code null} for none.
	 */
	public record Layer(ReactControl content, LayerAnchor anchor, String cssClass) {

		/**
		 * Creates a {@link Layer} without a CSS class of its own.
		 *
		 * @param content
		 *        The control shown over the base.
		 * @param anchor
		 *        Where the content sits over the base.
		 */
		public Layer(ReactControl content, LayerAnchor anchor) {
			this(content, anchor, null);
		}
	}

	/**
	 * Creates a {@link ReactOverlayControl}.
	 *
	 * @param context
	 *        The {@link ReactContext} for ID allocation and SSE registration.
	 * @param base
	 *        The control the layers are placed over, which defines the size of the overlay.
	 * @param layers
	 *        The content over the base, outermost last.
	 */
	public ReactOverlayControl(ReactContext context, ReactControl base, List<Layer> layers) {
		super(context, null, REACT_MODULE);
		putState(BASE, base);
		putState(LAYERS, layers.stream().map(ReactOverlayControl::descriptor).toList());
	}

	private static Map<String, Object> descriptor(Layer layer) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(CONTENT, layer.content());
		result.put(ANCHOR, layer.anchor().getExternalName());
		result.put(CSS_CLASS, layer.cssClass());
		return result;
	}

	/**
	 * Sets an additional CSS class, appended to the classes of the overlay.
	 *
	 * @param cssClass
	 *        The CSS class, or {@code null} for none.
	 */
	public void setCssClass(String cssClass) {
		putState(CSS_CLASS, cssClass);
	}

	/**
	 * Rendering-only state keys, omitted from the headless projection.
	 */
	@Override
	protected Set<String> scriptingPresentationKeys() {
		return Set.of(CSS_CLASS);
	}

	/**
	 * Structural: this control stacks its children and is elided from the headless projection.
	 */
	@Override
	public boolean scriptingTransparent() {
		return true;
	}

}
