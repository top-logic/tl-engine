/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.server.main;

import org.apache.zookeeper.ZooKeeper;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.kafka.server.starter.ZooKeeperStarter;

/**
 * Main program to start {@link ZooKeeper}.
 * 
 * @see KafkaStarterMain
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ZooKeeperStarterMain extends AbstractStarterMain {

	private ZooKeeperStarter _starter;

	@Override
	protected void start() throws Exception {
		ZooKeeperStarter.Config config = TypedConfiguration.newConfigItem(ZooKeeperStarter.Config.class);
		_starter = new ZooKeeperStarter(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		_starter.startup();
	}

	@Override
	protected void stop() throws Exception {
		_starter.shutdown();
	}

	public static void main(String[] args) throws Exception {
		new ZooKeeperStarterMain().runMain(args);
	}

}

