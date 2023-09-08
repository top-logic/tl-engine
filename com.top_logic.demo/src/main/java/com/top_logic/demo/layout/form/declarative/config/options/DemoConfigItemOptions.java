/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.config.options;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.func.Function0;
import com.top_logic.demo.layout.form.declarative.values.DemoConfigItem;

/**
 * Demo option provider for {@link ConfigurationItem}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DemoConfigItemOptions extends Function0<List<DemoConfigItem>> {

	@Override
	public List<DemoConfigItem> apply() {
		List<DemoConfigItem> list = new ArrayList<>();
		list.add(newConfigItem("First.first", "First.second"));
		list.add(newConfigItem("Second.first", "Second.second"));
		list.add(newConfigItem("Third.first", "Third.second"));
		return list;
	}

	private DemoConfigItem newConfigItem(String first, String second) {
		DemoConfigItem config = TypedConfiguration.newConfigItem(DemoConfigItem.class);
		config.setFirst(first);
		config.setSecond(second);
		return config;
	}

}
