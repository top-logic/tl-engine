/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.data;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyInitializer;

/**
 * {@link PropertyInitializer} for MBeans with an expression.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class MBeanByExpressionValueInitializer implements PropertyInitializer {

	@Override
	public Object getInitialValue(PropertyDescriptor property) {
		String packageName = getClass().getPackageName();
		return packageName + ":name=expression0";
	}

}
