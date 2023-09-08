/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.tooling;

import java.io.File;

import com.top_logic.basic.core.log.Log;

/**
 * Convention of the layout of files and directories within a <i>TopLogic</i> module.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ModuleLayout extends com.top_logic.basic.core.workspace.ModuleLayout {

	// For compatibility only.

	/**
	 * Creates a {@link ModuleLayout}.
	 */
	public ModuleLayout(Log log, File moduleDir) {
		super(log, moduleDir);
	}

}