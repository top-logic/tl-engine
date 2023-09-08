/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.proxy;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Service managing a configured {@link MailReceiver}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MailReceiverService extends ManagedClass {

	/**
	 * Configuration of the {@link MailReceiverService}.
	 */
	public interface Config extends ServiceConfiguration<MailReceiverService>, MailReceiver.ServerConfig {
		// Pure sum interface.
	}

	private final boolean _activated;

	private MailReceiver _server;

	/**
	 * Creates a new {@link MailReceiverService} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link MailReceiverService}.
	 */
	public MailReceiverService(InstantiationContext context, Config config) {
		_activated = config.isActivated();
		if (_activated) {
			_server = new MailReceiver(config);
		}
	}

	/**
	 * The default {@link MailReceiver}, or <code>null</code>, if not activated.
	 */
	public MailReceiver getServerInstance() {
		return _server;
	}

	@Override
	protected void startUp() {
		super.startUp();
		if (_server != null) {
			_server.login();
		}
	}

	@Override
	protected void shutDown() {
		if (_server != null) {
			_server.disconnect();
			_server = null;
		}
		super.shutDown();
	}

	/**
	 * The configured mail receiver.
	 */
	public static MailReceiver getMailReceiverInstance() {
		return getService().getServerInstance();
	}

	/**
	 * The service singleton.
	 */
	public static MailReceiverService getService() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton {@link BasicRuntimeModule} for {@link MailReceiverService}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<MailReceiverService> {

		/** Singleton {@link Module}. */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<MailReceiverService> getImplementation() {
			return MailReceiverService.class;
		}

	}

}

