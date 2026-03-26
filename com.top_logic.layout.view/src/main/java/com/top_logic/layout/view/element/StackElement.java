/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackAlign;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackDirection;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackGap;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * UIElement that wraps {@link ReactStackControl}.
 *
 * <p>
 * Renders a flexbox container that arranges its children along a configurable direction with
 * configurable gap and alignment.
 * </p>
 */
public class StackElement extends ContainerElement {

	/**
	 * Configuration for {@link StackElement}.
	 */
	@TagName("stack")
	public interface Config extends ContainerElement.Config {

		@Override
		@ClassDefault(StackElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getDirection()}. */
		String DIRECTION = "direction";

		/** Configuration name for {@link #getGap()}. */
		String GAP = "gap";

		/** Configuration name for {@link #getAlign()}. */
		String ALIGN = "align";

		/**
		 * The flex direction.
		 */
		@Name(DIRECTION)
		StackDirection getDirection();

		/**
		 * The gap between children.
		 */
		@Name(GAP)
		StackGap getGap();

		/**
		 * The cross-axis alignment.
		 */
		@Name(ALIGN)
		StackAlign getAlign();
	}

	private final StackDirection _direction;

	private final StackGap _gap;

	private final StackAlign _align;

	/**
	 * Creates a new {@link StackElement} from configuration.
	 */
	@CalledByReflection
	public StackElement(InstantiationContext context, Config config) {
		super(context, config);
		_direction = config.getDirection();
		_gap = config.getGap();
		_align = config.getAlign();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<IReactControl> childControls = createChildControls(context);

		List<ReactControl> reactChildren = childControls.stream()
			.map(c -> (ReactControl) c)
			.collect(Collectors.toList());

		return new ReactStackControl(context, _direction, _gap, _align, false, reactChildren);
	}
}
