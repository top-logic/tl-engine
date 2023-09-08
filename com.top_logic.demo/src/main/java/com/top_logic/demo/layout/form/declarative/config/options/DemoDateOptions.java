/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.config.options;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.func.Function0;

/**
 * Demo option provider for {@link Date}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DemoDateOptions extends Function0<List<Date>> {

	@Override
	public List<Date> apply() {
		return Arrays.asList(new Date(Long.MIN_VALUE), new Date(0), new Date(Long.MAX_VALUE));
	}

}
