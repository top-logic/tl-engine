/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.EventWriter;

/**
 * Proxy for an {@link EventRewriter}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class EventRewriterProxy<C extends EventRewriterProxy.Config<?>> extends AbstractConfiguredInstance<C>
		implements EventRewriter {

	/**
	 * Typed configuration interface definition for {@link EventRewriterProxy}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config<I extends EventRewriterProxy<?>> extends PolymorphicConfiguration<I> {
		// Configuration for EventRewriterProxy
	}

	private final EventRewriter _impl;

	/**
	 * Create a {@link EventRewriterProxy}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public EventRewriterProxy(InstantiationContext context, C config) {
		super(context, config);
		_impl = createImplementation(context, config);
	}

	/**
	 * Creates the actual implementation to delegate {@link #rewrite(ChangeSet, EventWriter)} to.
	 * 
	 * <p>
	 * This method is called from constructor.
	 * </p>
	 * 
	 * @param context
	 *        Context in which this proxy is created.
	 * @param config
	 *        Configuration for this proxy.
	 * @return {@link EventRewriter} to dispatch {@link #rewrite(ChangeSet, EventWriter)} to.
	 */
	protected abstract EventRewriter createImplementation(InstantiationContext context, C config);

	@Override
	public void rewrite(ChangeSet cs, EventWriter out) {
		_impl.rewrite(cs, out);
	}

}

