/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.func.Function0;
import com.top_logic.mig.html.layout.LayoutInfo;

/**
 * Default {@link LayoutInfo} options for dynamic component creation.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class DefaultLayoutInfoOptions extends Function0<List<LayoutInfo>> {

	@Override
	public List<LayoutInfo> apply() {
		return Arrays.asList(TypedConfiguration.newConfigItem(LayoutInfo.class));
	}

}
