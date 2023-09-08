/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.model;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.layout.DisplayDimension;

/**
 * Base class for {@link Box} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractBox implements Box {

	private int _columns;

	private int _rows;

	private DisplayDimension _width = DisplayDimension.HUNDERED_PERCENT;

	private String _cssClass = null;

	@Override
	public final Box getParent() {
		return internals().getParent();
	}

	@Override
	public final int getColumns() {
		return _columns;
	}

	@Override
	public final int getRows() {
		return _rows;
	}

	/**
	 * Initializes {@link #getRows()} and {@link #getColumns()}.
	 * 
	 * @param columns
	 *        See {@link #getColumns()}.
	 * @param rows
	 *        See {@link #getRows()}.
	 */
	public void setDimension(int columns, int rows) {
		_columns = columns;
		_rows = rows;

		// Note: No event must be fired here, because this method is only called internally during
		// the layout process (which happens during/directly-before rendering)
	}

	@Override
	public DisplayDimension getWidth() {
		return _width;
	}

	@Override
	public void setWidth(DisplayDimension width) {
		_width = width;
		notifyLayoutChange();
	}

	@Override
	public final String getCssClass() {
		return _cssClass;
	}

	@Override
	public void setCssClass(String cssClass) {
		_cssClass = cssClass;
		notifyLayoutChange();
	}

	@Override
	public String getStyle() {
		return null;
	}

	@Override
	public final void enter(int x, int y, int availableColumns, int availableRows, Table<ContentBox> table) {
		setDimension(availableColumns, availableRows);
		enterContent(x, y, availableColumns, availableRows, table);
	}

	@Override
	public final void layout() {
		localLayout();
	}

	/**
	 * Informs {@link BoxListener}s about a change in the hierarchy.
	 */
	protected void notifyLayoutChange() {
		internals().notifyLayoutChange(this);
	}

	/**
	 * Implementation of {@link #layout()}.
	 */
	protected abstract void localLayout();

	/**
	 * Implementation of {@link #enter(int, int, int, int, Table)}.
	 */
	protected abstract void enterContent(int x, int y, int availableColumns, int availableRows, Table<ContentBox> table);

	@Override
	public <L extends PropertyListener, S, V> boolean addListener(EventType<L, S, V> type, L listener) {
		return internals().addListener(type, listener);
	}

	@Override
	public <L extends PropertyListener, S, V> boolean removeListener(EventType<L, S, V> type, L listener) {
		return internals().removeListener(type, listener);
	}

	/**
	 * @see BoxInternals#isAttached()
	 */
	protected final boolean isAttached() {
		return internals().isAttached();
	}

	/**
	 * Hook called when the first {@link Box#LAYOUT_CHANGE} listener is added.
	 */
	protected void attach() {
		// Hook for sub-classes.
	}

	/**
	 * Hook called when the last {@link Box#LAYOUT_CHANGE} listener is removed.
	 */
	protected void detach() {
		// Hook for sub-classes.
	}

	@Override
	public final BoxInternals internals() {
		return _internals;
	}

	private final BoxInternals _internals = new BoxInternals() {
		@Override
		protected void onAttach() {
			AbstractBox.this.attach();
		}

		@Override
		protected void onDetach() {
			AbstractBox.this.detach();
		}

		@Override
		protected Box self() {
			return AbstractBox.this;
		}
	};

}
