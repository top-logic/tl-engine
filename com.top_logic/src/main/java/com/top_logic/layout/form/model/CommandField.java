/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;


import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.layout.basic.ButtonUIModel;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelRegistry;
import com.top_logic.layout.basic.check.CheckScope;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMemberVisitor;

/**
 * {@link FormField} for implementing buttons.
 * 
 * <p>
 * Directly subclass {@link CommandField} instead of parameterizing {@link CommandField} if the
 * {@link #executeCommand(com.top_logic.layout.DisplayContext)} functionality needs access to the UI
 * aspects.
 * </p>
 * 
 * @see FormFactory#newCommandField(String, com.top_logic.layout.basic.Command)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class CommandField extends AbstractButtonField implements CommandModel {

	private final CheckScope checkScope;

	/**
	 * Creates a {@link CommandField}.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * 
	 * @see FormFactory#newCommandField(String, com.top_logic.layout.basic.Command)
	 */
	protected CommandField(String name) {
		this(name, ConstantExecutabilityModel.ALWAYS_EXECUTABLE);
	}

	/**
	 * Creates a {@link CommandField}.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param executability
	 *        The dynamic executability.
	 * 
	 * @see FormFactory#newCommandField(String, com.top_logic.layout.basic.Command,
	 *      ExecutabilityModel)
	 */
	protected CommandField(String name, ExecutabilityModel executability) {
		this(name, executability, CheckScope.NO_CHECK);
	}

	/**
	 * Creates a {@link CommandField}.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param executability
	 *        The dynamic executability.
	 * @param checkScope
	 *        See {@link #getCheckScope()}.
	 * 
	 * @see FormFactory#newCommandField(String, com.top_logic.layout.basic.Command,
	 *      ExecutabilityModel, CheckScope)
	 */
	protected CommandField(String name, ExecutabilityModel executability, CheckScope checkScope) {
		super(name, executability);
		this.checkScope = checkScope;
	}

	@Override
	public CheckScope getCheckScope() {
		return this.checkScope;
	}

	/**
	 * If the first listener is registered this {@link CommandField} registers itself at
	 * {@link CommandModelRegistry} to be informed about an update.
	 * 
	 * @see AbstractFormMember#addListener(EventType, PropertyListener)
	 * @see ButtonUIModel#addListener(EventType, PropertyListener)
	 */
	@Override
	public <L extends PropertyListener, S, V> boolean addListener(EventType<L, S, V> type, L listener) {
		if (!hasListeners()) {
			CommandModelRegistry.getRegistry().registerCommandModel(this);
		}
		return super.addListener(type, listener);
	}

	/**
	 * If the last listener was deregistered, this {@link CommandField} deregisters itself from
	 * {@link CommandModelRegistry}.
	 * 
	 * @see ButtonUIModel#removeListener(EventType, PropertyListener)
	 * @see AbstractFormMember#removeListener(EventType, PropertyListener)
	 */
	@Override
	public <L extends PropertyListener, S, V> boolean removeListener(EventType<L, S, V> type, L listener) {
		boolean result = super.removeListener(type, listener);

		if (!hasListeners()) {
			CommandModelRegistry.getRegistry().deregisterCommandModel(this);
		}

		return result;

	}

	@Override
	public Object visit(FormMemberVisitor v, Object arg) {
		return v.visitCommandField(this, arg);
	}

}
