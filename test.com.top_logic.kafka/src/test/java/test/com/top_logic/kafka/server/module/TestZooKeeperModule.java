/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.kafka.server.module;

import java.io.File;

import org.apache.zookeeper.server.ServerConfig;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.kafka.server.module.ZooKeeperModule;

/**
 * Extension of {@link ZooKeeperModule} for tests.
 * 
 * <p>
 * This {@link ZooKeeperModule} removes the currently stored data before startup.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestZooKeeperModule extends ZooKeeperModule {

	/**
	 * Creates a new {@link TestZooKeeperModule}.
	 */
	public TestZooKeeperModule(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		removeZooKeeperData(getZooKeeperConf());
		super.startUp();
	}

	private void removeZooKeeperData(ServerConfig zooKeeperConfig) {
		File dataDir = zooKeeperConfig.getDataDir();
		FileUtilities.deleteR(dataDir);
	}

}

