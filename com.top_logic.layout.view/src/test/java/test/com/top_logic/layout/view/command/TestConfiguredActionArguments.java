/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.command.ActionArguments;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.WithTransactionAction;

/**
 * Tests running configured action chains with named arguments: the follow-up actions of an action
 * with a result, and the actions of a {@link WithTransactionAction}, which run with the arguments of
 * the enclosing chain and are instantiated in its scope.
 */
public class TestConfiguredActionArguments extends AbstractModelAccessTest {

	/** Name of the input channel of the command under test. */
	private static final String CHANNEL = "project";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ArgumentsProbe.reset();
		becomeUser(_root);
	}

	@Override
	protected void tearDown() throws Exception {
		ArgumentsProbe.reset();
		super.tearDown();
	}

	/**
	 * Tests that an action inside a transaction sees the unchanged arguments of the command, also
	 * after an action with a result.
	 */
	public void testTransactionPassesArguments() throws Exception {
		GenericViewCommand command = command(
			"<generic-command input='" + CHANNEL + "'>"
				+ "<with-transaction>"
				+ "<execute-script function=\"x -> 'other'\"/>"
				+ "<action class='" + ArgumentsProbe.class.getName() + "'/>"
				+ "</with-transaction>"
				+ "</generic-command>");

		assertEquals(List.of(CHANNEL), ArgumentsProbe.SCOPES.get(0).names());

		command.execute(new DefaultViewContext(null), _project);

		ActionArguments seen = ArgumentsProbe.SEEN.get(0);
		assertEquals(List.of(CHANNEL), seen.names());
		assertSame(_project, seen.value());
	}

	/**
	 * Tests that the configured follow-up actions and the selected value take effect when the
	 * command runs.
	 */
	public void testRunConfiguredFollowUps() throws Exception {
		GenericViewCommand command = command(
			"<generic-command>"
				+ "<execute-script function='x -> $x + 1' as='next'>"
				+ "<action class='" + ArgumentsProbe.class.getName() + "'/>"
				+ "</execute-script>"
				+ "<action class='" + ArgumentsProbe.class.getName() + "'/>"
				+ "</generic-command>");

		command.execute(new DefaultViewContext(null), 1);

		assertEquals(2, ArgumentsProbe.SEEN.size());
		ActionArguments inner = ArgumentsProbe.SEEN.get(0);
		assertEquals(List.of(GenericViewCommand.DEFAULT_ARGUMENT, "next"), inner.names());
		assertEquals(1, ((Number) inner.get(GenericViewCommand.DEFAULT_ARGUMENT)).intValue());
		assertEquals(2, ((Number) inner.get("next")).intValue());
		ActionArguments outer = ArgumentsProbe.SEEN.get(1);
		assertEquals(List.of(GenericViewCommand.DEFAULT_ARGUMENT), outer.names());
	}

	private static GenericViewCommand command(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestConfiguredActionArguments.class);
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap("generic-command",
			TypedConfiguration.getConfigurationDescriptor(GenericViewCommand.Config.class)));
		reader.setSource(CharacterContents.newContent(xml));
		GenericViewCommand.Config config = (GenericViewCommand.Config) reader.read();
		GenericViewCommand command = (GenericViewCommand) context.getInstance(config);
		context.checkErrors();
		return command;
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestConfiguredActionArguments.class);
	}
}
