/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.NamedModelRef;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * Compatibility to make {@link ValueRef} usable where a {@link ModelName} is required.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public class ValueRefNamingDirect extends ModelNamingScheme<Object, Object, ValueRef> {

	@Override
	public Class<ValueRef> getNameClass() {
		return ValueRef.class;
	}

	@Override
	public Class<Object> getModelClass() {
		return Object.class;
	}

	@Override
	public Class<Object> getContextClass() {
		return Object.class;
	}

	@Override
	public Maybe<ValueRef> buildName(Object valueContext, Object model) {
		Maybe<? extends ModelName> result = ReferenceFactory.legacyTryReferenceValue(model, valueContext);
		if (!result.hasValue()) {
			return Maybe.none();
		}
		ModelName valueRef = result.get();
		if (!(valueRef instanceof ValueRef)) {
			NamedModelRef ref = TypedConfiguration.newConfigItem(NamedModelRef.class);
			ref.setModelName(valueRef);
			return Maybe.some(ref);
		}
		return Maybe.some((ValueRef) valueRef);
	}

	@Override
	public Object locateModel(ActionContext context, Object valueContext, ValueRef name) {
		return new ValueResolver(valueContext).legacyResolveValueRef(name, context);
	}

}
