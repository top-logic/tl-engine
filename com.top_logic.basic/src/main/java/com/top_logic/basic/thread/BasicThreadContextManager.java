/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.thread;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.DefaultInteractionContext;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.SessionContext;
import com.top_logic.basic.SimpleSessionContext;
import com.top_logic.basic.config.InstantiationContext;

/**
 * Basic implementation of {@link ThreadContextManager}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class BasicThreadContextManager extends ThreadContextManager {

	/**
	 * Creates a new {@link BasicThreadContextManager}.
	 */
	public BasicThreadContextManager(InstantiationContext context, Config configuration) {
		super(context, configuration);
	}

	@Override
	protected ThreadContext internalNewSubSessionContext() {
		return new ThreadContext();
	}

	@Override
	public InteractionContext internalNewInteraction(ServletContext servletContext, HttpServletRequest request,
			HttpServletResponse response) {
		return new DefaultInteractionContext();
	}

	@Override
	public SessionContext internalNewSessionContext() {
		return new SimpleSessionContext();
	}

}

