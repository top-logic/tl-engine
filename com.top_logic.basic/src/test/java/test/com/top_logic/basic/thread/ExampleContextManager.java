/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.thread;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.thread.BasicThreadContextManager;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;

/**
 * Example class to show how to change {@link ThreadContextManager} and create other
 * {@link ThreadContext}s.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ExampleContextManager extends BasicThreadContextManager {

	/**
	 * Configuration for {@link ExampleContextManager}
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends ThreadContextManager.Config {

		/**
		 * Configuration for created {@link ExampleContext}s.
		 */
		ExampleContext.Config getContextConfig();
	}

	private ExampleContext.Config _contextConfig;

	/**
	 * Creates a new ExampleContextManager.
	 */
	public ExampleContextManager(InstantiationContext context, Config configuration) {
		super(context, configuration);
		_contextConfig = configuration.getContextConfig();
	}

	@Override
	protected ThreadContext internalNewSubSessionContext() {
		return new ExampleContext(_contextConfig);
	}

}
