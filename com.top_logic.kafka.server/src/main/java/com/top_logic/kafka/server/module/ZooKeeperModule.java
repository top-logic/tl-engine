/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.server.module;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.server.ServerConfig;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.kafka.server.starter.ZooKeeperStarter;

/**
 * Module starting the {@link ZooKeeper}.
 * 
 * <p>
 * Actually a wrapper for {@link ZooKeeperStarter}.
 * </p>
 * 
 * @see ZooKeeperStarter
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ZooKeeperModule extends ManagedClass {

	/**
	 * Configuration of the {@link ZooKeeperModule}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ManagedClass.ServiceConfiguration<ZooKeeperModule>, ZooKeeperStarter.Config {
		// sum interface
	}

	private final ZooKeeperStarter _zooKeeperStarter;

	/**
	 * Creates a new {@link ZooKeeperModule} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link ZooKeeperModule}.
	 */
	public ZooKeeperModule(InstantiationContext context, Config config) {
		_zooKeeperStarter = new ZooKeeperStarter(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		_zooKeeperStarter.startup();
	}

	/**
	 * The configuration of the {@link ZooKeeper}.
	 */
	public ServerConfig getZooKeeperConf() {
		return _zooKeeperStarter.getServerConfig();
	}

	@Override
	protected void shutDown() {
		_zooKeeperStarter.shutdown();
		super.shutDown();
	}

	/**
	 * Module for the {@link ZooKeeperModule}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<ZooKeeperModule> {

		/** Sole instance of {@link Module}. */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<ZooKeeperModule> getImplementation() {
			return ZooKeeperModule.class;
		}

	}

}

