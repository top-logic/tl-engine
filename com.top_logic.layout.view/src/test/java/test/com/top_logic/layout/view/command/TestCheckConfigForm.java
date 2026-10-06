/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.configedit.ConfigControlService;
import com.top_logic.layout.configedit.ConfigFormControl;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.CheckConfigFormAction;
import com.top_logic.layout.view.command.ConfigFormScope;
import com.top_logic.layout.view.command.ConfigFormValid;
import com.top_logic.util.error.TopLogicException;

/**
 * Test for {@link CheckConfigFormAction} and {@link ConfigFormValid}, guarding a command that saves
 * what the configuration forms of its element edit, and for the {@link ConfigFormScope} they find
 * the forms in.
 */
@SuppressWarnings("javadoc")
public class TestCheckConfigForm extends TestCase {

	/** A configuration with a mandatory property. */
	public interface MandatoryConfig extends ConfigurationItem {

		/** Property name for {@link #getName()}. */
		String NAME = "name";

		@Name(NAME)
		@Mandatory
		String getName();

		void setName(String value);
	}

	private ReactContext _reactContext;

	private ConfigFormScope _scope;

	private ViewContext _context;

	private MandatoryConfig _config;

	private ConfigFormControl _form;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_reactContext = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_scope = new ConfigFormScope(null);
		_context = new DefaultViewContext(_reactContext).withScope(ConfigFormScope.class, _scope);
		_config = TypedConfiguration.newConfigItem(MandatoryConfig.class);
		_form = new ConfigFormControl(_reactContext, _config, false);
		_scope.register(_form);
	}

	public void testCheckRefusesAnInvalidForm() {
		try {
			check();
			fail("A missing mandatory value must refuse the save.");
		} catch (TopLogicException ex) {
			// Expected.
		}
		assertTrue("The refusal puts the problem on display.", _form.hasVisibleErrors());
	}

	public void testCheckPassesAValidForm() {
		_config.setName("given");
		Object input = new Object();
		assertSame("The input is handed on to the saving action.", input, check(input));
	}

	public void testCheckWithoutFormsPasses() {
		_scope.unregister(_form);
		check();
	}

	public void testRuleDisablesWhileErrorsAreShown() {
		ConfigFormValid rule = rule();
		int[] revalidated = { 0 };
		Runnable stop = rule.observe(() -> revalidated[0]++);

		assertTrue("Errors not yet on display do not disable the command.", rule.isExecutable(null).isExecutable());

		try {
			check();
			fail("Expected a refusal.");
		} catch (TopLogicException ex) {
			// Expected.
		}
		assertFalse("The command is disabled while the problems are on screen.",
			rule.isExecutable(null).isExecutable());
		assertTrue("The command is told to re-evaluate.", revalidated[0] > 0);

		_config.setName("fixed");
		_form.checkForSave();
		assertTrue("Fixing the problems enables the command again.", rule.isExecutable(null).isExecutable());

		stop.run();
	}

	public void testRuleFollowsFormsRegisteredLater() {
		ConfigFormValid rule = rule();
		int[] revalidated = { 0 };
		rule.observe(() -> revalidated[0]++);

		MandatoryConfig other = TypedConfiguration.newConfigItem(MandatoryConfig.class);
		ConfigFormControl later = new ConfigFormControl(_reactContext, other, false);
		_scope.register(later);
		assertTrue("A form appearing is reported.", revalidated[0] > 0);

		later.checkForSave();
		assertFalse("Errors of the later form disable the command, too.", rule.isExecutable(null).isExecutable());
	}

	public void testScopeRegistersWithEnclosingScopes() {
		ConfigFormScope outer = new ConfigFormScope(null);
		ConfigFormScope inner = new ConfigFormScope(outer);

		inner.register(_form);
		assertTrue("A form in a panel is known to the window around it.", outer.getForms().contains(_form));

		inner.unregister(_form);
		assertFalse(outer.getForms().contains(_form));
	}

	private Object check() {
		return check(null);
	}

	private Object check(Object input) {
		CheckConfigFormAction action = new CheckConfigFormAction(null,
			TypedConfiguration.newConfigItem(CheckConfigFormAction.Config.class));
		return action.execute(_context, input);
	}

	private ConfigFormValid rule() {
		ConfigFormValid rule =
			new ConfigFormValid(null, TypedConfiguration.newConfigItem(ConfigFormValid.Config.class));
		rule.bind(_context);
		return rule;
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestCheckConfigForm.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE, ConfigControlService.Module.INSTANCE));
	}

}
