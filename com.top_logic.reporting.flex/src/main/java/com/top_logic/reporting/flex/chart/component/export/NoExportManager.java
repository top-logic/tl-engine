/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component.export;

import java.util.Map;

import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.export.AbstractOfficeExportHandler.OfficeExportValueHolder;

/**
 * Implementation of {@link ExportManager} that does not register an export command.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class NoExportManager implements ExportManager {

	/**
	 * Singleton <code>INSTANCE</code> of NoExportManager
	 */
	public static NoExportManager INSTANCE = new NoExportManager();

	private NoExportManager() {
	}

	@Override
	public String getTemplatePath() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDownloadLabel(LayoutComponent component) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getExportHandler() {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public OfficeExportValueHolder getExportValues(DefaultProgressInfo progressInfo, Map arguments,
			LayoutComponent caller) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void registerExportCommand(CommandRegistry registry) {
		// Ignore.
	}

}