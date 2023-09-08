/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

/**
 * {@link URLBuilder} can be used to encode a hierarchy of resources into a URL
 * for the client. The {@link #getURL() constructed URL} can be written to the
 * GUI. It will be decoded by a corresponding {@link URLParser}.
 * 
 * @see URLParser
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface URLBuilder {

	/**
	 * Adds the given resource to this URL builder. Adding a resource will
	 * typically called if some resource wants to dispatch a request to a sub
	 * resource.
	 * 
	 * @param resource
	 *        the sub resource to encode in the URL
	 */
	void addResource(CharSequence resource);

	/**
	 * Adds a custom parameter to the constructed URL.
	 * 
	 * @param name
	 *        The parameter name.
	 * @param value
	 *        the parameter value.
	 * @return This instance for call chaining.
	 */
	URLBuilder appendParameter(String name, CharSequence value);

	/**
	 * Adds a custom parameter to the constructed URL.
	 * 
	 * @param name
	 *        The parameter name.
	 * @param value
	 *        the parameter value.
	 * @return This instance for call chaining.
	 */
	URLBuilder appendParameter(String name, int value);

	/**
	 * Returns a representation of the currently composed URL.
	 * 
	 * @return a URL which represents the current state of the {@link URLBuilder}.
	 */
	String getURL();

	/**
	 * Creates a copy of this {@link URLBuilder}.
	 */
	URLBuilder copy();
}
