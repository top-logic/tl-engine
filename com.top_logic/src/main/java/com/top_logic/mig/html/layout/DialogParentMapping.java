/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.col.Mapping;

/**
 * Mapping of a {@link LayoutComponent} to its parent.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DialogParentMapping implements Mapping<LayoutComponent, LayoutComponent> {

	@Override
	public LayoutComponent map(LayoutComponent input) {
		return input.getDialogParent();
	}

}

