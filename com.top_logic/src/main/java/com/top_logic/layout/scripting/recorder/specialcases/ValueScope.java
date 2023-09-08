/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.specialcases;

import com.top_logic.basic.col.Maybe;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.ContextRef;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * Algorithm to reference and resolve object through {@link ContextRef}s assuming some addition
 * context.
 * 
 * <p>
 * The additional context is encapsulated by the {@link ValueScope} itself. It is established during
 * the {@link ValueScope} construction through the {@link ValueScopeFactory}.
 * </p>
 * 
 * @see ValueScopeFactory
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface ValueScope {

	/**
	 * Builds a {@link ValueRef} to the target.
	 * 
	 * @param target
	 *        Is allowed to be <code>null</code>.
	 * 
	 * @return A reference of the target object, if this scope supports the target object.
	 */
	Maybe<? extends ModelName> buildReference(Object target);

	/**
	 * Retrieve the object that is under the given scope-local label path.
	 * 
	 * @param actionContext
	 *        The context of the currently running action.
	 * @param valueRef
	 *        A path consisting of labels that uniquely identifies an object within this scope. Can
	 *        be <code>null</code>.
	 * @return The identified object. If there is no binding under the given path, an
	 *         {@link AssertionError} is thrown.
	 */
	Object resolveReference(ActionContext actionContext, ContextRef valueRef);

}
