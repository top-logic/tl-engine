/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import java.util.Map;

import com.top_logic.layout.form.FormContainer;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Programmatic API for object creation from form data.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface CreateFunction {

	/**
	 * Create a new object from the given information.
	 * 
	 * @param component
	 *        The context component.
	 * @param createContext
	 *        The "parent" object of the newly created one (taken from the model of the create
	 *        command). May be <code>null</code>, if the creation happens without context.
	 * @param form
	 *        The validated form containing the create information from the UI.
	 * @param arguments
	 *        Additional generic arguments, normally empty.
	 * @return The new created object, never <code>null</code>.
	 */
	Object createObject(LayoutComponent component, Object createContext, FormContainer form,
			Map<String, Object> arguments);

}
