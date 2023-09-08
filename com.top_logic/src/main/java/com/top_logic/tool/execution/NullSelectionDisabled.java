/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.basic.util.Utils;
import com.top_logic.layout.component.Selectable;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ExecutabilityRule} that returns "disabled" when the selection of the component is null.
 * <p>
 * The {@link LayoutComponent} has to implement {@link Selectable}.
 * </p>
 *
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class NullSelectionDisabled implements ExecutabilityRule {

	/** The {@link ExecutableState} for "no selection". */
	public static final ExecutableState EXEC_STATE_DISABLED =
		ExecutableState.createDisabledState(I18NConstants.ERROR_NO_SELECTION);

	/** The {@link NullSelectionDisabled} instance. */
	public static final NullSelectionDisabled INSTANCE = new NullSelectionDisabled();

	@Override
	public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> values) {
		if (!(component instanceof Selectable)) {
			String message = createErrorMessageNotASelectable(component);
			return ExecutableState.createErrorOccurredState(new ClassCastException(message));
		}
		Selectable selectable = (Selectable) component;
		if (selectable.getSelected() == null) {
			return NullSelectionDisabled.EXEC_STATE_DISABLED;
		}
		return ExecutableState.EXECUTABLE;
	}

	private String createErrorMessageNotASelectable(LayoutComponent component) {
		return "The component has to be a " + Selectable.class.getName()
			+ ", but is: " + Utils.debug(component);
	}

}
