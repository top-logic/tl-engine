/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.kafka.server.module;

import java.io.File;

import junit.framework.Test;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.server.ServerConfig;

import test.com.top_logic.basic.RearrangableTestSetup;

import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.kafka.server.module.ZooKeeperModule;

/**
 * Setup removing the {@link ZooKeeper} data.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RemoveZooKeeperFiles extends RearrangableTestSetup {

	private static final MutableInteger SETUP_CNT = new MutableInteger();

	/**
	 * Creates a new {@link RemoveZooKeeperFiles}.
	 */
	public RemoveZooKeeperFiles(Test test) {
		super(test, SETUP_CNT);
	}

	@Override
	protected void doSetUp() throws Exception {
		ZooKeeperModule zooKeeperModule = ZooKeeperModule.Module.INSTANCE.getImplementationInstance();
		ServerConfig config = zooKeeperModule.getZooKeeperConf();
		File dataDir = config.getDataDir();
		FileUtilities.deleteR(dataDir);
	}

	@Override
	protected void doTearDown() throws Exception {
		// Files are removed on startup.
	}

}

