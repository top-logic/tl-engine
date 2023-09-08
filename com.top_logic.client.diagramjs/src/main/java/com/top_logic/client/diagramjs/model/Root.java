/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.model;

import com.top_logic.client.diagramjs.core.Diagram;
import com.top_logic.client.diagramjs.core.ElementFactory;

/**
 * The top-level component of a {@link Diagram}.
 * 
 * @see ElementFactory#createRoot()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class Root extends Shape {

	/**
	 * Creates a {@link Root}.
	 */
	protected Root() {
		super();
	}

}
