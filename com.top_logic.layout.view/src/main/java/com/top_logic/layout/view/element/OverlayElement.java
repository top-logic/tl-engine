/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.LayerAnchor;
import com.top_logic.layout.react.control.layout.ReactOverlayControl;
import com.top_logic.layout.react.control.layout.ReactOverlayControl.Layer;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * {@link UIElement} that stacks content over a base element.
 *
 * <p>
 * The first child is the base; every further child is placed over it. A child that is a
 * {@link LayerElement} brings the position it takes and a CSS class of its own; any other child
 * covers the base as a whole. A caption over a picture, a badge in the corner of an avatar and a
 * scrim over a card are all the same composition.
 * </p>
 *
 * <p>
 * The base gives the overlay its height; its width is what the surrounding layout grants, and a
 * base sized relative to the overlay fills it. A base of fixed width wants a container that does not
 * stretch its items, such as a stack aligned to its start: stretched past its base, the overlay
 * anchors its layers to the free space beside the base rather than to the base.
 * </p>
 *
 * <p>
 * A layer passes the pointer through wherever it shows nothing, so that the base stays usable below
 * the free space of a layer that only anchors a small element.
 * </p>
 */
@InApp
public class OverlayElement extends ContainerElement {

	/**
	 * Configuration for {@link OverlayElement}.
	 */
	@TagName("overlay")
	public interface Config extends ContainerElement.Config {

		/** Configuration name for {@link #getCssClass()}. */
		String CSS_CLASS = "css-class";

		@Override
		@ClassDefault(OverlayElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Additional CSS class appended to the classes of the overlay.
		 */
		@Name(CSS_CLASS)
		@Nullable
		String getCssClass();
	}

	private final String _cssClass;

	/**
	 * Creates a new {@link OverlayElement} from configuration.
	 */
	@CalledByReflection
	public OverlayElement(InstantiationContext context, Config config) {
		super(context, config);
		_cssClass = config.getCssClass();

		if (getChildren().isEmpty()) {
			context.error("An <overlay> stacks content over a base element and therefore needs at"
				+ " least that base element as its first child.");
		}
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<IReactControl> childControls = createChildControls(context);

		List<UIElement> children = getChildren();
		ReactControl base = ContentControls.combine(context,
			childControls.isEmpty() ? List.of() : List.of(childControls.get(0)));

		List<Layer> layers = new ArrayList<>();
		for (int n = 1, cnt = childControls.size(); n < cnt; n++) {
			ReactControl content = (ReactControl) childControls.get(n);
			if (children.get(n) instanceof LayerElement layer) {
				layers.add(new Layer(content, layer.getPosition(), layer.getCssClass()));
			} else {
				layers.add(new Layer(content, LayerAnchor.FILL));
			}
		}

		ReactOverlayControl result = new ReactOverlayControl(context, base, layers);
		result.setCssClass(_cssClass);
		return result;
	}
}
