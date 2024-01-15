/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.form.definition.FormContextDefinition;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Configuration interface for the {@link ConfigureFormDefinitionActionOp} in the scripting framework.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public interface ConfigureFormDefinitionAction extends ApplicationAction, FormContextDefinition {

	/** Configuration name for the value of the {@link #getFormDefinition}. */
	String FORM_DEFINITION_NAME = "form-definition";

	/** Configuration name for the value of the {@link #getLayoutComponent()}. */
	String LAYOUT_COMPONENT_NAME = "layout-component";

	/** Configuration name for the value of the {@link #getType()}. */
	String TYPE_NAME = "type";

	/** Configuration name for the value of the {@link #isStandardForm()}. */
	String STANDARD_FORM_NAME = "standard-form";

	@Override
	@ClassDefault(ConfigureFormDefinitionActionOp.class)
	Class<ConfigureFormDefinitionActionOp> getImplementationClass();

	/** The customized form to store. */
	@Name(FORM_DEFINITION_NAME)
	FormDefinition getFormDefinition();

	/** @see #getFormDefinition() */
	void setFormDefinition(FormDefinition formDefinition);

	// Make it concrete with required context for FormDefinition configurations.
	@Override
	@NullDefault
	TLModelPartRef getFormContextType();

	/** The name of the {@link LayoutComponent}. */
	@Name(LAYOUT_COMPONENT_NAME)
	ComponentName getLayoutComponent();

	/** @see #getLayoutComponent() */
	void setLayoutComponent(ComponentName formDefinition);

	/** The name of the type of the form. */
	@Name(TYPE_NAME)
	String getType();

	/** @see #getType() */
	void setType(String name);

	/**
	 * Whether the standard form for the type is configured.
	 */
	@Name(STANDARD_FORM_NAME)
	@Mandatory
	boolean isStandardForm();

	/**
	 * Setter for {@link #isStandardForm()}.
	 */
	void setStandardForm(boolean value);
}
