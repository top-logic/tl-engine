/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.server.main;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.kafka.server.starter.KafkaStarter;

import kafka.Kafka;

/**
 * Main program to start {@link Kafka}.
 * 
 * @see ZooKeeperStarterMain
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class KafkaStarterMain extends AbstractStarterMain {

	private KafkaStarter _starter;

	@Override
	protected void start() throws Exception {
		KafkaStarter.Config config = TypedConfiguration.newConfigItem(KafkaStarter.Config.class);
		_starter = new KafkaStarter(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		_starter.startup();
	}

	@Override
	protected void stop() throws Exception {
		_starter.shutdown();
	}

	public static void main(String[] args) throws Exception {
		new KafkaStarterMain().runMain(args);
	}

}

