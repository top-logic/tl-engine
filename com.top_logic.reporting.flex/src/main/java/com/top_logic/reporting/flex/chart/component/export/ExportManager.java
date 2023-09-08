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
import com.top_logic.tool.export.ExportAware;


/**
 * Use this manager to prevent code-duplication. It combines methods are mostly copied to numerous
 * {@link ExportAware}-components.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public interface ExportManager {

	/**
	 * The template-file for the export
	 */
	public String getTemplatePath();

	/**
	 * The download-name of the exported file
	 */
	public String getDownloadLabel(LayoutComponent component);

	/**
	 * The ID of the export-handler, or <code>null</code> to disable export.
	 */
	public String getExportHandler();

	/**
	 * Creates the actual export-data, see
	 * {@link ExportAware#getExportValues(DefaultProgressInfo, Map)}
	 */
	@SuppressWarnings("rawtypes")
	public OfficeExportValueHolder getExportValues(DefaultProgressInfo progressInfo, Map arguments, LayoutComponent caller);

	/**
	 * Registers the command that is used.
	 * 
	 * @param registry
	 *        {@link CommandRegistry} to register the export command.
	 */
	void registerExportCommand(CommandRegistry registry);

}
