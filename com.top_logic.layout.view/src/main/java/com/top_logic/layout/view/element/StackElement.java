/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackAlign;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackDirection;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackGap;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackJustify;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * UIElement that wraps {@link ReactStackControl}.
 *
 * <p>
 * Renders a flexbox container that arranges its children along a configurable direction: with a
 * configurable gap and alignment, with the space left over distributed as configured, flowing into
 * further lines where asked to, and bounded to a width it is then centered in.
 * </p>
 */
@InApp
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

		/** Configuration name for {@link #getJustify()}. */
		String JUSTIFY = "justify";

		/** Configuration name for {@link #getWrap()}. */
		String WRAP = "wrap";

		/** Configuration name for {@link #getMaxWidth()}. */
		String MAX_WIDTH = "max-width";

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

		/**
		 * How the children are distributed along the direction of the stack.
		 *
		 * <p>
		 * Where the children together are smaller than the stack, this decides what happens with
		 * the space left over: it stays behind them (the default), before them, on both sides, or
		 * it is divided between them - a title and the actions belonging to it pushed to the
		 * opposite ends of a row, a row of buttons centered under a form.
		 * </p>
		 */
		@Name(JUSTIFY)
		StackJustify getJustify();

		/**
		 * Whether the children flow into further lines once they no longer fit next to each other.
		 *
		 * <p>
		 * A wrapping row keeps the size of its children instead of shrinking them into one line,
		 * which is what a row of items that each need a readable width - chips, cards, filter
		 * buttons - does on a narrow screen. Unset, the children stay in one line.
		 * </p>
		 */
		@Name(WRAP)
		boolean getWrap();

		/**
		 * The largest width the stack takes, as a CSS length such as "60rem".
		 *
		 * <p>
		 * A stack of a bounded width is centered in the space it is given, the space left over
		 * split between its two sides, which is how a column of running text stays readable on a
		 * wide screen. Below the bound nothing changes, so the stack still fills a narrow screen.
		 * </p>
		 *
		 * <p>
		 * Unset, the stack takes the width its container offers.
		 * </p>
		 */
		@Name(MAX_WIDTH)
		@Nullable
		String getMaxWidth();
	}

	private final StackDirection _direction;

	private final StackGap _gap;

	private final StackAlign _align;

	private final StackJustify _justify;

	private final boolean _wrap;

	private final String _maxWidth;

	private final String _cssClass;

	/**
	 * Creates a new {@link StackElement} from configuration.
	 */
	@CalledByReflection
	public StackElement(InstantiationContext context, Config config) {
		super(context, config);
		_direction = config.getDirection();
		_gap = config.getGap();
		_align = config.getAlign();
		_justify = config.getJustify();
		_wrap = config.getWrap();
		_maxWidth = config.getMaxWidth();
		_cssClass = config.getCssClass();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<IReactControl> childControls = createChildControls(context);

		List<ReactControl> reactChildren = childControls.stream()
			.map(c -> (ReactControl) c)
			.collect(Collectors.toList());

		ReactStackControl result = new ReactStackControl(context, _direction, _gap, _align, _wrap, reactChildren);
		result.setJustify(_justify);
		result.setMaxWidth(_maxWidth);
		result.setCssClass(_cssClass);
		return result;
	}
}
