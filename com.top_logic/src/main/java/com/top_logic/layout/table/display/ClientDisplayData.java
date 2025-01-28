/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.display;

import java.io.IOException;

import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.model.TableUtil;
import com.top_logic.layout.table.renderer.PagePaneProvider;

/**
 * Holder for information about table display at client side (e.g. {@link ViewportState} and
 * {@link VisiblePaneRequest}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ClientDisplayData {

	private ViewportState viewportState;
	private VisiblePaneRequest visiblePaneRequest;

	public ClientDisplayData() {
		viewportState = new ViewportState();
		visiblePaneRequest = new VisiblePaneRequest(IndexRange.undefined(), IndexRange.undefined());
	}

	public ViewportState getViewportState() {
		return viewportState;
	}

	public VisiblePaneRequest getVisiblePaneRequest() {
		return visiblePaneRequest;
	}

	/**
	 * Appends the serialized {@link ClientDisplayData client display data} from the given
	 * {@link TableViewModel view model} into the given output.
	 */
	public static void append(Appendable out, TableViewModel viewModel) throws IOException {
		ClientDisplayData clientDisplayData = viewModel.getClientDisplayData();

		out.append("function getDisplayData() {");

		appendPaneRequest(out, viewModel);
		appendViewportState(out, viewModel, clientDisplayData.getViewportState());

		out.append("return { visiblePane:visiblePane, viewportState:viewportState }");
		out.append("}()");
	}

	private static void appendPaneRequest(Appendable out, TableViewModel model) throws IOException {
		VisiblePaneRequest paneRequest = PagePaneProvider.getPane(model);

		IndexRange rowRange = paneRequest.getRowRange();
		IndexRange columnRange = paneRequest.getColumnRange();

		out.append("var visiblePane = new Object();");
		out.append("visiblePane.rowRange = new Object();");
		out.append("visiblePane.rowRange.firstIndex = " + rowRange.getFirstIndex() + ";");
		out.append("visiblePane.rowRange.lastIndex = " + rowRange.getLastIndex() + ";");
		out.append(
			"visiblePane.rowRange.forcedVisibleIndexInRange = " + rowRange.getForcedVisibleIndexInRange() + ";");

		out.append("visiblePane.columnRange = new Object();");
		out.append("visiblePane.columnRange.firstIndex = "
			+ TableUtil.getClientColumnIndex(model, columnRange.getFirstIndex()) + ";");
		out.append("visiblePane.columnRange.lastIndex = "
			+ TableUtil.getClientColumnIndex(model, columnRange.getLastIndex()) + ";");
		out.append("visiblePane.columnRange.forcedVisibleIndexInRange = "
			+ TableUtil.getClientColumnIndex(model, columnRange.getForcedVisibleIndexInRange()) + ";");
	}

	private static void appendViewportState(Appendable out, TableViewModel model, ViewportState state)
			throws IOException {
		RowIndexAnchor rowAnchor = state.getRowAnchor();
		ColumnAnchor columnAnchor = state.getColumnAnchor();

		int columnIndex = TableUtil.getClientColumnIndex(model, model.getColumnIndex(columnAnchor.getColumnName()));

		out.append("var viewportState = new Object();");
		out.append("viewportState.rowAnchor = new Object();");
		out.append("viewportState.rowAnchor.index = " + rowAnchor.getIndex() + ";");
		out.append("viewportState.rowAnchor.indexPixelOffset = " + rowAnchor.getIndexPixelOffset() + ";");

		out.append("viewportState.columnAnchor = new Object();");
		out.append("viewportState.columnAnchor.index = " + columnIndex + ";");
		out.append("viewportState.columnAnchor.indexPixelOffset = " + columnAnchor.getIndexPixelOffset() + ";");
	}

}
