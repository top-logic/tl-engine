/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.specialcases;

import java.util.Collection;

import com.top_logic.basic.col.Maybe;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ValueNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.value.object.ContextRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.NamedValue;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.table.model.EditableRowTableModel;

/**
 * {@link ValueScope} that identifies objects locally within a {@link Collection} of objects (e.g.
 * the row object of an {@link EditableRowTableModel}.
 * 
 * <p>
 * A {@link CollectionValueScope} used a configured {@link ValueNamingScheme} for computing an
 * identifying set of key value pairs for a target object.
 * </p>
 * 
 * <p>
 * A {@link ModelNamingScheme} is preferable over a {@link ValueNamingScheme}, if the target object
 * can be resolved without session context.
 * </p>
 * 
 * @see ModelNamingScheme
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class CollectionValueScope implements ValueScope {

	private final Maybe<ValueNamingScheme<?>> _customNameProvider;

	private final Collection<?> _objects;

	public CollectionValueScope(Collection<?> objects, ValueNamingScheme<?> customNameProvider) {
		_objects = objects;
		_customNameProvider = Maybe.<ValueNamingScheme<?>> toMaybe(customNameProvider);
	}

	@Override
	public Maybe<? extends ModelName> buildReference(Object value) {
		if (_customNameProvider.hasValue()) {
			if (_customNameProvider.get().supportsModel(value)) {
				@SuppressWarnings("unchecked") // Safe due to check above.
				ValueNamingScheme<Object> untypedNamingScheme = (ValueNamingScheme<Object>) _customNameProvider.get();
				Maybe<? extends ContextRef> namedValue = untypedNamingScheme.namedValueDirect(NamedValue.class, value);
				if (namedValue.hasValue()) {
					return namedValue;
				}
			}
		}
		return ValueNamingScheme.namedValue(NamedValue.class, value);
	}

	@Override
	public Object resolveReference(ActionContext actionContext, ContextRef valueName) {
		return ValueNamingScheme.resolveNamedValue(actionContext, _objects, (NamedValue) valueName);
	}

}
