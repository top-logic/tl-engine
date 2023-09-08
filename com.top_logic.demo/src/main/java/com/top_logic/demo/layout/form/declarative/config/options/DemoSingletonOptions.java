/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.config.options;

import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.func.Function0;
import com.top_logic.demo.layout.form.declarative.values.DemoAbstractSingleton;
import com.top_logic.demo.layout.form.declarative.values.DemoSingletonFirst;
import com.top_logic.demo.layout.form.declarative.values.DemoSingletonSecond;
import com.top_logic.demo.layout.form.declarative.values.DemoSingletonThird;

/**
 * Demo option provider for singletons.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DemoSingletonOptions extends Function0<List<DemoAbstractSingleton>> {

	@Override
	public List<DemoAbstractSingleton> apply() {
		return Arrays.<DemoAbstractSingleton> asList(
			DemoSingletonFirst.INSTANCE,
			DemoSingletonSecond.INSTANCE,
			DemoSingletonThird.INSTANCE);
	}

}
