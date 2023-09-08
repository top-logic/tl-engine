/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.function.Supplier;

import com.top_logic.basic.Log;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.config.annotation.Id;

/**
 * Context in which the instantiation of a configured class occurs.
 * 
 * <p>
 * The context is able to instantiate sub-components from their configurations and serves as error
 * reporting {@link Log}.
 * </p>
 * 
 * <p>
 * <b>Note:</b> All implementations must also implement {@link InstantiationContextSPI} and delegate
 * the {@link #getInstance(PolymorphicConfiguration)} method to
 * {@link InstantiationContextSPI#getInstance(InstantiationContext, PolymorphicConfiguration)}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface InstantiationContext extends Log {

	/**
	 * Special id that resolves to the outer instance of the specified scope type.
	 * 
	 * @see #resolveReference(Object, Class, ReferenceResolver)
	 */
	NamedConstant OUTER = new NamedConstant("outer");

	/**
	 * Instantiate or lookup the configured object from the given configuration
	 * object.
	 * 
	 * @param configuration
	 *        The configuration that should be instantiated.
	 * @return The configured object.
	 */
	<T> T getInstance(PolymorphicConfiguration<T> configuration);

	/** 
	 * Checks whether this {@link Log} has errors and terminates if it has.
	 */
	void checkErrors() throws ConfigurationException;

	/**
	 * Requests access to the instance instantiated from the configuration with the given
	 * identifier.
	 * 
	 * @param id
	 *        The identifier to resolve.
	 * @param scope
	 *        The type scope of identifiers to search in. Only identifiers qualify for resolution
	 *        that are defined by {@link Id} properties with the same type scope, see
	 *        {@link Id#value()}.
	 * @param setter
	 *        The callback to execute, when the instance with the given identifier becomes
	 *        available.
	 * @see Id
	 */
	<T> void resolveReference(Object id, Class<T> scope, ReferenceResolver<T> setter);

	/**
	 * Allow instantiating multiple components independently that may refer to each other using
	 * configuration references.
	 * 
	 * @param r
	 *        The function that may use this {@link InstantiationContext} for multiple independent
	 *        instantiations.
	 * @return The arbitrary result computed by the given function.
	 */
	<T> T deferredReferenceCheck(Supplier<T> r);

	/**
	 * Service method to return an {@link InstantiationContext} with the given {@link Log}.
	 * 
	 * <p>
	 * When the given {@link Log} is itself an {@link InstantiationContext}, it is returned,
	 * otherwise a new {@link DefaultInstantiationContext} with the given {@link Log} is returned.
	 * </p>
	 * 
	 * @param log
	 *        The {@link Log} to get an {@link InstantiationContext} for.
	 * 
	 * @return Either the given protocol it it is already an {@link InstantiationContext}, or a new
	 *         {@link DefaultInstantiationContext} with the given {@link Log}.
	 */
	static InstantiationContext toContext(Log log) {
		InstantiationContext context;
		if (log instanceof InstantiationContext) {
			context = (InstantiationContext) log;
		} else {
			context = new DefaultInstantiationContext(log);
		}
		return context;
	}
}
