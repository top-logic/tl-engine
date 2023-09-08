/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.ProtocolAdaptor;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.basic.sql.ConnectionPool;

/**
 * Context for a {@link DBSetupAction}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
abstract class SetupContext extends ProtocolAdaptor {

	private boolean _interactive;

	private final KnowledgeBaseConfiguration _kbConfig;

	/**
	 * Creates a new {@link SetupContext} with the given {@link KnowledgeBase} configuration.
	 */
	public SetupContext(Protocol protocol, KnowledgeBaseConfiguration kbConfig) {
		super(protocol);
		_kbConfig = kbConfig;
	}

	/**
	 * Creates a new {@link SetupContext} with the configuration of the {@link KnowledgeBase} with
	 * the given name read from the configuration.
	 * 
	 * @throws ConfigurationError
	 *         When there is no configuration with the given name.
	 */
	public SetupContext(Protocol protocol, String kb) {
		this(protocol, getKBConfig(kb));
	}

	/**
	 * Creates a new {@link SetupContext} with the configuration of the "default"
	 * {@link KnowledgeBase} read from the configuration.
	 */
	public SetupContext(Protocol protocol) {
		this(protocol, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME);
	}

	private static KnowledgeBaseConfiguration getKBConfig(String kb) {
		ServiceConfiguration<KnowledgeBaseFactory> kbfConfig;
		try {
			kbfConfig = ApplicationConfig.getInstance().getServiceConfiguration(KnowledgeBaseFactory.class);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
		KnowledgeBaseConfiguration config = ((KnowledgeBaseFactoryConfig) kbfConfig).getKnowledgeBases().get(kb);
		if (config == null) {
			throw new ConfigurationError(I18NConstants.NO_KBCONFIG__NAME.fill(kb));
		}
		return config;
	}

	/**
	 * Whether the tool was started from the command line.
	 */
	public boolean isInteractive() {
		return _interactive;
	}

	/**
	 * Setter for {@link #isInteractive()}
	 */
	public void setInteractive(boolean interactive) {
		_interactive = interactive;
	}

	/**
	 * The configuration of the {@link KnowledgeBase} to set up.
	 */
	public KnowledgeBaseConfiguration getKBConfig() {
		return _kbConfig;
	}

	/**
	 * The {@link ConnectionPool} to set up.
	 */
	public abstract ConnectionPool getConnectionPool();

}
