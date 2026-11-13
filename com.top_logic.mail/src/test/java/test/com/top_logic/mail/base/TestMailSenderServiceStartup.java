/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mail.base;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.mail.MailSenderService;
import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.tools.CollectingLogListener;

/**
 * Test of the activation of {@link MailSenderService} depending on its configured
 * {@link MailSenderService.Config#getFromAddress() system address}.
 */
public class TestMailSenderServiceStartup extends BasicTestCase {

	private static final List<String> MODIFIED_PROPERTIES = List.of(
		MailSenderService.Config.ACTIVATED,
		MailSenderService.Config.SERVER,
		MailSenderService.Config.LOOKUP_MX_RECORD,
		MailSenderService.Config.STARTUP_NOTIFICATION_RECEIVERS,
		MailSenderService.Config.FROM_ADDRESS);

	private Map<PropertyDescriptor, Object> _savedValues;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		MailSenderService.Config config = serviceConfig();
		_savedValues = new LinkedHashMap<>();
		for (String name : MODIFIED_PROPERTIES) {
			PropertyDescriptor property = config.descriptor().getProperty(name);
			_savedValues.put(property, config.value(property));
		}

		// Activation must not depend on a reachable SMTP server.
		set(config, MailSenderService.Config.ACTIVATED, true);
		set(config, MailSenderService.Config.SERVER, "localhost");
		set(config, MailSenderService.Config.LOOKUP_MX_RECORD, false);
		set(config, MailSenderService.Config.STARTUP_NOTIFICATION_RECEIVERS, Collections.emptyList());
	}

	@Override
	protected void tearDown() throws Exception {
		MailSenderService.Config config = serviceConfig();
		for (Entry<PropertyDescriptor, Object> entry : _savedValues.entrySet()) {
			config.update(entry.getKey(), entry.getValue());
		}
		ModuleUtil.INSTANCE.restart(MailSenderService.Module.INSTANCE);
		super.tearDown();
	}

	/**
	 * A system address with a top-level domain of more than five letters activates the service.
	 */
	public void testValidAddressWithLongTopLevelDomain() throws Exception {
		assertActivation("noreply@example.invalid", true);
	}

	/**
	 * A host name without domain is a valid system address, as a local installation sending to a
	 * mail sink uses it.
	 */
	public void testAddressWithoutDomain() throws Exception {
		assertActivation("noreply@localhost", true);
	}

	/**
	 * An address with a personal name is no valid system address.
	 */
	public void testAddressWithPersonalName() throws Exception {
		assertActivation("Max <max@example.com>", false);
	}

	private void assertActivation(String fromAddress, boolean expectActivated) throws Exception {
		set(serviceConfig(), MailSenderService.Config.FROM_ADDRESS, fromAddress);

		CollectingLogListener errors = new CollectingLogListener(Set.of(Level.ERROR), true);
		try {
			ModuleUtil.INSTANCE.restart(MailSenderService.Module.INSTANCE);
		} finally {
			errors.deactivate();
		}

		assertEquals("Activation with system address '" + fromAddress + "'.", expectActivated,
			MailSenderService.getInstance().isActivated());

		List<LogEntry> entries = errors.getLogEntries();
		if (expectActivated) {
			assertEquals("No error logged: " + LogEntry.joinMessages(entries, ", "), 0, entries.size());
		} else {
			assertEquals("The invalid system address is reported.", 1, entries.size());
			assertTrue("The report names the configured address: " + entries.get(0).getMessage(),
				entries.get(0).getMessage().contains("'" + fromAddress + "'"));
		}
	}

	private static void set(MailSenderService.Config config, String name, Object value) {
		config.update(config.descriptor().getProperty(name), value);
	}

	private static MailSenderService.Config serviceConfig() throws Exception {
		return (MailSenderService.Config) ApplicationConfig.getInstance()
			.getServiceConfiguration(MailSenderService.class);
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestMailSenderServiceStartup.class, MailSenderService.Module.INSTANCE));
	}

}
