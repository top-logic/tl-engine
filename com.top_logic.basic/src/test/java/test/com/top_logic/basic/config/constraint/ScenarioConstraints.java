/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.constraint;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Subtypes;

/**
 * {@link ConfigurationItem} interfaces for {@link TestConstraintChecker}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public interface ScenarioConstraints {

	public interface ScenarioTypeInstanceFormat extends ConfigurationItem {

		@InstanceFormat
		@Subtypes({})
		Object getInstance();

		void setInstance(Object value);
	}

}
