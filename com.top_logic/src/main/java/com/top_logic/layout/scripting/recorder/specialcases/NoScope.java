/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.specialcases;

import com.top_logic.basic.col.Maybe;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ReferenceFactory;
import com.top_logic.layout.scripting.recorder.ref.ValueResolver;
import com.top_logic.layout.scripting.recorder.ref.value.object.ContextRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.LabeledValue;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ValueScope} that resolves without any context.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class NoScope implements ValueScope {

	public static final ValueScope INSTANCE = new NoScope();

	@Override
	public Object resolveReference(ActionContext actionContext, ContextRef valueRef) {
		return ValueResolver.resolveLabeledValue(actionContext, null, null, cast(valueRef));
	}

	private LabeledValue cast(ContextRef valueRef) {
		try {
			return (LabeledValue) valueRef;
		} catch (ClassCastException ex) {
			throw (ClassCastException) new ClassCastException(valueRef.getConfigurationInterface().getName()
				+ " cannot be cast to " + LabeledValue.class.getName()).initCause(ex);
		}
	}

	@Override
	public Maybe<? extends ModelName> buildReference(Object target) {
		Maybe<? extends ContextRef> localValue = ReferenceFactory.localValue(target, null);
		if (localValue.hasValue() && !(localValue.get() instanceof LabeledValue)) {
			return Maybe.none();
		}
		return localValue;
	}

}