/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.slot;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.slot.control.SlotPlaceholderControl;

/**
 * UIElement that declares a named placeholder for {@code <slot-content>} contributions.
 *
 * <p>
 * The placeholder renders all contributions whose target slot name matches and whose tree-nearest
 * matching {@code <slot>} is this one. Routing is performed by the shared
 * {@link SlotRegistry}.
 * </p>
 */
public class SlotElement implements UIElement {

	/**
	 * Configuration for {@link SlotElement}.
	 */
	@TagName("slot")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(SlotElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getName()}. */
		String NAME = "name";

		/**
		 * The slot name. {@code <slot-content to="X">} contributions are routed here when this
		 * is the nearest matching {@code <slot name="X"/>} in the tree.
		 */
		@Name(NAME)
		@Mandatory
		String getName();
	}

	private final String _name;

	/**
	 * Creates a new {@link SlotElement} from configuration.
	 */
	@CalledByReflection
	public SlotElement(InstantiationContext context, Config config) {
		_name = config.getName();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return new SlotPlaceholderControl(context, _name, context.getSlotPath(),
			context.getSlotRegistry());
	}
}
