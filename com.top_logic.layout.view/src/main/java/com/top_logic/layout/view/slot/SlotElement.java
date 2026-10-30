/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.slot;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.slot.control.SlotPlaceholderControl;

/**
 * UIElement that declares a named placeholder for {@code <slot-content>} contributions.
 *
 * <p>
 * The placeholder renders all contributions whose target slot name matches and whose tree-nearest
 * matching {@code <slot>} is this one. Routing is performed by the shared
 * {@link SlotRegistry}. While no contribution is routed here, the placeholder renders the
 * {@link Config#getEmpty() fallback} elements instead.
 * </p>
 */
@InApp
public class SlotElement implements UIElement {

	/**
	 * Configuration for {@link SlotElement}.
	 */
	@TagName("slot")
	@TypeIcon("css:bi bi-puzzle")
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

		/** Configuration name for {@link #getEmpty()}. */
		String EMPTY = "empty";

		/**
		 * Elements shown in the slot while no {@code <slot-content>} is routed to it.
		 *
		 * <p>
		 * As soon as a contribution reaches the slot, it replaces these elements; when the last
		 * contribution leaves the display, they are shown again. The elements are built in the
		 * context of the view declaring the slot, so their channel references resolve there. Left
		 * empty, the slot shows nothing while no contribution is routed to it.
		 * </p>
		 *
		 * @implNote Keyed by {@link UIElement.Config#ID} so that a same-path overlay can address
		 *           an element.
		 */
		@Key(UIElement.Config.ID)
		@Name(EMPTY)
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<UIElement.Config> getEmpty();
	}

	/**
	 * Segment naming the {@link Config#getEmpty() fallback} of a slot in the slot path its elements
	 * are built with.
	 */
	private static final String EMPTY_SEGMENT = Config.EMPTY;

	private final String _name;

	private final List<UIElement> _empty;

	private final String _cssClass;

	/**
	 * Creates a new {@link SlotElement} from configuration.
	 */
	@CalledByReflection
	public SlotElement(InstantiationContext context, Config config) {
		_name = config.getName();
		_empty = new ArrayList<>(config.getEmpty().size());
		for (PolymorphicConfiguration<? extends UIElement> elementConfig : config.getEmpty()) {
			_empty.add(context.getInstance(elementConfig));
		}
		_cssClass = config.getCssClass();
	}

	@Override
	public List<ChildGroup> getChildGroups() {
		return _empty.isEmpty() ? List.of() : List.of(ChildGroup.elements(_empty));
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewContext emptyContext = context.withChildSlotPath(EMPTY_SEGMENT);
		List<ReactControl> emptyControls = new ArrayList<>(_empty.size());
		for (int i = 0; i < _empty.size(); i++) {
			ViewContext elementContext = emptyContext.withChildSlotPath(Integer.toString(i));
			emptyControls.add((ReactControl) _empty.get(i).createControl(elementContext));
		}
		SlotPlaceholderControl result = new SlotPlaceholderControl(context, _name, context.getSlotPath(),
			context.getSlotRegistry(), emptyControls);
		result.setCssClass(_cssClass);
		return result;
	}
}
