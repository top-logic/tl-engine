/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import java.util.List;

import com.top_logic.basic.func.Function1;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponent.Config;

/**
 * {@link Function1} converting a (non list) element into a (unmodifiable) list.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ToList extends Function1<List<LayoutComponent.Config>, LayoutComponent.Config> {

	@Override
	public List<Config> apply(Config arg) {
		return CollectionUtilShared.singletonOrEmptyList(arg);
	}
}

