/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.connect;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContextManager;

/**
 * An abstract {@link Thread} extension for all kafka connectors.
 * 
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
public abstract class ConnectThread extends Thread {
	
	/**
	 * Typed configuration interface definition for {@link ConnectThread}.
	 * 
	 * @author <a href="mailto:wta@top-logic.com">wta</a>
	 */
	public interface Config extends PolymorphicConfiguration<ConnectThread>, NamedConfigMandatory {
		// configuration interface definition
	}

	/**
	 * @see #getConfig()
	 */
	private final Config _config;
	
	/**
	 * Create a {@link ConnectThread}.
	 * 
	 * @param context
	 *            the {@link InstantiationContext} to create the new object in
	 * @param config
	 *            the configuration object to be used for instantiation
	 */
	public ConnectThread(final InstantiationContext context, final Config config) {
		super(config.getName());
		
		_config = config;
	}
	
	/**
	 * Request thread termination.
	 * 
	 * <p>
	 * <b>Note: </b>Has no effect if thread termination has already been
	 * requested or this thread has already been terminated.
	 * </p>
	 */
	public abstract void terminate();
	
	@Override
	public void run() {
		ThreadContextManager.inSystemInteraction(getClass(), new InContext() {
			@Override
			public void inContext() {
				startup();
				execute();
				shutdown();
			}
		});
	}
	
	/**
	 * Initialize this {@link ConnectThread} before running.
	 */
	protected void startup() {
		// does nothing
	}
	
	/**
	 * Cleanup this {@link ConnectThread} after execution has stopped.
	 */
	protected void shutdown() {
		// does nothing
	}

	/**
	 * Run connector routines.
	 * 
	 * <p><b>Note: </b>Implementing classes have to react to changes of #terminate()</p>
	 */
	protected abstract void execute();
	
	/**
	 * the {@link Config} this connector was instantiated with
	 */
	protected Config getConfig() {
		return _config;
	}
	
	/**
	 * Close the given resource.
	 * 
	 * @param closeable
	 *            the {@link AutoCloseable} to be closed
	 */
	protected void close(final AutoCloseable closeable) {
		final int max = 10;
		
		int tries = max;
		boolean closed = false;
		Throwable error = null;
		
		while(!closed && tries-- > 0) {
			try {
				closeable.close();
				closed = true;
			} catch (Throwable e) {
				// remember the error
				error = e;
			}
		}
		
		// log an error message to indicate that the given
		// resource could not be successfully closed after
		// exceeding the maximum number of retries.
		if(!closed) {
			Logger.error(String.format("%s: failed to close resource", getConfig().getName()), error, ConnectThread.class);
		}
	}
	
	/**
	 * Delay {@link #execute() execution} for the specified number of
	 * milliseconds.
	 * 
	 * @param delay
	 *            the delay in milliseconds
	 */
	protected void delay(final long delay) {
		final long wakeup = System.currentTimeMillis() + delay;
		long time = delay;
		
		while(System.currentTimeMillis() < wakeup) {
			try {
				Thread.sleep(time);
			} catch (final InterruptedException e) {
				time = wakeup - System.currentTimeMillis();
			}
		}
	}
}
