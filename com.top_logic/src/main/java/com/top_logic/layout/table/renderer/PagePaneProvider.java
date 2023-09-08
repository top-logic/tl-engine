/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import static com.top_logic.layout.table.renderer.RangeProviderUtil.*;

import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.display.IndexRange;
import com.top_logic.layout.table.display.VisiblePaneRequest;

/**
 * Provider for the visible pane on the current table page, based on the {@link VisiblePaneRequest}
 * for the table.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class PagePaneProvider {

	public static VisiblePaneRequest getPane(TableViewModel viewModel) {
		if (hasRows(viewModel) && hasDefinedPaneRequest(viewModel)) {
			return getPagePane(viewModel);
		} else {
			return getUndefinedPane();
		}
	}

	private static VisiblePaneRequest getPagePane(TableViewModel viewModel) {
		IndexRange pageRowRange = getPageRowRange(viewModel);
		IndexRange pageColumnRange = getPageColumnRange(viewModel);
		return createPageVisiblePane(pageRowRange, pageColumnRange);
	}

	private static IndexRange getPageRowRange(TableViewModel viewModel) {
		if (hasDefinedPaneRowRequestForPage(viewModel)) {
			return IndexRange.multiIndex(getFirstPaneRowOnPage(viewModel),
				getLastPaneRowOnPage(viewModel),
				getForcedVisibleRowOnPage(viewModel));
		} else {
			return IndexRange.undefined();
		}
	}

	private static int getForcedVisibleRowOnPage(TableViewModel viewModel) {
		if (isForcedVisibleRowRequestOnPage(viewModel)) {
			return getRequestedVisiblePane(viewModel).getRowRange().getForcedVisibleIndexInRange();
		} else {
			return getFirstPaneRowOnPage(viewModel);
		}
	}

	private static boolean isForcedVisibleRowRequestOnPage(TableViewModel viewModel) {
		int firstPanePageRow = getFirstPaneRowOnPage(viewModel);
		int lastPanePageRow = getLastPaneRowOnPage(viewModel);
		int forcedVisibleRow = getRequestedVisiblePane(viewModel).getRowRange().getForcedVisibleIndexInRange();
		return firstPanePageRow <= forcedVisibleRow && forcedVisibleRow <= lastPanePageRow;
	}

	private static IndexRange getPageColumnRange(TableViewModel viewModel) {
		VisiblePaneRequest requestedVisiblePane = getRequestedVisiblePane(viewModel);
		IndexRange columnRangeRequest = requestedVisiblePane.getColumnRange();
		return IndexRange.multiIndex(columnRangeRequest.getFirstIndex(),
			columnRangeRequest.getLastIndex(),
			columnRangeRequest.getForcedVisibleIndexInRange());
	}

	private static int getFirstPaneRowOnPage(TableViewModel viewModel) {
		return Math.max(getPaneRequestRowRange(viewModel).getFirstIndex(), viewModel.getPagingModel()
			.getFirstRowOnCurrentPage());
	}

	private static int getLastPaneRowOnPage(TableViewModel viewModel) {
		return Math.min(getPaneRequestRowRange(viewModel).getLastIndex(), viewModel.getPagingModel()
			.getLastRowOnCurrentPage());
	}

	private static VisiblePaneRequest getUndefinedPane() {
		return createPageVisiblePane(IndexRange.undefined(), IndexRange.undefined());
	}

	private static VisiblePaneRequest getRequestedVisiblePane(TableViewModel viewModel) {
		return viewModel.getClientDisplayData().getVisiblePaneRequest();
	}

	private static VisiblePaneRequest createPageVisiblePane(IndexRange pageRowRange, IndexRange columnRowRange) {
		return new VisiblePaneRequest(pageRowRange, columnRowRange);
	}
}
