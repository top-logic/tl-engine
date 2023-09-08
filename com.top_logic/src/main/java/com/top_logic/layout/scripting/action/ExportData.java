/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.action.ExportDataOp;

/**
 * Configuration for {@link ExportDataOp}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ExportData extends ApplicationAction {

	/**
	 * The file name to export the {@link #getData()} value to.
	 */
	String getExportFileName();

	/**
	 * Description of the {@link BinaryData} data to export from the application under test.
	 */
	ModelName getData();

}
