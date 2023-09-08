/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Collection;
import java.util.Map;

import com.top_logic.layout.component.Selectable;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ExecutabilityRule} that disables a button, if the target component has no selection.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NoSelectionDisabled implements ExecutabilityRule {

	/**
	 * Singleton {@link NoSelectionDisabled} instance.
	 */
	public static final NoSelectionDisabled INSTANCE = new NoSelectionDisabled();

	private NoSelectionDisabled() {
		// Singleton constructor.
	}
	
	@Override
	public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> someValues) {
		if (!(component instanceof Selectable)) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		Selectable selectable = (Selectable) component;
		Object selection = selectable.getSelected();
		if (isNullOrEmpty(selection)) {
			return ExecutableState.createDisabledState(I18NConstants.ERROR_NO_SELECTION);
		}
		return ExecutableState.EXECUTABLE;
	}

	private boolean isNullOrEmpty(Object obj) {
		return obj == null || (obj instanceof Collection<?> && ((Collection<?>) obj).isEmpty());
	}

}
