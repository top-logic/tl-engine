/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.constraint.annotation.Bound;
import com.top_logic.basic.config.constraint.annotation.Comparision;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackAlign;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackDirection;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackGap;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * One column of a {@link ColumnsElement}.
 *
 * <p>
 * The content of a column stands below each other in the order it is written, each element over
 * the full width of the column. A column is as tall as its content; it neither scrolls on its own
 * nor is it stretched to the height of a taller neighbour, so a main column of text beside a short
 * side column simply reaches further down the page.
 * </p>
 *
 * <p>
 * The weight says how much of the width this column takes while the columns stand next to each
 * other: two columns of weight 2 and weight 1 share the width two thirds to one third, two columns
 * of the same weight share it evenly. Below the breakpoint of the surrounding columns layout the
 * weight has no effect, because every column then takes the full width.
 * </p>
 */
@InApp
public class ColumnElement extends ContainerElement {

	/**
	 * Configuration for {@link ColumnElement}.
	 */
	@TagName("column")
	public interface Config extends ContainerElement.Config {

		/** Configuration name for {@link #getWeight()}. */
		String WEIGHT = "weight";

		@Override
		@ClassDefault(ColumnElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * The share of the width this column takes, relative to the weights of the other columns.
		 *
		 * <p>
		 * A column of weight 2 beside a column of weight 1 is twice as wide as that one. The
		 * weight decides nothing while the columns stack below the breakpoint, where each column
		 * takes the full width.
		 * </p>
		 */
		@Name(WEIGHT)
		@IntDefault(1)
		@Bound(comparison = Comparision.GREATER_OR_EQUAL, value = 1)
		int getWeight();
	}

	private final int _weight;

	/**
	 * Creates a new {@link ColumnElement} from configuration.
	 */
	@CalledByReflection
	public ColumnElement(InstantiationContext context, Config config) {
		super(context, config);
		_weight = config.getWeight();
	}

	/**
	 * The share of the width this column takes.
	 *
	 * @see Config#getWeight()
	 */
	public int getWeight() {
		return _weight;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<ReactControl> children = createChildControls(context).stream()
			.map(c -> (ReactControl) c)
			.collect(Collectors.toList());

		return new ReactStackControl(context, StackDirection.COLUMN, StackGap.DEFAULT, StackAlign.STRETCH, false,
			children);
	}
}
