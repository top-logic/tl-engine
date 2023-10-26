/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.log.line;

import java.io.PrintStream;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Prints the selected {@link LogLine} to the console.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class PrintLogLineCommand extends AbstractCommandHandler {

	/** {@link ConfigurationItem} for the {@link PrintLogLineCommand}. */
	public interface Config extends AbstractCommandHandler.Config {

		@Override
		@StringDefault(CommandHandlerFactory.EXPORT_BUTTONS_GROUP)
		String getClique();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.EXPORT_NAME)
		CommandGroupReference getGroup();

		@Override
		@FormattedDefault(CommandHandler.TargetConfig.TARGET_SELECTION_SELF)
		ModelSpec getTarget();

	}

	/** {@link TypedConfiguration} constructor for {@link PrintLogLineCommand}. */
	public PrintLogLineCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		/* Print to "System.out", not "System.err", as it is being printed there on someone's
		 * command. It should not trigger a "System.err" observer that notifies the administrators
		 * that something is wrong. */
		print(System.out, (LogLine) model);
		InfoService.showInfo(I18NConstants.LOG_LINE_PRINTED_TO_CONSOLE);
		return HandlerResult.DEFAULT_RESULT;
	}

	private void print(PrintStream out, LogLine logLine) {
		/* When an administrator reads the console, they should know where this message is coming
		 * from. It might be an error printed to the console by another administrator. And that
		 * error on the console should not cause the former administrator to needlessly investigate
		 * its origin or even alarm other people. */
		out.println("Log entry printed from the log viewer:");
		out.println("--------------------------------------");
		out.println(logLine.getMessage());
		out.println(logLine.getDetails());
	}

}
