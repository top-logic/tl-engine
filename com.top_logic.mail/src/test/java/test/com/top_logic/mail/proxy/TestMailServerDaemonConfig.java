/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mail.proxy;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;
import test.com.top_logic.mail.base.TestingMailProcessor;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.mail.proxy.AbstractMailServerDaemon.UnknownMailStrategy;
import com.top_logic.mail.proxy.ConfiguredMailServerDaemon;

/**
 * Tests the defaults of the {@link ConfiguredMailServerDaemon.Config mail server daemon
 * configuration}.
 */
@SuppressWarnings("javadoc")
public class TestMailServerDaemonConfig extends AbstractTypedConfigurationTestCase {

	private static final String ROOT_TAG = "daemon";

	public void testUnknownMailsAreKeptByDefault() throws Exception {
		ConfiguredMailServerDaemon.Config config = read(daemon(""));

		assertEquals(UnknownMailStrategy.KEEP, config.getStrategyForUnknownMails());
	}

	public void testExplicitStrategy() throws Exception {
		ConfiguredMailServerDaemon.Config config = read(daemon(" strategy-for-unknown-mails=\"DELETE\""));

		assertEquals(UnknownMailStrategy.DELETE, config.getStrategyForUnknownMails());
	}

	private static String daemon(String attributes) {
		return "<" + ROOT_TAG + " name=\"daemon\" processor=\"" + TestingMailProcessor.class.getName() + "\""
			+ attributes + "/>";
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap(ROOT_TAG,
			TypedConfiguration.getConfigurationDescriptor(ConfiguredMailServerDaemon.Config.class));
	}

	public static Test suite() {
		return suite(TestMailServerDaemonConfig.class);
	}

}
