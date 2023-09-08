/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.values;

import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * A {@link PolymorphicConfiguration} that serves as example value for
 * {@link PolymorphicConfiguration}-valued properties.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface DemoPolymorphicConfig extends PolymorphicConfiguration<DemoDefaultConfiguredInstance>,
		AbstractDemoConfigItem {

	// Pure combination of its super interfaces.

}
