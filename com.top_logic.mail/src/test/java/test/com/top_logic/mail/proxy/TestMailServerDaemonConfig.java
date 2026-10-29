/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mail.proxy;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;
import test.com.top_logic.mail.base.TestingMailProcessor;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.mail.proxy.AbstractMailServerDaemon.UnknownMailStrategy;
import com.top_logic.mail.proxy.ConfiguredMailServerDaemon;
import com.top_logic.mail.proxy.InitialMails;

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

	public void testInitialMailsAreSkippedByDefault() throws Exception {
		ConfiguredMailServerDaemon.Config config = read(daemon(""));

		assertEquals(InitialMails.SKIP, config.getInitialMails());
	}

	public void testExplicitInitialMails() throws Exception {
		ConfiguredMailServerDaemon.Config config =
			read(daemon(" " + ConfiguredMailServerDaemon.Config.INITIAL_MAILS + "=\"PROCESS\""));

		assertEquals(InitialMails.PROCESS, config.getInitialMails());
	}

	public void testProcessAllMailsIsRejected() {
		initFailureTest();
		try {
			read(daemon(" process-all-mails=\"false\""));
			fail("Configuration with removed property must not be accepted.");
		} catch (ConfigurationException ex) {
			BasicTestCase.assertContains("process-all-mails", ex.getMessage());
		}
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
