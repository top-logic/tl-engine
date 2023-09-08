/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * {@link ThreadContextManager} creating {@link ThreadContext} for applications with more than one
 * {@link KnowledgeBase}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MultiKBContextManager extends TLContextManager {

	/**
	 * Creates a new {@link MultiKBContextManager}.
	 */
	public MultiKBContextManager(InstantiationContext context, Config configuration) {
		super(context, configuration);
	}

	@Override
	public ThreadContext internalNewSubSessionContext() {
		return new MultiKBContext();
	}

	@Override
	public InteractionContext internalNewInteraction(ServletContext servletContext, HttpServletRequest request,
			HttpServletResponse response) {
		return new MultiKBInteraction(servletContext, request, response);
	}

}

