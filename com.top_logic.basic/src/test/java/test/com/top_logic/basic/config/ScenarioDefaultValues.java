/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;

/**
 * {@link ConfigurationItem} interfaces for {@link TestDefaultValues}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public interface ScenarioDefaultValues {

	public interface Example extends ConfigurationItem {

		int INT_DEFAULT = 1;

		String STRING_DEFAULT = "a";

		Class<?> CLASS_DEFAULT = System.class;

		@IntDefault(INT_DEFAULT)
		int getInt();

		@IntDefault(INT_DEFAULT)
		Integer getInteger();

		@StringDefault(STRING_DEFAULT)
		@Nullable
		String getStringNullable();

		@StringDefault(STRING_DEFAULT)
		@NonNullable
		String getStringNonNullable();

		@ClassDefault(System.class)
		Class<?> getClassNullable();

		@ClassDefault(System.class)
		@NonNullable
		Class<?> getClassNonNullable();

	}

}
