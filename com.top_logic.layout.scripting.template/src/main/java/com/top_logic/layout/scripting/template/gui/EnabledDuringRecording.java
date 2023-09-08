/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} that only enables a button, if recording is active.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class EnabledDuringRecording implements ExecutabilityRule {

	/**
	 * Singleton {@link EnabledDuringRecording} instance.
	 */
	public static final EnabledDuringRecording INSTANCE = new EnabledDuringRecording();

	private EnabledDuringRecording() {
		// Singleton constructor.
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		return ((ScriptRecorderTree) aComponent).isRecording() ? ExecutableState.EXECUTABLE
			: ExecutableState.createDisabledState(I18NConstants.ERROR_NOT_RECORDING);
	}

}