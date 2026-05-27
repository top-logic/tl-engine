/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.slot;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.slot.control.SlotContentControl;

/**
 * UIElement that contributes its children to the nearest matching {@link SlotElement}.
 *
 * <p>
 * The contribution renders nothing at its declared position; instead, the children are rendered
 * by the matched {@code <slot>} placeholder. The children are constructed in the declaring
 * view's {@link ViewContext}, so their channel references resolve locally.
 * </p>
 */
public class SlotContentElement implements UIElement {

	/**
	 * Configuration for {@link SlotContentElement}.
	 */
	@TagName("slot-content")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(SlotContentElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getTo()}. */
		String TO = "to";

		/** Configuration name for {@link #getChildren()}. */
		String CHILDREN = "children";

		/**
		 * Name of the target {@code <slot>}. Contribution is routed to the nearest such slot in
		 * the tree.
		 *
		 * <p>
		 * Attribute is called {@code to} (not {@code slot}) to avoid ambiguity with the {@code
		 * <slot>} child tag name inside the default container.
		 * </p>
		 */
		@Name(TO)
		@Mandatory
		String getTo();

		/**
		 * The contributed child elements.
		 */
		@Name(CHILDREN)
		@DefaultContainer
		@TreeProperty
		List<PolymorphicConfiguration<? extends UIElement>> getChildren();
	}

	private final String _slotName;

	private final List<UIElement> _children;

	/**
	 * Creates a new {@link SlotContentElement} from configuration.
	 */
	@CalledByReflection
	public SlotContentElement(InstantiationContext context, Config config) {
		_slotName = config.getTo();
		_children = new ArrayList<>(config.getChildren().size());
		for (PolymorphicConfiguration<? extends UIElement> childConfig : config.getChildren()) {
			_children.add(context.getInstance(childConfig));
		}
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<ReactControl> childControls = new ArrayList<>(_children.size());
		for (int i = 0; i < _children.size(); i++) {
			ViewContext childContext = context.withChildSlotPath(Integer.toString(i));
			childControls.add((ReactControl) _children.get(i).createControl(childContext));
		}
		return new SlotContentControl(context, _slotName, context.getSlotPath(),
			context.getSlotRegistry(), childControls);
	}
}
