/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

/**
 * A {@link URLParser} decodes a URL formerly constructed by a corresponding
 * {@link URLBuilder}.
 * 
 * <p>
 * The resources returned by {@link #getResource()} or {@link #removeResource()}
 * have the inverse order as in the corresponding {@link URLBuilder}, i.e. the
 * first returned resource represents the top most resource, the second one is
 * the direct child in the hierarchy encoded by the {@link URLBuilder}, and so
 * on.
 * </p>
 * 
 * @see URLBuilder
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface URLParser {

	/**
	 * Removes the current resource from the parser and returns it. After
	 * calling this method, the same {@link URLParser} can be given to some sub
	 * resource in the represented hierarchy.
	 * 
	 * <p>
	 * Must not be called if {@link #isEmpty()} returns <code>false</code>.
	 * </p>
	 * 
	 * @return the current resource
	 * 
	 * @see #getResource()
	 */
	String removeResource();

	/**
	 * Returns the current resource without removing it.
	 * 
	 * <p>
	 * Must not be called if {@link #isEmpty()} returns <code>false</code>.
	 * </p>
	 * 
	 * @see #removeResource()
	 */
	String getResource();

	/**
	 * Decides whether there is a current resource.
	 * 
	 * @return true iff {@link #removeResource()} or {@link #getResource()} can
	 *         be called.
	 */
	boolean isEmpty();
}
