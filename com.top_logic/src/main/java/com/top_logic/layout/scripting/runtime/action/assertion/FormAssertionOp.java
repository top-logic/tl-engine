/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action.assertion;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.action.assertion.FormAssertion;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractFormActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertion;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * The base class for all assertions about a single {@link FormMember}. It asserts for the existence
 * of the {@link FormMember} before it delegates to the subclass asserts. It can also be used
 * directly (not subclassed) if just the existence of the field should be checked.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class FormAssertionOp<S extends FormAssertion> extends AbstractFormActionOp<S> {

	/**
	 * Creates a {@link FormAssertionOp} from configuration.
	 */
	public FormAssertionOp(InstantiationContext context, S config) {
		super(context, config);
	}

	@Override
	public final Object processInternal(ActionContext actionContext, Object argument) {
		FormMember member = findMember(actionContext);
		ApplicationAssertions.assertNotNull(config, "Could not find the form member.", member);
		checkAsserts(member, actionContext);
		return argument;
	}

	/**
	 * Subclasses should override this method to implement their assertions about the
	 * {@link FormMember}. If it does not exist, this method is not called but instead an
	 * {@link ApplicationAssertion} is thrown.
	 * 
	 * @param formMember
	 *        Is guaranteed to be not <code>null</code>.
	 * @param actionContext
	 *        Is guaranteed to be not <code>null</code>.
	 */
	protected void checkAsserts(FormMember formMember, ActionContext actionContext) {
		/* No further asserts. This action checks just for the existence of the FormMember. But this
		 * is done before this method is called, to make sure subclasses overriding this method
		 * don't have to worry about null. */
	}

}
