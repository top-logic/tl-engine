/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModelPart;
import com.top_logic.tool.execution.ExecutableState.CommandVisibility;

/**
 * {@link ExecutabilityRule} that disables a button, if the components model is not a
 * {@link TLModelPart}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class IsModelPart implements ExecutabilityRule {

	@Override
	public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> values) {
		if (model instanceof TLModelPart) {
			return ExecutableState.EXECUTABLE;
		} else {
			return new ExecutableState(CommandVisibility.DISABLED, getNotModelPartDisabledKey(model));
		}
	}

	private ResKey getNotModelPartDisabledKey(Object model) {
		return I18NConstants.ERROR_IS_NOT_MODEL_PART.fill(MetaLabelProvider.INSTANCE.getLabel(model));
	}

}
