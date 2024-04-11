/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure.toolrow;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.common.webfolder.ui.clipboard.ClipboardDialog;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Provides a command to open the {@link ClipboardDialog}.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class ClipboardCommandModel extends AbstractCommandModel {
	
	private LayoutComponent component;

	/**
	 * Creates a {@link ClipboardCommandModel}.
	 */
	@CalledByReflection
	public ClipboardCommandModel(LayoutComponent aComponent) {
		this.component = aComponent;
	}

	@Override
	protected HandlerResult internalExecuteCommand(DisplayContext context) {
		ClipboardDialog.createDialog(component).open(context);
		return HandlerResult.DEFAULT_RESULT;
	}
	
}

