/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.compact;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandHandler} opening the dialog that compacts the history of the application.
 *
 * @implNote Opens a {@link CompactHistoryDialog}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CompactHistoryCommand extends AbstractCommandHandler {

	/**
	 * Identifier under which an instance of this class is configured.
	 */
	public static final String COMMAND_ID = "compactHistory";

	/**
	 * Width of the opened dialog in pixels.
	 */
	private static final int DIALOG_WIDTH = 600;

	/**
	 * Height of the opened dialog in pixels.
	 */
	private static final int DIALOG_HEIGHT = 400;

	/**
	 * Creates a {@link CompactHistoryCommand} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CompactHistoryCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {
		return new CompactHistoryDialog(DisplayDimension.px(DIALOG_WIDTH), DisplayDimension.px(DIALOG_HEIGHT))
			.open(context);
	}

}
