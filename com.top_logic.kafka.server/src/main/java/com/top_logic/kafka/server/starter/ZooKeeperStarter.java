/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.server.starter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.common.config.ConfigException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.apache.zookeeper.server.persistence.FileTxnSnapLog;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;

import com.top_logic.basic.AliasedProperties;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;

/**
 * Starter for the {@link ZooKeeper}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ZooKeeperStarter implements Starter {

	/**
	 * Configuration of the {@link ZooKeeperStarter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfigurationItem {

		/**
		 * {@link ZooKeeper} configuration file.
		 */
		@StringDefault("/WEB-INF/conf/kafka/zookeeper.properties")
		String getConfigFile();

	}

	private final ServerConfig _config;

	private ZooKeeperServer _zkServer;

	private FileTxnSnapLog _txnLog;

	ServerCnxnFactory _cnxnFactory;

	/**
	 * Creates a new {@link ZooKeeperStarter} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link ZooKeeperStarter}.
	 */
	public ZooKeeperStarter(InstantiationContext context, Config config) {
		ServerConfig newServerConfig = new ServerConfig();
		loadServerConfig(context, config, newServerConfig);
		_config = newServerConfig;
	}

	private void loadServerConfig(InstantiationContext context, Config config, ServerConfig serverConfig) {
		Properties properties = new AliasedProperties();
		try {
			try (InputStream configStream = FileManager.getInstance().getStream(config.getConfigFile())) {
				properties.load(configStream);
			}
		} catch (IOException ex) {
			context.error("Unable to load configuration '" + config.getConfigFile() + "'.", ex);
			return;
		}

		QuorumPeerConfig peerConfig = new QuorumPeerConfig();
		try {
			peerConfig.parseProperties(properties);
		} catch (IOException | ConfigException | QuorumPeerConfig.ConfigException ex) {
			context.error("Unable to parse configuration.", ex);
			return;
		}
		serverConfig.readFrom(peerConfig);
	}

	/**
	 * {@link ServerConfig} that is used to create the {@link ZooKeeper}.
	 */
	public ServerConfig getServerConfig() {
		return _config;
	}

	@Override
	public void startup() {
		startupZooKeeper();
	}

	/**
	 * Code stolen from
	 * {@link org.apache.zookeeper.server.ZooKeeperServerMain#runFromConfig(ServerConfig)}.
	 */
	private void startupZooKeeper() {
		boolean success = false;
		_txnLog = null;
		try {
			_zkServer = new ZooKeeperServer();
			File dataLogDir = _config.getDataLogDir();
			File snapDir = _config.getDataDir();
			_txnLog = new FileTxnSnapLog(dataLogDir, snapDir);
			registerZKShutdownHandler(_zkServer, new CountDownLatch(0));
			_zkServer.setTxnLogFactory(_txnLog);
			_zkServer.setTickTime(_config.getTickTime());
			_zkServer.setMinSessionTimeout(_config.getMinSessionTimeout());
			_zkServer.setMaxSessionTimeout(_config.getMaxSessionTimeout());
			_cnxnFactory = ServerCnxnFactory.createFactory();
			_cnxnFactory.configure(_config.getClientPortAddress(), _config.getMaxClientCnxns());
			_cnxnFactory.startup(_zkServer);
			success = true;
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			if (!success) {
				closeLog();
			}
		}
	}

	private void registerZKShutdownHandler(ZooKeeperServer zkServer, CountDownLatch countDown) {
		// register ShutdownHandler, otherwise error is logged.
		try {
			Class<?> shutdownHandlerClass = Class.forName("org.apache.zookeeper.server.ZooKeeperServerShutdownHandler");
			Constructor<?> constructor = shutdownHandlerClass.getDeclaredConstructor(CountDownLatch.class);
			// package private constructor
			constructor.setAccessible(true);
			Object newShutdownHandler = constructor.newInstance(countDown);

			Method registerMethod =
				ZooKeeperServer.class.getDeclaredMethod("registerServerShutdownHandler", shutdownHandlerClass);
			// package private method
			registerMethod.setAccessible(true);
			registerMethod.invoke(zkServer, newShutdownHandler);
		} catch (ClassNotFoundException ex) {
			errorRegisterShutdownHandler(ex);
		} catch (NoSuchMethodException ex) {
			errorRegisterShutdownHandler(ex);
		} catch (SecurityException ex) {
			errorRegisterShutdownHandler(ex);
		} catch (InstantiationException ex) {
			errorRegisterShutdownHandler(ex);
		} catch (IllegalAccessException ex) {
			errorRegisterShutdownHandler(ex);
		} catch (IllegalArgumentException ex) {
			errorRegisterShutdownHandler(ex);
		} catch (InvocationTargetException ex) {
			errorRegisterShutdownHandler(ex);
		}

	}

	private void errorRegisterShutdownHandler(Throwable ex) {
		Logger.error("Unable to register ZooKeeperServerShutdownHandler.", ex, ZooKeeperStarter.class);
	}

	private void closeLog() {
		if (_txnLog != null) {
			try {
				_txnLog.close();
			} catch (IOException ex) {
				Logger.error("Unable to close file log.", ex, ZooKeeperStarter.class);
			}
		}
	}

	@Override
	public void shutdown() {
		closeLog();
		_cnxnFactory.shutdown();
		waitForShutdownCompleted(2000);

	}

	private void waitForShutdownCompleted(int timeout) {
		Thread t = new Thread() {

			@Override
			public void run() {
				try {
					_cnxnFactory.join();
				} catch (InterruptedException ex) {
					// OK
				}
			}
		};
		t.start();
		try {
			// Wait until factory is shut down
			t.join(timeout);
		} catch (InterruptedException ex) {
			t.interrupt();
		}
		if (t.isAlive()) {
			Logger.error("Shutting down " + ServerCnxnFactory.class.getSimpleName() + " does not completed within "
				+ timeout + "ms.", ZooKeeperStarter.class);
		}
	}

}

