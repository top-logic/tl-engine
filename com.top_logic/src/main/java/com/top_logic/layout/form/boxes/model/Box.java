/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.model;

import java.util.Collection;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.listener.PropertyObservable;
import com.top_logic.layout.DisplayDimension;

/**
 * A box describes a part of a table layout with rows and columns.
 * 
 * <p>
 * A {@link Box} may either entirely consist of other non-overlapping {@link Box}es, or directly
 * occupy a rectangular region of a table.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Box extends PropertyObservable, BoxSPI {

	/**
	 * {@link EventType} for registering {@link BoxListener}s.
	 * 
	 * @see #addListener(EventType, PropertyListener)
	 */
	EventType<BoxListener, Box, Void> LAYOUT_CHANGE =
		new EventType<>("layoutChange") {
			@Override
			public Bubble dispatch(BoxListener listener, Box sender, Void oldValue, Void newValue) {
				return listener.layoutChanged(sender);
			}
		};

	/**
	 * The {@link Box}, this one is nested in, <code>null</code> if this is the top-level box.
	 */
	Box getParent();

	/**
	 * All {@link Box}es directly nested in this {@link Box}.
	 */
	Collection<Box> getChildren();

	/**
	 * The number of columns, this {@link Box} occupies in the target table.
	 */
	int getColumns();

	/**
	 * The number of rows, this {@link Box} occupies in the target table.
	 */
	int getRows();

	/**
	 * The CSS class string to add to the <code>td</code> created by this box or any of its child
	 * boxes.
	 */
	String getCssClass();

	/**
	 * @see #getCssClass()
	 */
	void setCssClass(String cssClass);

	/**
	 * The CSS style string to add to the <code>td</code> created by this box.
	 * 
	 * <p>
	 * Only relevant for {@link Box} that do not consist of other boxes.
	 * </p>
	 */
	String getStyle();

	/**
	 * Specification of the CSS width of this box.
	 * 
	 * <p>
	 * Only relevant for the first box in a column.
	 * </p>
	 */
	DisplayDimension getWidth();

	/**
	 * @see #getWidth()
	 */
	void setWidth(DisplayDimension width);

	/**
	 * Computes the minimum width and height of this {@link Box}.
	 */
	void layout();

	/**
	 * Computes {@link #getColumns()} and {@link #getRows()} and adds this {@link Box} (or the
	 * {@link Box}es this {@link Box} consists of) to the given target table.
	 * 
	 * @param x
	 *        The column coordinate of the leftmost cell of this {@link Box} in the target table.
	 * @param y
	 *        The row coordinate of the topmost cell of this {@link Box} in the target table.
	 * @param availableColumns
	 *        The number of columns that should be occupied by this {@link Box}.
	 * @param availableRows
	 *        The number of rows that should be occupied by this {@link Box}.
	 * @param table
	 *        The target {@link Table} to add this {@link Box} to.
	 */
	void enter(int x, int y, int availableColumns, int availableRows, Table<ContentBox> table);

}
