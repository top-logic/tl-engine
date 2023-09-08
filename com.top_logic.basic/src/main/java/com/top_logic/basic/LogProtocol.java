/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;


/**
 * {@link Protocol} logging messages to the {@link Logger}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LogProtocol extends AbstractProtocol {

	/**
	 * The source that should be handed to {@link Logger}.
	 * 
	 * @see Logger#info(String, Object)
	 */
	private final Class<?> logSource;

	/**
	 * Creates a {@link LogProtocol}.
	 * 
	 * @param logSource
	 *        The source that should be handed to {@link Logger}, see the second
	 *        argument of {@link Logger#info(String, Object)}.
	 * @param chain
	 *        See {@link AbstractProtocol#AbstractProtocol(Protocol)}.
	 *        
	 * @since TL_5_6_1
	 */
	public LogProtocol(Class<?> logSource, Protocol chain) {
		super(chain);
		
		this.logSource = logSource;
	}

	/**
	 * Creates a {@link LogProtocol}.
	 *
	 * @param logSource
	 *        The source that should be handed to {@link Logger}, see the second
	 *        argument of {@link Logger#info(String, Object)}.
	 */
	public LogProtocol(Class<?> logSource) {
		this(logSource, NoProtocol.INSTANCE);
	}
	
	@Override
	protected void reportError(String message, Throwable ex) {
		Logger.error(enhance(message), ex, logSource);
	}

	@Override
	protected RuntimeException reportFatal(String message, Throwable ex) {
		String enhancedMessage = enhance(message);
		Logger.error(enhancedMessage, ex, logSource);
		return new AbortExecutionException(enhancedMessage, ex);
	}
	
	@Override
	protected RuntimeException createAbort() {
		return new AbortExecutionException("Aborting execution due to previous errors", getFirstProblem());
	}

	@Override
	public void localInfo(String message, int verbosityLevel) {
		switch (verbosityLevel) {
			case WARN:
				Logger.warn(enhance(message), logSource);
				break;
			case INFO:
				Logger.info(enhance(message), logSource);
				break;
		case VERBOSE:
		case DEBUG:
			Logger.debug(enhance(message), logSource);
			break;
		}
	}

	/**
	 * Hook for sub-classes to modifiy the logged message.
	 */
	protected String enhance(String message) {
		return message;
	}

}