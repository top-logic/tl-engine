/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.message;

/**
 * A template of an internationalizable {@link Message}.
 * 
 * <p>
 * A {@link Template} is instantiated by declaring a <code>public static</code>
 * variable in a <code>Messages</code> class that subclasses
 * {@link AbstractMessages}.
 * </p>
 * 
 * <p>
 * <b>Note:</b> This interface <b>must not</b> be implemented by application
 * code.
 * </p>
 * 
 * @see AbstractMessages
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Template {

	/**
	 * The number of arguments, this {@link Template} supports.
	 */
	int getParameterCount();

	/**
	 * The name of the scope, this {@link Template} lives in.
	 * 
	 * <p>
	 * May be <code>null</code>, if this messages is defined in the global
	 * namespace.
	 * </p>
	 */
	String getNameSpace();

	/**
	 * The local name of this {@link Template} that makes it unique within its
	 * {@link #getNameSpace()}.
	 */
	String getLocalName();

}