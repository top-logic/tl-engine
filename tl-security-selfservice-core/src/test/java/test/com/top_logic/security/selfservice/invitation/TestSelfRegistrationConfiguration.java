/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.selfservice.invitation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.module.TypedRuntimeModule.ModuleConfiguration;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.security.selfservice.invitation.InvitationModule;
import com.top_logic.security.selfservice.invitation.SelfRegistration;
import com.top_logic.security.selfservice.invitation.SelfRegistrationMode;

/**
 * Test of the configuration of the {@link SelfRegistration} as an application reads it from the
 * configuration files of its modules.
 *
 * <p>
 * The test configuration <code>tl-security-selfservice-core.test.config.xml</code> configures
 * <code>&lt;self-registration&gt;</code> a second time, as an application does in its own
 * configuration. The options of the module configuration, read from the variables
 * {@link #MODE_VARIABLE} and {@link #CAPTCHA_VARIABLE}, must be kept then.
 * </p>
 */
public class TestSelfRegistrationConfiguration extends BasicTestCase {

	/** The variable defining the mode of the self-registration. */
	private static final String MODE_VARIABLE = "tl_self_registration";

	/** The variable switching the captcha of the self-registration. */
	private static final String CAPTCHA_VARIABLE = "tl_self_registration_captcha";

	/**
	 * Without variables, the registration is off and asks for a captcha once switched on.
	 */
	public void testDefaults() throws Exception {
		SelfRegistration.Config config = withVariables(null, null);
		assertEquals(SelfRegistrationMode.OFF, config.getMode());
		assertTrue(config.getCaptcha());
		assertOverlayKept(config);
	}

	/**
	 * The variables set mode and captcha, also when the application configures the
	 * self-registration a second time.
	 */
	public void testVariables() throws Exception {
		SelfRegistration.Config config = withVariables("approval", "false");
		assertEquals(SelfRegistrationMode.APPROVAL, config.getMode());
		assertFalse(config.getCaptcha());
		assertOverlayKept(config);
	}

	private static void assertOverlayKept(SelfRegistration.Config config) {
		assertNotNull("Texts of the module configuration lost.", config.getRegistrationText());
		assertTrue("Mail script of the test configuration not applied.",
			config.getRegistrationMail().toString().contains("testSelfServiceMail"));
	}

	/**
	 * Reads the configuration of the self-registration from the configuration files of the
	 * application with the given values of {@link #MODE_VARIABLE} and {@link #CAPTCHA_VARIABLE}.
	 */
	private static SelfRegistration.Config withVariables(String mode, String captcha) throws Exception {
		String modeBefore = System.getProperty(MODE_VARIABLE);
		String captchaBefore = System.getProperty(CAPTCHA_VARIABLE);
		setProperty(MODE_VARIABLE, mode);
		setProperty(CAPTCHA_VARIABLE, captcha);
		try {
			AliasManager.getInstance().reload();
			return readConfig();
		} finally {
			setProperty(MODE_VARIABLE, modeBefore);
			setProperty(CAPTCHA_VARIABLE, captchaBefore);
			AliasManager.getInstance().reload();
		}
	}

	private static void setProperty(String name, String value) {
		if (value == null) {
			System.clearProperty(name);
		} else {
			System.setProperty(name, value);
		}
	}

	private static SelfRegistration.Config readConfig() throws Exception {
		List<BinaryContent> files = XMLProperties.Module.INSTANCE.config().getTypedConfigs();
		Map<String, ConfigurationDescriptor> root = Collections.singletonMap(ApplicationConfig.ROOT_TAG,
			TypedConfiguration.getConfigurationDescriptor(ApplicationConfig.Config.class));
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestSelfRegistrationConfiguration.class);
		ApplicationConfig.Config application = (ApplicationConfig.Config) new ConfigurationReader(context, root)
			.setSources(files)
			.setVariableExpander(AliasManager.getInstance().getExpander())
			.read();
		context.checkErrors();
		ModuleConfiguration service = application.getServices().get(InvitationModule.class);
		assertNotNull("No configuration of " + InvitationModule.class.getName(), service);
		return ((InvitationModule.Config) service.getInstance()).getSelfRegistration();
	}

	/**
	 * The suite.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(
			ServiceTestSetup.createSetup(TestSelfRegistrationConfiguration.class,
				ThreadContextManager.Module.INSTANCE));
	}

}
