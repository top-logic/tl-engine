/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.io.PrintWriter;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.Resources;

/**
 * Write a JScript function to submit the form for the given command.
 * 
 * @author    <a href=mailto:fsc@top-logic.com>fsc</a>
 */
public interface CommandScriptWriter {

	public void writeCommandScript(String aPath, Resources aRes, PrintWriter anOut, LayoutComponent aComponent,
			CommandHandler aCommand);
}
