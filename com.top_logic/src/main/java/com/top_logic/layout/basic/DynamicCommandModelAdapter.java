/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.ExecutabilityModel;
import com.top_logic.layout.form.model.ExecutabilityPolling;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link CommandModelAdapter} with dynamic executability based on an {@link ExecutabilityModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DynamicCommandModelAdapter extends CommandModelAdapter implements ExecutabilityPolling,
		ExecutabilityModel.ExecutabilityListener {

	private final ExecutabilityModel _executability;

	/**
	 * Creates a {@link DynamicCommandModelAdapter}.
	 * 
	 * @param impl
	 *        The {@link CommandModel} to delegate all but executability.
	 * @param executability
	 *        The {@link ExecutabilityModel} to delegate executability to.
	 * 
	 * @see CommandModelFactory#commandModel(CommandModel, ExecutabilityModel)
	 */
	protected DynamicCommandModelAdapter(CommandModel impl, ExecutabilityModel executability) {
		super(impl);
		_executability = executability;
	}

	@Override
	protected void firstListenerAdded() {
		super.firstListenerAdded();

		_executability.addExecutabilityListener(this);
		CommandModelRegistry.getRegistry().registerCommandModel(this);
	}

	@Override
	protected void lastListenerRemoved() {
		super.lastListenerRemoved();

		CommandModelRegistry.getRegistry().deregisterCommandModel(this);
		_executability.removeExecutabilityListener(this);
	}

	@Override
	protected void forwardEvent(EventType<?, ?, ?> type, Object oldValue, Object newValue) {
		// Filter out events produced in reaction of executability.
		if (type == FormMember.VISIBLE_PROPERTY) {
			return;
		}
		if (type == CommandModel.EXECUTABLE_PROPERTY) {
			return;
		}
		if (type == CommandModel.NOT_EXECUTABLE_REASON_PROPERTY) {
			return;
		}
		super.forwardEvent(type, oldValue, newValue);
	}

	@Override
	public void updateExecutabilityState() {
		_executability.updateExecutabilityState();
	}

	@Override
	public void handleExecutabilityChange(ExecutabilityModel sender, ExecutableState oldState, ExecutableState newState) {
		DefaultButtonUIModel.notifyStateChanged(this, oldState, newState);
	}

	@Override
	public boolean isVisible() {
		return _executability.getExecutability().isVisible();
	}

	@Override
	public void setVisible(boolean visible) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isExecutable() {
		return _executability.getExecutability().isExecutable();
	}

	@Override
	public ResKey getNotExecutableReasonKey() {
		return _executability.getExecutability().getI18NReasonKey();
	}

	@Override
	public void setExecutable() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNotExecutable(ResKey disabledReason) {
		throw new UnsupportedOperationException();
	}

}
