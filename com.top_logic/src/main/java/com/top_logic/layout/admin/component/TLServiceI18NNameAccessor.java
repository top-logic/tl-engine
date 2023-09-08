/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.provider.label.I18NClassNameProvider;

/**
 * {@link Accessor} for the i18n name of a {@link BasicRuntimeModule}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TLServiceI18NNameAccessor extends ReadOnlyAccessor<BasicRuntimeModule<?>> {

	@Override
	public Object getValue(BasicRuntimeModule<?> object, String property) {
		Class<?> implementationClass = object.getImplementation();

		return I18NClassNameProvider.INSTANCE.getLabel(implementationClass);
	}

}
