/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.progress;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractSystemCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Cancel a running {@link ProgressInfo} and close its dialog.
 * 
 * @see CancelProgressHandler
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CancelProgressImmediatelyHandler extends AbstractSystemCommand {

	public CancelProgressImmediatelyHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext,
			LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {

		((AbstractProgressComponent) aComponent).getInfo().signalStop();
		
		HandlerResult result = new HandlerResult();
		result.setCloseDialog(true);
		return result;
	}

}
