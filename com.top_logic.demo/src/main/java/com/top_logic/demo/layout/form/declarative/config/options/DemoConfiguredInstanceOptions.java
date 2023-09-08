/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.config.options;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.func.Function0;
import com.top_logic.demo.layout.form.declarative.values.DemoConfiguredInstance;
import com.top_logic.demo.layout.form.declarative.values.DemoPolymorphicConfig;

/**
 * Demo option provider for {@link ConfiguredInstance}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DemoConfiguredInstanceOptions extends Function0<List<DemoConfiguredInstance>> {

	@Override
	public List<DemoConfiguredInstance> apply() {
		List<DemoConfiguredInstance> list = new ArrayList<>();
		list.add(newConfiguredInstance("First.first", "First.second"));
		list.add(newConfiguredInstance("Second.first", "Second.second"));
		list.add(newConfiguredInstance("Third.first", "Third.second"));
		return list;
	}

	private DemoConfiguredInstance newConfiguredInstance(String first, String second) {
		DemoPolymorphicConfig config = TypedConfiguration.newConfigItem(DemoPolymorphicConfig.class);
		config.setFirst(first);
		config.setSecond(second);
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

}
