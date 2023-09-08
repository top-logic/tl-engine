/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.module;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;

/**
 * Managed Class to test the presence of a {@link ThreadContext} during creation, startup and
 * shutdown.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ServiceDependencies({
	ThreadContextManager.Module.class
})
public class TestedManagedClassWithThreadContext extends AbstractTestedManagedClass {

	boolean _threadContextOnCreation;

	boolean _threadContextOnStartUp;

	boolean _threadContextOnShutDown;

	/**
	 * Creates a new {@link TestedManagedClassWithThreadContext} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link TestedManagedClassWithThreadContext}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public TestedManagedClassWithThreadContext(InstantiationContext context, Config config)
			throws ConfigurationException {
		_threadContextOnCreation = ThreadContextManager.getSubSession() != null;
	}

	@Override
	protected void startUp() {
		super.startUp();
		_threadContextOnStartUp = ThreadContextManager.getSubSession() != null;
	}

	@Override
	protected void shutDown() {
		_threadContextOnShutDown = ThreadContextManager.getSubSession() != null;
		super.shutDown();
	}

}

