/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.layout.Control;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The class {@link EmptyLayoutControlProvider} is a {@link LayoutControlProvider} which always
 * returns <code>null</code>.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class EmptyLayoutControlProvider implements LayoutControlProvider {

	@Override
	public Control createLayoutControl(Strategy strategy, LayoutComponent component) {
		return null;
	}

}
