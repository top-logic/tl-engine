/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.meterware.servletunit;

import jakarta.servlet.ServletContext;

/**
 * Use {@link ServletContextFactory#create(WebApplication)}
 * whenever you have to create a new {@link ServletContext}.
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 * @version    $Revision: 253790 $  $Author: bhu $  $Date: 2018-05-06 10:50:34 +0200 (So, 06. Mai 2018) $
 */
public abstract class ServletContextFactory {

	private static final class DefaultServletContextFactory extends ServletContextFactory {
		
		@Override
		public ServletContext createInternal(WebApplication application) {
			return new ServletUnitServletContext(application);
		}
		
	}
	
	public static final ServletContextFactory DEFAULT_INSTANCE = new DefaultServletContextFactory();
	
	private static ServletContextFactory singleton = DEFAULT_INSTANCE;

	/**
	 * If you have special requirements for the {@link ServletContext}s that are used during testing,
	 * use this method to set your own {@link ServletContextFactory}.
	 * You can always access the default via {@link ServletContextFactory#DEFAULT_INSTANCE}
	 * 
	 * @see #getInstance()
	 * 
	 * @param newInstance Must not be <code>null</code>.
	 */
	public static final synchronized void setInstance(ServletContextFactory newInstance) {
		if (newInstance == null) {
			throw new NullPointerException();
		}
		singleton = newInstance;
	}

	/**
	 * The {@link ServletContextFactory} to be used for creating {@link ServletContext}s.
	 * 
	 * @return Never <code>null</code>
	 */
	public static final synchronized ServletContextFactory getInstance() {
		return singleton;
	}

	/**
	 * Create a new {@link ServletContext} for the given {@link WebApplication} with the
	 * {@link ServletContextFactory} registered under {@link ServletContextFactory#getInstance()}.
	 */
	public static ServletContext create(WebApplication application) {
		return getInstance().createInternal(application);
	}

	/**
	 * Internal variant for other {@link ServletContextFactory}s.
	 * Create a new {@link ServletContext} for the given {@link WebApplication} with this {@link ServletContextFactory}.
	 */
	public abstract ServletContext createInternal(WebApplication application);

}
