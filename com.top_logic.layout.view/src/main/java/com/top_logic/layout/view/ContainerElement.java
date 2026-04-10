/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.layout.react.control.IReactControl;

/**
 * Base class for UIElements that hold a list of child elements.
 */
public abstract class ContainerElement implements UIElement {

	/**
	 * Configuration for {@link ContainerElement}.
	 */
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getChildren()}. */
		String CHILDREN = "children";

		/**
		 * The child elements.
		 */
		@Name(CHILDREN)
		@DefaultContainer
		@TreeProperty
		List<PolymorphicConfiguration<? extends UIElement>> getChildren();
	}

	private final List<UIElement> _children;

	/**
	 * Creates a new {@link ContainerElement} from configuration.
	 */
	@CalledByReflection
	protected ContainerElement(InstantiationContext context, Config config) {
		_children = config.getChildren().stream()
			.map(context::getInstance)
			.collect(Collectors.toList());
	}

	/**
	 * The child elements (shared, stateless).
	 */
	protected List<UIElement> getChildren() {
		return _children;
	}

	/**
	 * Creates controls for all children.
	 */
	protected List<IReactControl> createChildControls(ViewContext context) {
		return _children.stream()
			.map(child -> child.createControl(context))
			.collect(Collectors.toList());
	}
}
