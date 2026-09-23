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
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactColumnsControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackGap;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * A page laid out in columns of unequal width that reflows to a single column when the space gets
 * narrow.
 *
 * <p>
 * Each {@link ColumnElement column} takes a share of the width according to its weight: a main
 * column of weight 2 beside a side column of weight 1 splits the width two thirds to one third.
 * The columns keep the order they are written in, from left to right.
 * </p>
 *
 * <p>
 * Below the breakpoint the columns stack, each one over the full width, in the order they are
 * written: the main column first, the side column below it. What decides is the width this element
 * itself is granted, not the width of the browser window, so the same page reflows inside a narrow
 * pane of a split panel just as it does on a phone.
 * </p>
 *
 * <p>
 * The columns are a page layout, not a set of panes: there are no splitters to drag, the layout is
 * as tall as its tallest column, and a column is as tall as its own content. Content longer than
 * the window is reached by scrolling the page, so a long main column and a short side column read
 * as one page rather than as two boxes scrolling against each other.
 * </p>
 *
 * @implNote Renders through {@link ReactColumnsControl}, which lays the columns out as a wrapping
 *           flex row and gives each column a flex basis derived from the breakpoint, so that the
 *           reflow is decided by the browser layout without measuring the element.
 */
@InApp
public class ColumnsElement implements UIElement {

	/**
	 * Configuration for {@link ColumnsElement}.
	 */
	@TagName("columns")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getBreakpoint()}. */
		String BREAKPOINT = "breakpoint";

		/** Configuration name for {@link #getGap()}. */
		String GAP = "gap";

		/** Configuration name for {@link #getColumns()}. */
		String COLUMNS = "columns";

		@Override
		@ClassDefault(ColumnsElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * The width below which the columns stack, as a CSS length such as "48rem".
		 *
		 * <p>
		 * Compared against the width this element is granted, not against the width of the
		 * browser window. The same layout therefore stacks inside a narrow pane of a wide window
		 * as well.
		 * </p>
		 */
		@Name(BREAKPOINT)
		@StringDefault("48rem")
		String getBreakpoint();

		/**
		 * The space between the columns, and between the stacked columns below the breakpoint.
		 */
		@Name(GAP)
		StackGap getGap();

		/**
		 * The columns, from left to right, and stacked in this order below the breakpoint.
		 *
		 * <p>
		 * At least one column is needed.
		 * </p>
		 */
		@Name(COLUMNS)
		@DefaultContainer
		@TreeProperty
		List<PolymorphicConfiguration<? extends ColumnElement>> getColumns();
	}

	private final String _breakpoint;

	private final StackGap _gap;

	private final List<ColumnElement> _columns;

	/**
	 * Creates a new {@link ColumnsElement} from configuration.
	 */
	@CalledByReflection
	public ColumnsElement(InstantiationContext context, Config config) {
		_breakpoint = config.getBreakpoint();
		_gap = config.getGap();
		_columns = new ArrayList<>();
		for (PolymorphicConfiguration<? extends ColumnElement> columnConfig : config.getColumns()) {
			ColumnElement column = context.getInstance(columnConfig);
			if (column != null) {
				_columns.add(column);
			}
		}

		if (_columns.isEmpty()) {
			context.error("A <columns> layout places content in columns and therefore needs at least"
				+ " one <column> child.");
		}
	}

	@Override
	public List<ChildGroup> getChildGroups() {
		return List.of(ChildGroup.elements(List.<UIElement> copyOf(_columns)));
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<Integer> weights = new ArrayList<>(_columns.size());
		List<ReactControl> columnControls = new ArrayList<>(_columns.size());
		for (int n = 0, cnt = _columns.size(); n < cnt; n++) {
			ColumnElement column = _columns.get(n);
			ViewContext columnContext = context.withChildSlotPath(Integer.toString(n));

			weights.add(Integer.valueOf(column.getWeight()));
			columnControls.add((ReactControl) column.createControl(columnContext));
		}

		return new ReactColumnsControl(context, _breakpoint, _gap, weights, columnControls);
	}
}
