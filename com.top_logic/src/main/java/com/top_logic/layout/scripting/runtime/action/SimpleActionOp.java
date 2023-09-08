/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.ApplicationSession;

/**
 * Simple (not configured) application action implementation.
 * 
 * <p>
 * Implementations must be public classes and have a public no-argument constructor. If an
 * implementation is an inner class, it must be static.
 * </p>
 * 
 * @see ActionFactory#simpleAction(Class)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SimpleActionOp {

	/**
	 * Perform the test in the context of an {@link ApplicationSession}.
	 * 
	 * @param context
	 *        The context of the action.
	 * @throws Exception
	 *         Any exception that may happen during test.
	 */
	void performTest(ActionContext context) throws Exception;
	
}