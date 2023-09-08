/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.progress;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.AbstractSystemCommand;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;

/**
 * {@link AbstractCommandHandler} that stops a current progress.
 * 
 * @see CancelProgressImmediatelyHandler
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CancelProgressHandler extends AbstractSystemCommand {

	/**
	 * Configuration of the {@link CancelProgressHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractSystemCommand.Config {

		@Override
		@ListDefault({ ProgressStoppedExecutability.class })
		List<PolymorphicConfiguration<? extends ExecutabilityRule>> getExecutability();

	}

	/**
	 * Creates a new {@link CancelProgressHandler} from the given configuration.
	 */
	public CancelProgressHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
			Object model, Map<String, Object> someArguments) {

		((AbstractProgressComponent) aComponent).getInfo().signalStop();

		/* don't close dialog here because this is handled by the component that works on the long
		 * running action. This component reacts on the "shouldStop"-parameter in its
		 * DefaultProgressInfo and sets "finished"-flag of this progress info. This will in turn
		 * lead to closing the dialog on next refresh cycle. */
		return HandlerResult.DEFAULT_RESULT;
	}

}
