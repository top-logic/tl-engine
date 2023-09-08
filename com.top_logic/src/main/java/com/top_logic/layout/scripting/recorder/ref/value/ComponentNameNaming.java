/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.QualifiedComponentNameConstraint;

/**
 * {@link AbstractModelNamingScheme} for {@link ComponentName}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ComponentNameNaming extends AbstractModelNamingScheme<ComponentName, ComponentNameNaming.Name> {

	/**
	 * {@link ModelName} of {@link ComponentName}.
	 */
	public interface Name extends ModelName {

		/**
		 * The literal value.
		 */
		@Constraint(QualifiedComponentNameConstraint.class)
		ComponentName getValue();

		/**
		 * @see #getValue()
		 */
		void setValue(ComponentName value);

	}

	/**
	 * Creates a new {@link ComponentNameNaming}.
	 */
	public ComponentNameNaming() {
		super(ComponentName.class, ComponentNameNaming.Name.class);
	}

	@Override
	protected void initName(Name name, ComponentName model) {
		name.setValue(model);
	}

	@Override
	public ComponentName locateModel(ActionContext context, Name name) {
		return name.getValue();
	}

}

