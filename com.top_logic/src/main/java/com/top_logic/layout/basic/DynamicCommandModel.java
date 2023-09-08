/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.layout.form.model.ExecutabilityModel;
import com.top_logic.layout.form.model.ExecutabilityModel.ExecutabilityListener;
import com.top_logic.layout.form.model.ExecutabilityPolling;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link AbstractCommandModel} with dynamic executability based on an {@link ExecutabilityModel}.
 * 
 * @see #getExecutability()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DynamicCommandModel extends AbstractCommandModel implements ExecutabilityPolling,
		ExecutabilityListener {

	private ExecutabilityModel _executability;

	/**
	 * Creates a {@link DynamicCommandModel}.
	 * 
	 * @param executability
	 *        See {@link #getExecutability()}
	 */
	public DynamicCommandModel(ExecutabilityModel executability) {
		_executability = executability;
	}

	/**
	 * The {@link ExecutabilityModel} of this {@link DynamicDelegatingCommandModel}.
	 */
	public final ExecutabilityModel getExecutability() {
		return _executability;
	}

	/**
	 * Allow lately initializing the {@link ExecutabilityModel} from super constructors.
	 * 
	 * @param executability
	 *        See {@link #getExecutability()}.
	 */
	protected void initExecutability(ExecutabilityModel executability) {
		_executability = executability;
	}

	/**
	 * If the first listener is registered this {@link DynamicDelegatingCommandModel} registers itself at
	 * {@link CommandModelRegistry} to be informed about an update.
	 * 
	 * @see AbstractCommandModel#addListener(EventType, PropertyListener)
	 */
	@Override
	protected void firstListenerAdded() {
		super.firstListenerAdded();

		_executability.addExecutabilityListener(this);
		CommandModelRegistry.getRegistry().registerCommandModel(this);
	}

	/**
	 * If the last listener was deregistered, this {@link DynamicDelegatingCommandModel} deregisters itself
	 * from {@link CommandModelRegistry}.
	 * 
	 * @see AbstractCommandModel#removeListener(EventType, PropertyListener)
	 */
	@Override
	protected void lastListenerRemoved() {
		super.lastListenerRemoved();

		CommandModelRegistry.getRegistry().deregisterCommandModel(this);
		_executability.removeExecutabilityListener(this);
	}

	@Override
	public void handleExecutabilityChange(ExecutabilityModel sender, ExecutableState oldState, ExecutableState newState) {
		// Trigger calculating a new executability state and updates to the visible and executable
		// properties.
		updateState();
	}

	@Override
	public void updateExecutabilityState() {
		_executability.updateExecutabilityState();
	}

	@Override
	protected ExecutableState computeState() {
		return super.computeState().combine(_executability.getExecutability());
	}

}
