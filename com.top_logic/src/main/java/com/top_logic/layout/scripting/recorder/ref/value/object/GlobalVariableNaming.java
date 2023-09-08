/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.object;

import java.util.Map;

import com.top_logic.basic.col.Maybe;
import com.top_logic.layout.scripting.action.SetGlobalVariableAction;
import com.top_logic.layout.scripting.recorder.ref.GlobalModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.NamedModelName;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.GlobalVariableStore;
import com.top_logic.util.Utils;

/**
 * A {@link GlobalModelNamingScheme} that identifies a global variable of the scripting framework.
 * 
 * @see GlobalVariableStore
 * @see SetGlobalVariableAction
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class GlobalVariableNaming extends GlobalModelNamingScheme<Object, GlobalVariableNaming.GlobalVariableName> {

	/** The {@link ModelName} for the {@link GlobalVariableNaming}. */
	public interface GlobalVariableName extends NamedModelName {

		// marker interface

	}

	@Override
	public Class<? extends GlobalVariableName> getNameClass() {
		return GlobalVariableName.class;
	}

	@Override
	public Class<Object> getModelClass() {
		return Object.class;
	}

	@Override
	public Maybe<GlobalVariableName> buildName(Object model) {
		GlobalVariableStore variables = GlobalVariableStore.getFromSession();
		if (variables == null) {
			return Maybe.none();
		}
		for (Map.Entry<String, Object> variable : variables.getMappings().entrySet()) {
			if (Utils.equals(variable.getValue(), model)) {
				// Don't care if there is more than one match. Just take the first.
				return createName(variable.getKey());
			}
		}
		return Maybe.none();
	}

	private Maybe<GlobalVariableName> createName(String variableName) {
		GlobalVariableName name = createName();
		name.setName(variableName);
		return Maybe.toMaybeButTreatNullAsValidValue(name);
	}

	@Override
	public Object locateModel(ActionContext actionContext, GlobalVariableName name) {
		String variableSpec = name.getName();
		/* Lenient parsing, see FuzzyValueFormat.Parser.VARIABLE: */
		String variableName = variableSpec.startsWith("$") ? variableSpec.substring(1) : variableSpec;
		return actionContext.getGlobalVariableStore().get(variableName);

	}

}
