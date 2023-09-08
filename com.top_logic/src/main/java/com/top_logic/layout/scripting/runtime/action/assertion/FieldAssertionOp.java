/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action.assertion;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.action.assertion.FieldAssertion;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * The base class for all assertions about a single {@link FormField}. It asserts for the existence
 * of the field before it delegates to the subclass asserts.
 * <p>
 * This class is not abstract for compatibility with existing scripts.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class FieldAssertionOp<S extends FieldAssertion> extends FormAssertionOp<S> {

	/**
	 * Creates a {@link FieldAssertionOp} from configuration.
	 */
	public FieldAssertionOp(InstantiationContext context, S config) {
		super(context, config);
	}

	@Override
	protected final void checkAsserts(FormMember field, ActionContext actionContext) {
		checkAsserts((FormField) field, actionContext);
	}

	/**
	 * Subclasses should override this method to implement their assertions about the field. If the
	 * field does not exist (field == null) this method is not called but instead an
	 * ApplicationAssertion is thrown.
	 * 
	 * @param field
	 *        Is guaranteed to be not <code>null</code>.
	 * @param actionContext
	 *        Is guaranteed to be not <code>null</code>.
	 */
	protected void checkAsserts(FormField field, ActionContext actionContext) {
		// Not abstract, as the class can not be abstract.
	}

}
