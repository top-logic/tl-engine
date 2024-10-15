/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.execution.start;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.bpe.bpml.model.StartEvent;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link StartEventSelector} using the component's model as {@link StartEvent}.
 */
@InApp
public class ModelAsStartEvent implements StartEventSelector {

	/**
	 * Singleton {@link ModelAsStartEvent} instance.
	 */
	public static final ModelAsStartEvent INSTANCE = new ModelAsStartEvent();

	private ModelAsStartEvent() {
		// Singleton constructor.
	}

	@Override
	public StartEvent getStartEvent(LayoutComponent component) {
		return (StartEvent) component.getModel();
	}

}
