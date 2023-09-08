/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action.assertion;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.assertion.ValueAssertion;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * Asserts that the given {@link ValueAssertion} holds.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class ValueAssertionOp extends AbstractApplicationActionOp<ValueAssertion> {

	/**
	 * Configuration constructor for {@link ValueAssertionOp}.
	 */
	public ValueAssertionOp(InstantiationContext context, ValueAssertion config) {
		super(context, config);
	}

	@Override
	public final Object processInternal(ActionContext actionContext, Object argument) {
		ValueAssertion action = config;
		Object expected = actionContext.resolve(action.getExpectedValue());
		ModelName actualSpec = action.getActualValue();
		Object actual = actionContext.resolve(actualSpec);

		boolean inverted = action.isInverted();
		if (action.getComparision().compare(action, actual, expected) == inverted) {
			Class<?> actualClass = actual == null ? null : actual.getClass();
			Class<?> expectedClass = expected == null ? null : expected.getClass();
			boolean sameClass = (CollectionUtil.equals(actualClass, expectedClass));
			StringBuilder msg = new StringBuilder();
			msg.append("The actual value '");
			if (sameClass) {
				msg.append(StringServices.debugValue(actual));
			} else {
				msg.append(StringServices.debug(actual));
			}
			msg.append("' ");
			if (!inverted) {
				msg.append("not ");
			}
			msg.append(action.getComparision().getDescription());
			msg.append(" the ");
			msg.append(inverted ? "unexpected" : "expected");
			msg.append(" value '");
			if (sameClass) {
				msg.append(StringServices.debugValue(expected));
			} else {
				msg.append(StringServices.debug(expected));
			}
			msg.append("'.");
			ApplicationAssertions.fail(action, msg.toString());
		}
		return argument;
	}

}
