/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import java.util.List;

import com.top_logic.layout.Renderer;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.control.TableUpdateAccumulator.UpdateRequest;

/**
 * {@link Renderer} for a {@link TableControl}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ITableRenderer extends ControlRenderer<TableControl> {

	/**
	 * The row number of a client-side HTML element representing that row.
	 * 
	 * @param rowId
	 *        The HTML ID value of a client-side row element.
	 * @return The corresponding row number in the {@link TableViewModel}.
	 */
	int getRow(String rowId);

	/**
	 * Produces client-side updates for the rows identified by the given update requests and adds
	 * them to the given {@link UpdateQueue}.
	 * 
	 * @param view
	 *        The {@link TableControl} being updated.
	 *
	 * @param updateRequests
	 *        Identifiers for the rows to update.
	 * @param actions
	 *        {@link UpdateQueue} to add the produced update actions to.
	 */
	void updateRows(TableControl view, List<UpdateRequest> updateRequests, UpdateQueue actions);

}
