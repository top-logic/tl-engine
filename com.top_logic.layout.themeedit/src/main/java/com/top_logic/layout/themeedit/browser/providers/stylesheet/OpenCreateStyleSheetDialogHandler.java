/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.stylesheet;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.gui.Theme;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Creates a new stylesheet for the given {@link Theme}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven F�rster</a>
 */
public class OpenCreateStyleSheetDialogHandler extends AbstractCommandHandler {

	/**
	 * Creates a {@link OpenCreateStyleSheetDialogHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public OpenCreateStyleSheetDialogHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {
		return new CreateStylesheetDialog(component).open(context);
	}


}
