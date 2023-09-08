/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.model;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.annotation.DefaultValueProvider;
import com.top_logic.basic.config.annotation.DefaultValueProviderShared;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;

/**
 * Primary {@link DBIndex}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DBPrimary extends DBIndex {

	@Override
	@StringDefault("pk")
	public String getName();

	@Override
	@ComplexDefault(PrimaryKind.class)
	public Kind getKind();

	/**
	 * {@link DefaultValueProvider} providing {@link com.top_logic.basic.db.model.DBIndex.Kind#PRIMARY}.
	 */
	class PrimaryKind extends DefaultValueProviderShared {
		@Override
		public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
			return Kind.PRIMARY;
		}
	}
}
