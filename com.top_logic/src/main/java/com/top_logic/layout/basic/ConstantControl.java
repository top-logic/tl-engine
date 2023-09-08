/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.util.Collections;
import java.util.Map;

import com.top_logic.layout.Control;

/**
 * The class {@link ConstantControl} is a simple control which has a
 * {@link #getModel() model} of type T, is constantly visible and never has
 * updates.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ConstantControl<T> extends AbstractConstantControl {
	
	private T model;
	
	protected ConstantControl(T model, Map<String, ControlCommand> commandsByName) {
		super(commandsByName);
		this.model = model;
	}
	
	protected ConstantControl(T model) {
		this(model, Collections.<String, ControlCommand>emptyMap());
	}

	/**
	 * The model that is displayed by this {@link Control}.
	 */
	@Override
	public final T getModel() {
		return model;
	}

	/**
	 * @see #getModel()
	 */
	public void setModel(T model) {
		this.model = model;

		requestRepaint();
	}

}

