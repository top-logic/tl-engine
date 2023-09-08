/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.function.Supplier;

import com.top_logic.basic.annotation.FrameworkInternal;

/**
 * Service provider interface for implementing {@link InstantiationContext}.
 * 
 * <p>
 * All implementations of {@link InstantiationContext} must de-facto implement
 * {@link InstantiationContextSPI} to allow adapter implementations passing the correct outter
 * context to the typed configuration constructors.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface InstantiationContextSPI extends InstantiationContext {

	/**
	 * Instantiate or lookup the configured object from the given configuration object.
	 * 
	 * @param self
	 *        The outer {@link InstantiationContext} instance to pass to configuration constructors.
	 * @param configuration
	 *        The configuration that should be instantiated.
	 * @return The configured object.
	 */
	@FrameworkInternal
	<T> T getInstance(InstantiationContext self, PolymorphicConfiguration<T> configuration);

	/**
	 * Implementation of {@link #deferredReferenceCheck(Supplier)}.
	 *
	 * @param self
	 *        The outer {@link InstantiationContext} instance to pass to configuration constructors.
	 * @param r
	 *        See {@link #deferredReferenceCheck(Supplier)}
	 * @return See {@link #deferredReferenceCheck(Supplier)}
	 */
	<T> T deferredReferenceCheck(InstantiationContext self, Supplier<T> r);
}
