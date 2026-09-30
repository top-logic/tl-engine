/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.instances.export;

import com.top_logic.model.instance.exporter.XMLInstanceExporter;

/**
 * Custom operation that is performed on a {@link XMLInstanceExporter} operation.
 */
public interface ExportCustomization {
	/**
	 * Operation done during instance export.
	 * 
	 * @param exporter
	 *        The currently running export.
	 */
	void perform(XMLInstanceExporter exporter);
}