/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config;

import java.util.Collection;
import java.util.Optional;

import com.top_logic.basic.treexf.FactoryResolver;
import com.top_logic.layout.DisplayContext;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * Plug-in to the TL-Script function registry.
 * 
 * <p>
 * A {@link MethodResolver} provides implementation details for TL-Script functions.
 * </p>
 * 
 * @see SearchBuilder.Config#getMethodResolvers()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface MethodResolver extends FactoryResolver {

	/**
	 * Find a {@link MethodBuilder} for a function with the given name.
	 * 
	 * @return The {@link MethodBuilder} for the TL-Script function with the given name, or
	 *         <code>null</code>, if this resolver does not provide a builder for funtions with the
	 *         given name.
	 */
	MethodBuilder<?> getMethodBuilder(String functionName);

	/**
	 * All function names of functions defined by this resolver.
	 */
	Collection<String> getMethodNames();

	/**
	 * Retrieved the documentation for the given method with the given function. The returned value
	 * is an HTML snipplet describing the function and its parameters.
	 * 
	 * @param context
	 *        Rendering context.
	 * @param functionName
	 *        Name of the function to get documentation for.
	 * 
	 * @return Documentation for the specified function, if any, regardless of whether there is no
	 *         function with that name or the function exists but has no documentation.
	 */
	Optional<String> getDocumentation(DisplayContext context, String functionName);

}
