/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import static com.top_logic.layout.table.renderer.RangeProviderUtil.*;

import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.display.VisiblePaneRequest;

/**
 * Computes and provides a {@link RenderRange} for a given {@link VisiblePaneRequest}, stored in the
 * {@link TableViewModel}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class RenderRangeProvider {

	public static RenderRange createRange(TableViewModel viewModel) {
		if (hasRows(viewModel)) {
			return createNonEmptyRange(viewModel);
		} else {
			return createEmptyRange();
		}
	}

	private static RenderRange createNonEmptyRange(TableViewModel viewModel) {
		if (hasDefinedPaneRowRequestForPage(viewModel)) {
			return createPaneBasedRange(viewModel);
		} else {
			return createNonPaneBasedRowRange(viewModel);
		}
	}

	private static RenderRange createPaneBasedRange(TableViewModel viewModel) {
		if (mustExpandPaneRange(viewModel)) {
			return createExpandedRange(viewModel);
		} else {
			return createNonExpandedRange(viewModel);
		}
	}

	private static boolean mustExpandPaneRange(TableViewModel viewModel) {
		return getPaneRowCountOnPage(viewModel) < getDefaultWrittenRowCount(viewModel);
	}

	private static int getDefaultWrittenRowCount(TableViewModel viewModel) {
		return Math.min(TableViewModel.INITIAL_VIEW_PORT_ROW_COUNT, viewModel.getPagingModel().getCurrentPageSize());
	}

	private static RenderRange createExpandedRange(TableViewModel viewModel) {
		int firstRenderedRow = getFirstRenderedRowOfExpansion(viewModel);
		int lastRenderedRow = getLastRenderedRow(viewModel, firstRenderedRow);
		return createRenderRange(firstRenderedRow, lastRenderedRow);
	}

	private static int getFirstRenderedRowOfExpansion(TableViewModel viewModel) {
		return getFirstRenderedRow(viewModel, getFirstPaneRowOnPage(viewModel),
			getRowCountRequestBeforePane(viewModel), getFirstPageRow(viewModel));
	}

	private static int getFirstPageRow(TableViewModel viewModel) {
		return viewModel.getPagingModel().getFirstRowOnCurrentPage();
	}

	private static int getLastPageRow(TableViewModel viewModel) {
		return viewModel.getPagingModel().getLastRowOnCurrentPage();
	}

	private static int getFirstRenderedRow(TableViewModel viewModel, int baseRow, int rowCountRequestBeforeBaseRow, int upperBoundRow) {
		int firstRowCandidate = Math.max(baseRow - rowCountRequestBeforeBaseRow, upperBoundRow);
		int lastRowCandidate = firstRowCandidate + getDefaultWrittenRowCount(viewModel) - 1;
		if (lastRowCandidate > getLastPageRow(viewModel)) {
			firstRowCandidate = getLastPageRow(viewModel) - getDefaultWrittenRowCount(viewModel) + 1;
		}
		return Math.max(firstRowCandidate, getFirstPageRow(viewModel));
	}

	private static int getRowCountRequestBeforePane(TableViewModel viewModel) {
		return getRowCountRequestBefore(getExpansionRowCount(viewModel));
	}

	private static int getRowCountRequestBefore(int overallRowCountRequest) {
		return (int) Math.floor((double) overallRowCountRequest / 2);
	}

	private static int getExpansionRowCount(TableViewModel viewModel) {
		return getDefaultWrittenRowCount(viewModel) - getPaneRowCountOnPage(viewModel);
	}

	private static int getLastRenderedRow(TableViewModel viewModel, int firstRenderedRow) {
		int lastRenderedRowCandidate = firstRenderedRow + getDefaultWrittenRowCount(viewModel) - 1;
		int lastPageRow = getLastPageRow(viewModel);
		return Math.min(lastRenderedRowCandidate, lastPageRow);
	}

	private static RenderRange createNonExpandedRange(TableViewModel viewModel) {
		int firstRenderedRow = getFirstRenderedRowOfPane(viewModel);
		int lastRenderedRow = getLastRenderedRow(viewModel, firstRenderedRow);
		return createRenderRange(firstRenderedRow, lastRenderedRow);
	}

	private static int getFirstRenderedRowOfPane(TableViewModel viewModel) {
		int baseRow;
		if (isForcedVisibleRowOnPage(viewModel)) {
			baseRow = getPaneRequestRowRange(viewModel).getForcedVisibleIndexInRange();
		} else {
			baseRow = getFirstPaneRowOnPage(viewModel);
		}
		return getFirstRenderedRow(viewModel,
									baseRow,
									getRowCountRequestBeforeBaseRow(viewModel),
									getFirstPaneRowOnPage(viewModel));
	}

	private static boolean isForcedVisibleRowOnPage(TableViewModel viewModel) {
		int forcedVisibleRow = getPaneRequestRowRange(viewModel).getForcedVisibleIndexInRange();
		return forcedVisibleRow >= getFirstPaneRowOnPage(viewModel) &&
				forcedVisibleRow <= getLastPaneRowOnPage(viewModel);
	}

	private static int getRowCountRequestBeforeBaseRow(TableViewModel viewModel) {
		return getRowCountRequestBefore(getDefaultWrittenRowCount(viewModel) - 1);
	}

	private static int getPaneRowCountOnPage(TableViewModel viewModel) {
		int firstRow = getFirstPaneRowOnPage(viewModel);
		int lastRow = getLastPaneRowOnPage(viewModel);
		return lastRow - firstRow + 1;
	}

	private static int getFirstPaneRowOnPage(TableViewModel viewModel) {
		return Math.max(getPaneRequestRowRange(viewModel).getFirstIndex(), getFirstPageRow(viewModel));
	}

	private static int getLastPaneRowOnPage(TableViewModel viewModel) {
		return Math.min(getPaneRequestRowRange(viewModel).getLastIndex(), getLastPageRow(viewModel));
	}

	private static RenderRange createNonPaneBasedRowRange(TableViewModel viewModel) {
		if (hasDefinedScrollPosition(viewModel)) {
			return createScrollPositionBasedRowRange(viewModel);
		} else {
			return createFirstPageRowRange(viewModel);
		}
	}

	private static boolean hasDefinedScrollPosition(TableViewModel viewModel) {
		return getRowAnchor(viewModel) > -1;
	}

	private static int getRowAnchor(TableViewModel viewModel) {
		return viewModel.getClientDisplayData().getViewportState().getRowAnchor().getIndex();
	}

	private static RenderRange createScrollPositionBasedRowRange(TableViewModel viewModel) {
		int firstRenderedRow = getFirstRenderedRowOfScrollPosition(viewModel);
		int lastRenderedRow = getLastRenderedRow(viewModel, firstRenderedRow);
		return createRenderRange(firstRenderedRow, lastRenderedRow);
	}

	private static int getFirstRenderedRowOfScrollPosition(TableViewModel viewModel) {
		return getFirstRenderedRow(viewModel,
								   getRowAnchor(viewModel),
								   0,
								   getFirstPageRow(viewModel));
	}

	private static RenderRange createFirstPageRowRange(TableViewModel viewModel) {
		int firstRenderedRow = getFirstPageRow(viewModel);
		int lastRenderedRow = getLastRenderedRow(viewModel, firstRenderedRow);
		return createRenderRange(firstRenderedRow, lastRenderedRow);
	}

	private static RenderRange createEmptyRange() {
		int firstRenderedRow = -1;
		int lastRenderedRow = -1;
		return createRenderRange(firstRenderedRow, lastRenderedRow);
	}

	private static RenderRange createRenderRange(int firstRenderedRow, int lastRenderedRow) {
		return new RenderRange(firstRenderedRow, lastRenderedRow);
	}
}
