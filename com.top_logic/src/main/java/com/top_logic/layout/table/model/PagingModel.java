/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.Arrays;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.NoBubblingEventType;
import com.top_logic.basic.listener.PropertyObservableBase;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LayoutContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.TableModel;
import com.top_logic.util.ToBeValidated;

/**
 * Paging state of a table.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PagingModel extends PropertyObservableBase implements TableModelListener, ToBeValidated {

	/** ID suffix, which is used to store current page size in {@link PersonalConfiguration} */
	public static final String PAGE_SIZE_PROPERTY_SUFFIX = ".pageSize";

	/**
	 * Special {@link #getPageSizeSpec()} that requests disable paging.
	 */
	public static final int SHOW_ALL = 0;

	/**
	 * {@link EventType} for {@link #getPage()} changes.
	 * 
	 * @see PageListener
	 */
	public static final EventType<PageListener, PagingModel, Integer> PAGE_EVENT =
		new NoBubblingEventType<>("page") {

			@Override
			protected void internalDispatch(PageListener listener, PagingModel sender, Integer oldValue,
					Integer newValue) {
				listener.handlePageChanged(sender, oldValue, newValue);
			}

		};

	/**
	 * {@link EventType} for {@link #getPageSizeSpec()} changes.
	 * 
	 * @see PageSizeListener
	 */
	public static final EventType<PageSizeListener, PagingModel, Integer> PAGE_SIZE_EVENT =
		new NoBubblingEventType<>("pageSize") {

			@Override
			protected void internalDispatch(PageSizeListener listener, PagingModel sender, Integer oldValue,
					Integer newValue) {
				listener.handlePageSizeChanged(sender, oldValue, newValue);
			}

		};

	/**
	 * {@link EventType} for {@link #getPageCount()} changes.
	 * 
	 * @see PageCountListener
	 */
	public static final EventType<PageCountListener, PagingModel, Integer> PAGE_COUNT_EVENT =
		new NoBubblingEventType<>("pageCount") {

			@Override
			protected void internalDispatch(PageCountListener listener, PagingModel sender, Integer oldValue,
					Integer newValue) {
				listener.handlePageCountChanged(sender, oldValue, newValue);
			}

		};

	/**
	 * {@link EventType} for {@link #getPageSizeOptions()} changes.
	 * 
	 * @see PageSizeOptionsListener
	 */
	public static final EventType<PageSizeOptionsListener, PagingModel, int[]> PAGE_SIZE_OPTIONS_EVENT =
		new NoBubblingEventType<>("pageSizeOptions") {

			@Override
			protected void internalDispatch(PageSizeOptionsListener listener, PagingModel sender, int[] oldValue,
					int[] newValue) {
				listener.handlePageSizeOptionsChanged(sender, oldValue, newValue);
			}

		};

	private static final int DEFAULT_INDEX = 0;

	private State _state;

	private static class State {
		private boolean _showAll;

		private int _rowCount;

		private int _pageSize;

		private int _pageCount;

		private int _page;

		private int _currentPageSize;

		public State(boolean showAll, int rowCount, int pageSize, int pageCount, int page) {
			_showAll = showAll;
			_rowCount = rowCount;
			_pageSize = pageSize;
			_pageCount = pageCount;
			_page = page;

			consolidate();
		}

		public State(State other) {
			_showAll = other._showAll;
			_rowCount = other._rowCount;
			_pageSize = other._pageSize;
			_pageCount = other._pageCount;
			_page = other._page;

			_currentPageSize = other._currentPageSize;
		}

		public void setRowCount(int rowCount) {
			_rowCount = rowCount;
			consolidate();
		}

		public void setPageSize(int pageSize) {
			_showAll = pageSize == SHOW_ALL;
			_pageSize = pageSize;
			consolidate();
		}

		public void setPage(int page) {
			_page = page;
			consolidate();
		}

		void consolidate() {
			int effectivePageSize = computeEffectivePageSize(_showAll, _rowCount, _pageSize);
			_pageCount = computePageCount(_rowCount, effectivePageSize);
			_page = computePage(_pageCount, _page);
			_currentPageSize = computeCurrentPageSize(_rowCount, effectivePageSize, _page);
		}

		private int computeCurrentPageSize(int rowCount, int effectivePageSize, int page) {
			return Math.min(effectivePageSize, rowCount - page * effectivePageSize);
		}

		private static int computeEffectivePageSize(boolean showAll, int rowCount, int pageSize) {
			if (showAll) {
				return rowCount;
			} else {
				return pageSize;
			}
		}

		private static int computePageCount(int rowCount, int effectivePageSize) {
			if ((rowCount == 0) || (effectivePageSize == 0)) {
				// May only happen, if there are no rows in the current table model.
				return 1;
			}

			return (rowCount + effectivePageSize - 1) / effectivePageSize;
		}

		/**
		 * @param pageCount
		 *        The total number of pages.
		 * @param page
		 *        The last visible page.
		 */
		private static int computePage(int pageCount, int page) {
			if (page >= pageCount) {
				return pageCount - 1;
			} else {
				return page;
			}
		}

		public State copy() {
			return new State(this);
		}

		public int getPageSize() {
			return _pageSize;
		}

		public int getEffectivePageSize() {
			return computeEffectivePageSize(_showAll, _rowCount, _pageSize);
		}

		public int getCurrentPageSize() {
			return _currentPageSize;
		}

		public int getPage() {
			return _page;
		}

		public int getPageCount() {
			return _pageCount;
		}
	}


	/**
	 * Array of valid page sizes
	 */
	private int[] pageSizeOptions;

	/**
	 * Index into the {@link #getPageSizeOptions()} array that represents the current page size.
	 */
	private int _pageSizeIndex;

	private ConfigKey _configKey;

	private final TableModel _tableModel;

	private boolean invalid;


	/**
	 * Creates a {@link PagingModel}.
	 * 
	 * @param configKey
	 *        The {@link ConfigKey} to store the page size personalization with.
	 */
	public PagingModel(ConfigKey configKey, TableModel tableModel) {
		_configKey = configKey;
		_tableModel = tableModel;

		internalSetPageSizeOptions(TableConfiguration.getDefaultPageSizeOptions());
		this._pageSizeIndex = DEFAULT_INDEX;

		_tableModel.addTableModelListener(this);

		_state = new State(false, 0, 0, 0, 0);
		updatePageSize();
		updateRowCount();
	}

	private int internalGetRowCount() {
		return _tableModel.getRowCount();
	}

	/**
	 * The currently displayed page.
	 */
	public int getPage() {
		revalidate();
		return _state.getPage();
	}

	/**
	 * @see #getPage()
	 */
	public void setPage(int newPage) {
		revalidate();
		State before = stateCopy();
		_state.setPage(newPage);
		fireEvents(before);
	}

	/**
	 * Get the number of rows that are displayed on one page, or {@value #SHOW_ALL}, if paging is
	 * disabled.
	 */
	public final int getPageSizeSpec() {
		revalidate();
		return _state.getPageSize();
	}

	/**
	 * The {@link #getPageSizeSpec()}, where {@link #SHOW_ALL} is replaced by the number of total rows.
	 */
	public final int getEffectivePageSize() {
		revalidate();
		return _state.getEffectivePageSize();
	}

	/**
	 * The number of rows displayed on the current page.
	 */
	public final int getCurrentPageSize() {
		revalidate();
		return _state.getCurrentPageSize();
	}

	private int internalGetPageSize() {
		return getPageSize(_pageSizeIndex);
	}

	private int getPageSize(int index) {
		return getPageSizeOptions()[index];
	}

	/**
	 * Transiently sets the new page size.
	 * 
	 * @see #changePageSizeSpec(int)
	 * 
	 * @param requestedPageSize
	 *        The new {@link #getPageSizeSpec()}. The value must be contained in
	 *        {@link #getPageSizeOptions()}.
	 */
	public final void setPageSizeSpec(int requestedPageSize) {
		assert requestedPageSize >= 0 : "Page size must be 0 or greater.";
	
		int newIndex = ArrayUtil.indexOf(requestedPageSize, getPageSizeOptions());
		setPageSizeIndex(newIndex);
	}

	private void setPageSizeIndex(int newIndex) {
		revalidate();
		State before = stateCopy();
		this._pageSizeIndex = validatePageSizeIndex(newIndex);

		updatePageSize();
		fireEvents(before);
	}

	/**
	 * Sets the new page size to the personal configuration.
	 * 
	 * @see #setPageSizeSpec(int)
	 */
	public void changePageSizeSpec(int newPageSize) {
		setPageSizeSpec(newPageSize);
		savePageSize(_configKey);
	}

	/**
	 * Setter for page size options.
	 * 
	 * <p>
	 * Note: The current page size may change, if it is not part of the new options.
	 * </p>
	 */
	public void setPageSizeOptions(int... newOptions) {
		revalidate();
		State before = stateCopy();

		int[] oldOptions = this.pageSizeOptions;
		internalSetPageSizeOptions(newOptions);
		firePageSizeOptions(oldOptions, newOptions);

		updatePageSize();
		fireEvents(before);
	}

	private void internalSetPageSizeOptions(int[] newOptions) {
		this.pageSizeOptions = newOptions;
		_pageSizeIndex = validatePageSizeIndex(_pageSizeIndex);
	}

	private int validatePageSizeIndex(int index) {
		int optionsLength = pageSizeOptions.length;
		if (index < 0 || index > optionsLength) {
			index = DEFAULT_INDEX;
		}
		return index;
	}

	/**
	 * The valid values for {@link #getPageSizeSpec()}.
	 */
	public int[] getPageSizeOptions() {
		return pageSizeOptions;
	}

	/**
	 * Retrieves the personal page size settings from personal configuration stored under the
	 * default {@link ConfigKey}.
	 * 
	 * @see #loadPageSize(ConfigKey)
	 */
	public void loadPageSize() {
		loadPageSize(_configKey);
	}

	/**
	 * Retrieves the personal page size settings from personal configuration stored under the given
	 * {@link ConfigKey}.
	 */
	public void loadPageSize(ConfigKey configKey) {
		String pageSizeConfigKey = pageSizeConfigKey(configKey);
		if (pageSizeConfigKey == null) {
			return;
		}
	
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return;
		}

		Integer personalPageSize = (Integer) pc.getValue(pageSizeConfigKey);
		if (personalPageSize == null) {
			return;
		}

		setPageSizeSpec(personalPageSize.intValue());
	}

	/**
	 * Restores the page size to its default value (e.g. provided by configuration).
	 */
	public void resetPageSize() {
		setPageSizeSpec(pageSizeOptions[0]);

		removeStoredPageSize(_configKey);
	}

	/**
	 * Removes the page size stored under the given {@link ConfigKey} from the personal
	 * configuration. The page size in this {@link PagingModel} remains untouched.
	 */
	public void removeStoredPageSize(ConfigKey configKey) {
		String pageSizeConfigKey = pageSizeConfigKey(configKey);
		if (pageSizeConfigKey == null) {
			return;
		}

		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return;
		}

		pc.setValue(pageSizeConfigKey, null);
	}

	/**
	 * Stores the current page size under the given {@link ConfigKey} to the personal configuration.
	 */
	public void savePageSize(ConfigKey configKey) {
		String pageSizeConfigKey = pageSizeConfigKey(configKey);
		if (pageSizeConfigKey == null) {
			return;
		}

		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return;
		}

		pc.setValue(pageSizeConfigKey, internalGetPageSize());
	}

	private String pageSizeConfigKey(ConfigKey globalKey) {
		String tableId = globalKey.get();
		if (tableId == null) {
			return null;
		}

		return tableId + PAGE_SIZE_PROPERTY_SUFFIX;
	}

	/**
	 * Get the number of pages of this table.
	 */
	public int getPageCount() {
		revalidate();
		return _state.getPageCount();
	}

	/**
	 * The index of the first row displayed on the current {@link #getPage()}.
	 */
	public final int getFirstRowOnCurrentPage() {
		return getPage() * getEffectivePageSize();
	}

	/**
	 * Row number of the last row on the current page.
	 */
	public final int getLastRowOnCurrentPage() {
		return getFirstRowOnCurrentPage() + getCurrentPageSize() - 1;
	}

	/**
	 * Navigates to first page.
	 */
	public final void showFirstPage() {
		setPage(0);
	}

	/**
	 * Navigates to previous page.
	 */
	public final void showPreviousPage() {
		setPage((getPage() - 1));
	}

	/**
	 * Navigates to next page.
	 */
	public final void showNextPage() {
		setPage((getPage() + 1));
	}

	/**
	 * Navigates to last page.
	 */
	public final void showLastPage() {
		setPage((getPageCount() - 1));
	}

	/**
	 * Choose a page so that the given row index is displayed.
	 */
	public void showRow(int row) {
		if (row < 0) {
			return;
		}
		setPage(getPage(row));
	}

	/**
	 * Computes the page number, on which the given row is show.
	 * 
	 * @param row
	 *        The row in question.
	 * @return The page, on which the given row is shown.
	 */
	private int getPage(int row) {
		int effectivePageSize = getEffectivePageSize();
		if (effectivePageSize == 0) {
			return 0;
		}
		return row / effectivePageSize;
	}

	@Override
	public void handleTableModelEvent(TableModelEvent event) {
		switch (event.getType()) {
			case TableModelEvent.DELETE:
			case TableModelEvent.INSERT:
			case TableModelEvent.INVALIDATE: {
				if (!invalid) {
					invalid = true;

					LayoutContext context = DefaultDisplayContext.getDisplayContext().getLayoutContext();
					if (context.isInCommandPhase()) {
						context.notifyInvalid(this);
					} else {
						// Eagerly validate, because rendering already started.
						revalidate();
					}
				}
				break;
			}
		}
	}

	@Override
	public void validate(DisplayContext context) {
		revalidate();
	}

	private void revalidate() {
		if (invalid) {
			invalid = false;

			State before = stateCopy();
			updateRowCount();
			fireEvents(before);
		}
	}

	private State stateCopy() {
		return _state.copy();
	}

	private void updateRowCount() {
		_state.setRowCount(internalGetRowCount());
	}

	private void updatePageSize() {
		_state.setPageSize(internalGetPageSize());
	}

	private void fireEvents(State before) {
		firePageSize(before.getPageSize(), _state.getPageSize());
		firePageCount(before.getPageCount(), _state.getPageCount());
		firePage(before.getPage(), _state.getPage());
	}

	private void firePageSize(int oldPageSize, int newPageSize) {
		if (newPageSize == oldPageSize) {
			return;
		}
		notifyListeners(PAGE_SIZE_EVENT, this, oldPageSize, newPageSize);
	}

	private void firePageCount(int oldPageCount, int newPageCount) {
		if (newPageCount == oldPageCount) {
			return;
		}
		notifyListeners(PAGE_COUNT_EVENT, this, oldPageCount, newPageCount);
	}

	private void firePage(int oldPage, int newPage) {
		if (newPage == oldPage) {
			return;
		}
		notifyListeners(PAGE_EVENT, this, oldPage, newPage);
	}

	private void firePageSizeOptions(int[] oldOptions, int[] newOptions) {
		if (Arrays.equals(newOptions, oldOptions)) {
			return;
		}
		notifyListeners(PAGE_SIZE_OPTIONS_EVENT, this, oldOptions, newOptions);
	}

}
