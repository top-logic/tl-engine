/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.base.services.simpleajax.ClientAction;

/**
 * The class {@link Validator} is an interface which must only be used by the framework. It is just
 * created to validate some object using some {@link ControlScope} as validation scope, i.e. the
 * methods in this interface must just be used in the method
 * {@link DisplayContext#validateScoped(ControlScope, Validator, UpdateQueue, Object)}.
 * 
 * @param <T>
 *        the type of objects which can be validated by this validator.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Validator<T> {

	/**
	 * This method validates the given <code>obj</code> and appends the resulting
	 * {@link ClientAction}s to the given {@link UpdateQueue}.
	 * <p>
	 * <b>This method must just be called from
	 * {@link DisplayContext#validateScoped(ControlScope, Validator, UpdateQueue, Object)}</b>
	 * </p>
	 * 
	 * @param context
	 *            the context to validate the given <code>obj</code>
	 * @param queue
	 *            the {@link UpdateQueue} to append the validation actions
	 * @param obj
	 *            the Object to validate
	 */
	public void validate(DisplayContext context, UpdateQueue queue, T obj);

}
