/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.renderer;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.table.TableRenderer;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;

/**
 * Extension of {@link DefaultTableRenderer} for tests. This instance can be used to ensure that not
 * a default {@link TableRenderer} is used.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class TestingTableRenderer extends DefaultTableRenderer {

	/** Singleton {@link TestingTableRenderer} instance. */
	public static final TestingTableRenderer INSTANCE = new TestingTableRenderer();

	private TestingTableRenderer() {
		super(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, createConfig());
	}

	private static Config createConfig() {
		Config configItem = TypedConfiguration.newConfigItem(DefaultTableRenderer.Config.class);
		configItem.setImplementationClass(TestingTableRenderer.class);
		return configItem;
	}

}
