/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.display.IndexRange;
import com.top_logic.layout.table.display.VisiblePaneRequest;

/**
 * Utility methods for range provider (e.g. {@link RenderRangeProvider}, {@link PagePaneProvider})
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class RangeProviderUtil {

	/** Whether the given table has rows to display. */
	public static boolean hasRows(TableViewModel viewModel) {
		return viewModel.getRowCount() > 0;
	}

	/**
	 * Whether a visible pane is requested for the given table, either for columns or for rows of
	 * the current page.
	 */
	public static boolean hasDefinedPaneRequest(TableViewModel viewModel) {
		return paneColumnRequestIsDefined(viewModel)
			|| hasDefinedPaneRowRequestForPage(viewModel);
	}

	/** Whether a visible pane is requested for rows of the current page. */
	public static boolean hasDefinedPaneRowRequestForPage(TableViewModel viewModel) {
		return paneRowRequestIsDefined(viewModel) && paneRowRequestHasIntersectionWithPage(viewModel);
	}

	/** Whether the requested visible pane defines a row range. */
	public static boolean paneRowRequestIsDefined(TableViewModel viewModel) {
		return getPaneRequestRowRange(viewModel) != IndexRange.undefined();
	}

	/** Whether the requested visible pane defines a column range. */
	public static boolean paneColumnRequestIsDefined(TableViewModel viewModel) {
		return getPaneRequestColumnRange(viewModel) != IndexRange.undefined();
	}

	/** Whether the row range of the requested visible pane intersects the current page. */
	public static boolean paneRowRequestHasIntersectionWithPage(TableViewModel viewModel) {
		return getPaneRequestRowRange(viewModel).getLastIndex() >= viewModel.getPagingModel().getFirstRowOnCurrentPage() &&
			getPaneRequestRowRange(viewModel).getFirstIndex() <= viewModel.getPagingModel().getLastRowOnCurrentPage();
	}

	/** The row range of the requested visible pane. */
	public static IndexRange getPaneRequestRowRange(TableViewModel viewModel) {
		return getVisiblePaneRequest(viewModel).getRowRange();
	}

	/** The column range of the requested visible pane. */
	public static IndexRange getPaneRequestColumnRange(TableViewModel viewModel) {
		return getVisiblePaneRequest(viewModel).getColumnRange();
	}

	private static VisiblePaneRequest getVisiblePaneRequest(TableViewModel viewModel) {
		return viewModel.getClientDisplayData().getVisiblePaneRequest();
	}
}
