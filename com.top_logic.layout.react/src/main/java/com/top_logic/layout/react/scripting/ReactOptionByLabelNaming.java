/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.scripting;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.scripting.recorder.ref.ContextDependent;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * Names an option of a React select control by its label, relative to the control's
 * {@link ReactOptionScope option scope}.
 *
 * <p>
 * The label only has to be unique among the control's options, not globally — so this provides
 * context-relative identity for the headless agent interface, selected automatically by the
 * {@link ReactOptionScope} value-context type.
 * </p>
 */
public class ReactOptionByLabelNaming
		extends ModelNamingScheme<ReactOptionScope, Object, ReactOptionByLabelNaming.Name> {

	/**
	 * {@link ModelName} pointing to a React select option by its label.
	 */
	public interface Name extends ContextDependent {

		/**
		 * The option label.
		 */
		String getLabel();

		/**
		 * @see #getLabel()
		 */
		void setLabel(String value);
	}

	@Override
	public Class<Name> getNameClass() {
		return Name.class;
	}

	@Override
	public Class<Object> getModelClass() {
		return Object.class;
	}

	@Override
	public Class<ReactOptionScope> getContextClass() {
		return ReactOptionScope.class;
	}

	@Override
	public Maybe<Name> buildName(ReactOptionScope valueContext, Object model) {
		String label = valueContext.labels().getLabel(model);
		if (label == null || label.trim().isEmpty()) {
			return Maybe.none();
		}
		String trimmed = label.trim();
		if (resolve(valueContext, trimmed) != model) {
			// Not found or ambiguous: the label does not uniquely identify this option.
			return Maybe.none();
		}
		Name name = TypedConfiguration.newConfigItem(Name.class);
		name.setLabel(trimmed);
		return Maybe.some(name);
	}

	@Override
	public Object locateModel(ActionContext context, ReactOptionScope valueContext, Name name) {
		String expected = name.getLabel();
		Object option = resolve(valueContext, expected);
		if (option == null) {
			throw ApplicationAssertions.fail(name,
				"No unique option labeled '" + expected + "' among " + labelsOf(valueContext) + ".");
		}
		return option;
	}

	/**
	 * The unique option whose label equals {@code expected}, or {@code null} if there is none or more
	 * than one.
	 */
	private Object resolve(ReactOptionScope valueContext, String expected) {
		LabelProvider labels = valueContext.labels();
		Object found = null;
		for (Object option : valueContext.options()) {
			String label = labels.getLabel(option);
			if (label != null && label.trim().equals(expected)) {
				if (found != null) {
					return null;
				}
				found = option;
			}
		}
		return found;
	}

	private List<String> labelsOf(ReactOptionScope valueContext) {
		LabelProvider labels = valueContext.labels();
		List<String> result = new ArrayList<>();
		for (Object option : valueContext.options()) {
			String label = labels.getLabel(option);
			if (label != null) {
				result.add(label.trim());
			}
		}
		return result;
	}
}
