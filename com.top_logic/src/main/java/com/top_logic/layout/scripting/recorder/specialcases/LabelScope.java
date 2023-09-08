/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.specialcases;

import java.util.List;

import com.top_logic.basic.col.Maybe;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.scripting.recorder.ref.FuzzyValueFormat;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ReferenceFactory;
import com.top_logic.layout.scripting.recorder.ref.ValueNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ValueResolver;
import com.top_logic.layout.scripting.recorder.ref.value.object.CompactLabelPath;
import com.top_logic.layout.scripting.recorder.ref.value.object.ContextRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.LabelPath;
import com.top_logic.layout.scripting.recorder.ref.value.object.LabeledValue;
import com.top_logic.layout.scripting.recorder.ref.value.object.NamedValue;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ValueScope} that relies on {@link ValueNamingScheme}s with a {@link LabelProvider}
 * fall-back to identify values by label.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LabelScope implements ValueScope {

	private final LabelProvider _labelProvider;

	private final List<?> _options;

	/**
	 * Creates a {@link LabelScope}.
	 * 
	 * @param labelProvider
	 *        The {@link LabelProvider} used to identify values for which no
	 *        {@link ValueNamingScheme} exists.
	 * @param options
	 *        The context options to choose from during resolve.
	 */
	public LabelScope(LabelProvider labelProvider, List<?> options) {
		_labelProvider = labelProvider;
		_options = options;
	}

	@Override
	public Maybe<? extends ModelName> buildReference(Object value) {
		return ReferenceFactory.localValue(value, _labelProvider);
	}

	@Override
	@SuppressWarnings("deprecation") // As long as the class exists, it has to be supported here.
	public Object resolveReference(ActionContext actionContext, ContextRef valueRef) {
		if (valueRef instanceof LabeledValue) {
			return ValueResolver.resolveLabeledValue(actionContext, _options, _labelProvider, (LabeledValue) valueRef);
		} else if ((valueRef instanceof LabelPath) || (valueRef instanceof CompactLabelPath)) {
			// For compatibility with older test scripts (SelectField values were recorded as
			// LabelPath).

			String label = getSingletonPathEntry(valueRef);
			return FuzzyValueFormat.resolveLabeledValue(actionContext, valueRef, _options, _labelProvider, label);
		} else {
			return ValueNamingScheme.resolveNamedValue(actionContext, _options, (NamedValue) valueRef);
		}
	}

	@SuppressWarnings("deprecation") // As long as the class exists, it has to be supported here.
	private String getSingletonPathEntry(ContextRef valueRef) {
		if (valueRef instanceof LabelPath) {
			return ((LabelPath) valueRef).getLabelPath().get(0);
		} else {
			return ((CompactLabelPath) valueRef).getLabelPath().get(0);
		}
	}

}

