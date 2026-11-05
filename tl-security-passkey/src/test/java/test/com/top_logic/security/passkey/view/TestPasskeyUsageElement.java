/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.passkey.view;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.security.passkey.PasskeyTestHost;

import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.element.TextElement;
import com.top_logic.security.passkey.PasskeyService;
import com.top_logic.security.passkey.PasskeyUsage;
import com.top_logic.security.passkey.view.PasskeyUsageElement;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link PasskeyUsageElement}: its content is shown in the configured usage of the
 * passkeys and hidden in the other.
 */
public class TestPasskeyUsageElement extends BasicTestCase {

	private static final String CONTENT = "Content of the usage";

	private ViewContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PasskeyTestHost.useDefault();
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
	}

	@Override
	protected void tearDown() throws Exception {
		PasskeyTestHost.useDefault();
		super.tearDown();
	}

	/** In the usage {@link PasskeyUsage#LOGIN}, only the element for that usage shows its content. */
	public void testLogin() throws Exception {
		PasskeyTestHost.use(PasskeyUsage.LOGIN);

		assertShown(control(PasskeyUsage.LOGIN));
		assertHidden(control(PasskeyUsage.SECOND_FACTOR));
	}

	/**
	 * In the usage {@link PasskeyUsage#SECOND_FACTOR}, only the element for that usage shows its
	 * content.
	 */
	public void testSecondFactor() throws Exception {
		PasskeyTestHost.use(PasskeyUsage.SECOND_FACTOR);

		assertShown(control(PasskeyUsage.SECOND_FACTOR));
		assertHidden(control(PasskeyUsage.LOGIN));
	}

	private static void assertShown(IReactControl control) {
		assertTrue("The content stands in place of the element.", control instanceof ReactTextControl);
		ReactTextControl text = (ReactTextControl) control;
		assertEquals(CONTENT, text.getText());
		assertFalse("The content is shown.", text.isHidden());
	}

	private static void assertHidden(IReactControl control) {
		assertFalse("The content is not built.", control instanceof ReactTextControl);
		assertTrue("Nothing takes place in the container.", ((ReactControl) control).isHidden());
	}

	private IReactControl control(PasskeyUsage usage) {
		TextElement.Config text = TypedConfiguration.newConfigItem(TextElement.Config.class);
		text.update(text.descriptor().getProperty(TextElement.Config.LABEL), ResKey.text(CONTENT));

		PasskeyUsageElement.Config config = TypedConfiguration.newConfigItem(PasskeyUsageElement.Config.class);
		config.update(config.descriptor().getProperty(PasskeyUsageElement.Config.USAGE), usage);
		config.getChildren().add(text);

		PasskeyUsageElement element =
			(PasskeyUsageElement) new DefaultInstantiationContext(TestPasskeyUsageElement.class).getInstance(config);
		return element.createControl(_context);
	}

	/**
	 * The suite, with the services of the passkeys.
	 */
	public static Test suite() {
		PasskeyTestHost.init();
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestPasskeyUsageElement.class),
				ModelService.Module.INSTANCE,
				PersonManager.Module.INSTANCE,
				PasskeyService.Module.INSTANCE));
	}

}
