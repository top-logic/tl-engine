/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Collection;
import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;

/**
 * {@link NullModelDisabled} rule that also disables a command when its target model is a
 * {@link TLObject#tTransient() transient object}, or not a {@link TLObject} at all.
 */
public class TransientObjectDisabled extends NullModelDisabled {

	/** {@link ExecutableState#isDisabled() Disabled} state "Object is transient". */
	@SuppressWarnings("hiding")
	public static final ExecutableState EXEC_STATE_DISABLED =
		ExecutableState.createDisabledState(I18NConstants.ERROR_TRANSIENT_OBJECT);

	/**
	 * Singleton {@link TransientObjectDisabled} instance.
	 */
	@SuppressWarnings("hiding")
	public static final TransientObjectDisabled INSTANCE = new TransientObjectDisabled();

	private TransientObjectDisabled() {
		// Singleton constructor.
	}
	
	@Override
	public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> someValues) {
		ExecutableState result = super.isExecutable(component, model, someValues);
		if (!result.isExecutable()) {
			return result;
		}

		return testTransient(model);
	}

	private ExecutableState testTransient(Object model) {
		if (model instanceof TLObject) {
			if (((TLObject) model).tTransient()) {
				return EXEC_STATE_DISABLED;
			} else {
				return ExecutableState.EXECUTABLE;
			}
		} else if (model instanceof Collection<?>) {
			// Test content.
			for (Object content : (Collection<?>) model) {
				ExecutableState result = testTransient(content);
				if (!result.isExecutable()) {
					return result;
				}
			}
			return ExecutableState.EXECUTABLE;
		} else {
			return EXEC_STATE_DISABLED;
		}
	}

}
