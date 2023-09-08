/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.internal.gen.NoImplementationClassGeneration;

/**
 * {@link ConfigurationItem} interfaces for {@link TestPropertyWithoutCommonRoot}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public interface ScenarioPropertyWithoutCommonRoot {

	public interface ScenarioTypeA extends ConfigurationItem {

		String getTestProperty();

	}

	public interface ScenarioTypeB extends ConfigurationItem {

		String getTestProperty();

	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeC extends ScenarioTypeB, ScenarioTypeA {
		// Nothing needed in here
	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeD extends ConfigurationItem {

		ScenarioTypeC getBrokenConfig();

	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeE extends ConfigurationItem {

		ScenarioTypeD getBrokenConfig();

	}

}
