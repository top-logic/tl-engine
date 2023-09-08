/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.component.ComponentBasedNamingScheme;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.scripting.recorder.ref.ui.LayoutComponentResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationActionOp;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.util.TLModelUtil;

/**
 * The {@link ApplicationActionOp} replaces and store a {@link FormDefinition} of a
 * {@link TLStructuredType} for a {@link LayoutComponent}, or sets it as default for a
 * {@link TLStructuredType}.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class ConfigureFormDefinitionActionOp extends AbstractApplicationActionOp<ConfigureFormDefinitionAction>
		implements LayoutComponentResolver {

	/** {@link TypedConfiguration} constructor. */
	public ConfigureFormDefinitionActionOp(InstantiationContext context, ConfigureFormDefinitionAction config) {
		super(context, config);
	}

	@Override
	protected Object processInternal(ActionContext context, Object argument) throws Throwable {
		FormDefinition formDefinition = getConfig().getFormDefinition();
		LayoutComponent componentRoot = context.getMainLayout();
		LayoutComponent component =
			ComponentBasedNamingScheme.locateComponent(getConfig(), componentRoot, getConfig().getLayoutComponent());
		checkVisible(context, getConfig(), component);
		String typeName = getConfig().getType();
		if (getConfig().isStandardForm()) {
			TLStructuredType type = (TLStructuredType) TLModelUtil.resolveQualifiedName(typeName);
			AbstractConfigureFormDefinitionCommand.updateStandardForm((FormComponent) component, formDefinition, type);
		} else {
			AbstractConfigureFormDefinitionCommand.replaceAndStoreFormDefinition(component, formDefinition, typeName);
		}

		return argument;
	}

}
