/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.config.options;

import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.func.Function0;
import com.top_logic.demo.layout.form.declarative.values.DemoEnum;

/**
 * Demo option provider for {@link Enum}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DemoEnumOptions extends Function0<List<DemoEnum>> {

	@Override
	public List<DemoEnum> apply() {
		// Not all of the enum values and in a different order to demonstrate that this works.
		return Arrays.asList(DemoEnum.THIRD, DemoEnum.FIRST);
	}

}
