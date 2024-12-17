/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ExecutabilityRule} that displays a command only if its component is opened in a dialog.
 */
public class InDialogExecutable implements ExecutabilityRule {

	/**
	 * Singleton {@link InDialogExecutable} instance.
	 */
	public static final InDialogExecutable INSTANCE = new InDialogExecutable();

	private InDialogExecutable() {
		// Singleton constructor.
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		return aComponent.openedAsDialog() ? ExecutableState.EXECUTABLE : ExecutableState.NOT_EXEC_HIDDEN;
	}

}
