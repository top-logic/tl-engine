/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;


import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityModel} that constantly returns a given {@link ExecutableState}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConstantExecutabilityModel implements ExecutabilityModel {

	/**
	 * {@link ConstantExecutabilityModel} that unconditinally reports to be executable.
	 */
	public static final ExecutabilityModel ALWAYS_EXECUTABLE =
		new ConstantExecutabilityModel(ExecutableState.EXECUTABLE);

	private ExecutableState _executability;

	/**
	 * Creates a new {@link ConstantExecutabilityModel}
	 * 
	 * @param executability
	 *        Value of {@link #getExecutability()}.
	 */
	public ConstantExecutabilityModel(ExecutableState executability) {
		_executability = executability;
	}

	@Override
	public ExecutableState getExecutability() {
		return _executability;
	}

	@Override
	public void addExecutabilityListener(ExecutabilityListener listener) {
		// Executability does not change, therefore no listener necessary.
	}

	@Override
	public void removeExecutabilityListener(ExecutabilityListener listener) {
		// Executability does not change, therefore no listener necessary.
	}

	@Override
	public void updateExecutabilityState() {
		// No update needed.
	}

}

