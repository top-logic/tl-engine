/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.execution;

import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.scripting.action.ApplicationAction;

/**
 * Exception thrown when an environment value does not match the corresponding value in the given
 * {@link ApplicationAction}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class EnvironmentMismatch extends I18NException {

	private final ApplicationAction _action;

	private final Object _environmentValue;

	private final Object _actionValue;

	EnvironmentMismatch(ResKey key, ApplicationAction action, Object environmentValue, Object actionValue) {
		super(key);
		_action = action;
		_environmentValue = environmentValue;
		_actionValue = actionValue;
	}

	/**
	 * The action which shall be executed with wrong environment value.
	 */
	public ApplicationAction getAction() {
		return _action;
	}

	/**
	 * The value of the environment.
	 */
	public Object getEnvironmentValue() {
		return _environmentValue;
	}

	/**
	 * The the expected environment value in the action.
	 */
	public Object getActionValue() {
		return _actionValue;
	}

}

