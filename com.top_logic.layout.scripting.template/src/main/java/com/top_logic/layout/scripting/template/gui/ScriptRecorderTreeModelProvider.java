/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.component.model.ModelProvider;
import com.top_logic.layout.scripting.action.ActionChain;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The {@link ModelProvider} of the {@link ScriptRecorderTree}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ScriptRecorderTreeModelProvider implements ModelProvider {

	@Override
	public Object getBusinessModel(LayoutComponent businessComponent) {
		Object model = businessComponent.getModel();

		if (model == null) {
			return ScriptContainer.createTransient(TypedConfiguration.newConfigItem(ActionChain.class));
		}

		return model;
	}

}
