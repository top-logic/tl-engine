/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.model;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.model.AbstractExecutabilityModel;
import com.top_logic.layout.form.model.AbstractDynamicCommand;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link AbstractExecutabilityModel} whose {@link #getExecutability()} can be set.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestingExecutabilityModel extends AbstractDynamicCommand {

	private ExecutableState _executability = ExecutableState.EXECUTABLE;

	private final Command _delegate;

	/**
	 * Creates a new {@link TestingExecutabilityModel} delegating {@link Command} methods to
	 * given {@link Command}.
	 */
	public TestingExecutabilityModel(Command executable) {
		_delegate = executable;
	}

	@Override
	protected ExecutableState calculateExecutability() {
		return _executability;
	}

	/**
	 * Setter for the {@link ExecutableState}.
	 */
	public void setExecutability(ExecutableState executability) {
		if (executability == null) {
			throw new NullPointerException("'executability' must not be 'null'.");
		}
		_executability = executability;
		updateExecutabilityState();
	}

	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		return _delegate.executeCommand(context);
	}

}

