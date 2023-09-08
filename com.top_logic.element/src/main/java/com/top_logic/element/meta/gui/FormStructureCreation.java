/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.gui;

import com.top_logic.element.structured.StructuredElement;
import com.top_logic.model.TLObject;

/**
 * {@link FormObjectCreation} linking a created {@link StructuredElement} to its context in which it
 * is created.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormStructureCreation extends FormObjectCreation {

	/**
	 * Singleton {@link FormStructureCreation} instance.
	 */
	@SuppressWarnings("hiding")
	public static final FormStructureCreation INSTANCE = new FormStructureCreation();

	/**
	 * Creates a {@link FormStructureCreation}.
	 */
	protected FormStructureCreation() {
		super();
	}

	@Override
	protected void initContainer(TLObject container, TLObject newInstance, Object createContext) {
		super.initContainer(container, newInstance, createContext);
		if (container instanceof StructuredElement) {
			((StructuredElement) container).getChildrenModifiable().add((StructuredElement) newInstance);
		}
	}

}
