/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime;

import java.util.List;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.action.ConditionalAction;
import com.top_logic.layout.scripting.action.DynamicAction;
import com.top_logic.layout.scripting.runtime.action.ApplicationActionOp;

/**
 * User session in an application under test.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ApplicationSession {

	/**
	 * Processes the given {@link ApplicationActionOp} in the context of this
	 * {@link ApplicationSession}.
	 * 
	 * @return The result computed by the given {@link ApplicationAction}, see e.g.
	 *         {@link ConditionalAction#getCondition()}.
	 */
	public Object process(ApplicationAction action);

	/**
	 * Resolves the concrete actions of the given {@link DynamicAction} in the context of the
	 * current session.
	 * 
	 * @param action
	 *        The {@link DynamicAction} to resolve.
	 * @return The concrete actions produced by the given {@link DynamicAction}.
	 */
	public List<ApplicationAction> resolve(DynamicAction action);

	/**
	 * Invalidates this session.
	 * 
	 * <p>
	 * No {@link ApplicationAction} can be sent to
	 * {@link #process(ApplicationAction)} after invalidation.
	 * </p>
	 */
	public void invalidate();

	/**
	 * Trigger rendering of the UI.
	 * 
	 * <p>
	 * This is equivalent to the F5 action in the browser.
	 * </p>
	 * 
	 * TODO: This is a preliminary API, since it is not yet possible to do any
	 * interactions with the rendered UI.
	 */
	void render();
	
	/**
     * Call this as soon as you do not need a session any more.
     * 
     * This simulates a logout / timeout of Session. After this
     * call the ApplictionSession must not be used any more. 
     */
	public void tearDown();

	/**
	 * The {@link TLSubSessionContext} used to evaluate {@link ApplicationAction}.
	 */
	TLSubSessionContext getSubSession();

}
