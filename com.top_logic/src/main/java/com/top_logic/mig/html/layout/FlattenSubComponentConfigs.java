/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.func.Function1;
import com.top_logic.mig.html.layout.LayoutComponent.Config;

/**
 * Function to collect all {@link Config} of subcomponents in a {@link SubComponentConfig}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FlattenSubComponentConfigs extends Function1<List<Config>, Collection<? extends SubComponentConfig>> {

	@Override
	public List<Config> apply(Collection<? extends SubComponentConfig> arg) {
		return arg
			.stream()
			.flatMap(subComponent -> subComponent.getComponents().stream())
			.collect(Collectors.toList());
	}

}

