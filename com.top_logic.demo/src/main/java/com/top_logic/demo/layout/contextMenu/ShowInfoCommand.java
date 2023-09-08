/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.contextMenu;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.demo.model.types.A;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Demo for a type-specific context menu entry.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ShowInfoCommand extends AbstractCommandHandler {

	/**
	 * Creates a {@link ShowInfoCommand}.
	 */
	public ShowInfoCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		InfoService.showInfo(Fragments.text("Info: " + ((A) model).getName()));
		return HandlerResult.DEFAULT_RESULT;
	}

}
