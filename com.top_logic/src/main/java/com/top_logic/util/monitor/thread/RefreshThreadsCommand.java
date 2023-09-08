/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.monitor.thread;

import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Refresh the thread list.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class RefreshThreadsCommand extends AbstractCommandHandler {

	/**
	 * Creates a {@link RefreshThreadsCommand}.
	 */
	public RefreshThreadsCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model, Map<String, Object> arguments) {
		if (component instanceof TableComponent) {
			updateThreadInfos((TableComponent) component);
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Update the given table model by the current {@link ThreadData}s.
	 * 
	 * @param component
	 *        The component executing this command.
	 */
	protected void updateThreadInfos(TableComponent component) {
		EditableRowTableModel model = component.getTableModel();

		Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();

		boolean orderInvalid = false;
		// Update existing thread information.
		for (int n = model.getRowCount() - 1; n >= 0; n--) {
			ThreadData info = (ThreadData) model.getRowObject(n);
			Thread thread = info.getThread();

			StackTraceElement[] trace = traces.remove(thread);
			if (trace != null) {
				if (info.update(trace)) {
					model.updateRows(n, n);
					orderInvalid = true;
				}
			} else {
				model.removeRow(n);
			}
		}

		// Add new threads.
		for (Entry<Thread, StackTraceElement[]> entry : traces.entrySet()) {
			model.addRowObject(new ThreadData(entry.getKey(), entry.getValue()));
			orderInvalid = true;
		}

		if (orderInvalid) {
			component.getTableData().getViewModel().reapplyOrder();
		}
	}
}
