/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export.tableview;

import com.top_logic.basic.AbortExecutionException;

/**
 * What a running {@link TableViewExcelExport} reports its progress to, and asks whether it is to
 * stop.
 */
public interface ExportMonitor {

	/**
	 * A monitor that follows nothing and never stops the export.
	 */
	ExportMonitor NONE = new ExportMonitor() {
		@Override
		public void progress(int done, int total) {
			// Nobody follows.
		}

		@Override
		public void checkCancelled() {
			// Never stopped.
		}
	};

	/**
	 * Reports how many of the rows to export are written.
	 *
	 * @param done
	 *        The number of rows written so far.
	 * @param total
	 *        The number of rows the export writes.
	 */
	void progress(int done, int total);

	/**
	 * Stops the export, if it was asked to stop.
	 *
	 * @throws AbortExecutionException
	 *         If the export is to stop.
	 */
	void checkCancelled() throws AbortExecutionException;

}
