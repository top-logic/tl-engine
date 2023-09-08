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

	public static boolean hasRows(TableViewModel viewModel) {
		return viewModel.getRowCount() > 0;
	}

	public static boolean hasDefinedPaneRequest(TableViewModel viewModel) {
		return paneColumnRequestIsDefined(viewModel)
			|| hasDefinedPaneRowRequestForPage(viewModel);
	}

	public static boolean hasDefinedPaneRowRequestForPage(TableViewModel viewModel) {
		return paneRowRequestIsDefined(viewModel) && paneRowRequestHasIntersectionWithPage(viewModel);
	}

	public static boolean paneRowRequestIsDefined(TableViewModel viewModel) {
		return getPaneRequestRowRange(viewModel) != IndexRange.undefined();
	}

	public static boolean paneColumnRequestIsDefined(TableViewModel viewModel) {
		return getPaneRequestColumnRange(viewModel) != IndexRange.undefined();
	}

	public static boolean paneRowRequestHasIntersectionWithPage(TableViewModel viewModel) {
		return getPaneRequestRowRange(viewModel).getLastIndex() >= viewModel.getPagingModel().getFirstRowOnCurrentPage() &&
			getPaneRequestRowRange(viewModel).getFirstIndex() <= viewModel.getPagingModel().getLastRowOnCurrentPage();
	}

	public static IndexRange getPaneRequestRowRange(TableViewModel viewModel) {
		return getVisiblePaneRequest(viewModel).getRowRange();
	}

	public static IndexRange getPaneRequestColumnRange(TableViewModel viewModel) {
		return getVisiblePaneRequest(viewModel).getColumnRange();
	}

	private static VisiblePaneRequest getVisiblePaneRequest(TableViewModel viewModel) {
		return viewModel.getClientDisplayData().getVisiblePaneRequest();
	}
}
