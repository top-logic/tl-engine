/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.TLDeleteProtected;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} that disables a command, if the {@link TLModelPart} model is
 * {@link TLDeleteProtected}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DisableIfDeleteProtected implements ExecutabilityRule {

	/**
	 * Singleton {@link DisableIfDeleteProtected} instance.
	 */
	public static final DisableIfDeleteProtected INSTANCE = new DisableIfDeleteProtected();

	private DisableIfDeleteProtected() {
		// Singleton constructor.
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
		if ((model instanceof TLModelPart) && DisplayAnnotations.isDeleteProtected((TLModelPart) model)) {
	        return ExecutableState.createDisabledState(I18NConstants.DELETE_PROTECTED);
	    }
	    return ExecutableState.EXECUTABLE;
	}

}