/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewCommands;
import com.top_logic.layout.view.element.HtmlElement;
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

	/** Name of a channel of the view the commands under test run in. */
	static final String SELECTION = "selection";

	/** Name of a further channel of the view the commands under test run in. */
	static final String OTHER = "other";

	/** An action recording the arguments it runs with. */
	static final String PROBE = "<action class='" + ArgumentsProbe.class.getName() + "'/>";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ArgumentsProbe.reset();
	}

	@Override
	protected Person initialUser() {
		return _root;
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
			"<generic-command inputs='" + CHANNEL + "'>"
				+ "<with-transaction>"
				+ "<execute-script function=\"'other'\"/>"
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
				+ "<execute-script function='$input + 1'><with-result as='next'>"
				+ "<action class='" + ArgumentsProbe.class.getName() + "'/>"
				+ "</with-result></execute-script>"
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

	/**
	 * Tests that the scripts of a chain - of an action, a condition, a switch and its cases, a
	 * confirmation - use the arguments of the command as variables without declaring them, the
	 * inputs of the command renamed with {@code as}.
	 */
	public void testScriptsUseNamedArguments() throws Exception {
		DefaultViewContext context = context(Map.of(SELECTION, "t", OTHER, "o"));
		GenericViewCommand command = command(
			"<generic-command inputs='" + SELECTION + " as ticket, " + OTHER + "'>"
				+ "<confirm expr=\"if($ticket == 't', null, 'Really?')\"/>"
				+ "<execute-script function=\"$ticket + '-' + $other\"><with-result as='joined'>"
				+ PROBE
				+ "</with-result></execute-script>"
				+ "<if test=\"$ticket == 't'\"><then><with-transaction>" + PROBE + "</with-transaction></then></if>"
				+ "<switch value='$other' as='current'>"
				+ "<case test=\"$current == 'o' &amp;&amp; $ticket == 't'\">" + PROBE + "</case>"
				+ "</switch>"
				+ "</generic-command>");

		command.execute(context, "t");

		assertEquals(3, ArgumentsProbe.SEEN.size());
		ActionArguments joined = ArgumentsProbe.SEEN.get(0);
		assertEquals(List.of("ticket", OTHER, "joined"), joined.names());
		assertEquals("t-o", joined.get("joined"));
		assertEquals(List.of("ticket", OTHER), ArgumentsProbe.SEEN.get(1).names());
		assertEquals("t", ArgumentsProbe.SEEN.get(2).value());
	}

	/**
	 * Tests that the inputs of an action are variables of its script, named after their channel or
	 * the given name.
	 */
	public void testActionInputsAreVariables() throws Exception {
		DefaultViewContext context = context(Map.of(OTHER, "o"));
		GenericViewCommand command = command(
			"<generic-command>"
				+ "<execute-script function=\"$extra + $" + OTHER + "\" inputs='" + OTHER + " as extra, "
				+ OTHER + "'>"
				+ "<with-result>" + PROBE + "</with-result>"
				+ "</execute-script>"
				+ "</generic-command>");

		command.execute(context, null);

		assertEquals("oo", ArgumentsProbe.SEEN.get(0).get("result"));
	}

	/**
	 * Tests that the first input of a command is its main argument, the input its executability
	 * rules decide over.
	 */
	public void testFirstInputDrivesExecutability() throws Exception {
		DefaultViewContext context = context(Map.of(OTHER, "o"));
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestConfiguredActionArguments.class);
		GenericViewCommand.Config config = parse(instantiation,
			"<generic-command inputs='" + SELECTION + ", " + OTHER + "'>"
				+ "<executability><null-input-disabled/></executability>"
				+ PROBE
				+ "</generic-command>");
		GenericViewCommand command = (GenericViewCommand) instantiation.getInstance(config);
		instantiation.checkErrors();
		ViewCommandModel model = ViewCommandModel.forCommand(context, command, config);

		assertFalse("The first input is empty.", model.executability(model.resolveInput()).isExecutable());

		context.resolveChannel(new ChannelRef(SELECTION)).set("s");
		assertTrue(model.executability(model.resolveInput()).isExecutable());

		model.perform(context);
		ActionArguments seen = ArgumentsProbe.SEEN.get(0);
		assertEquals(SELECTION, seen.defaultName());
		assertEquals("s", seen.value());
		assertEquals("o", seen.get(OTHER));
	}

	/**
	 * Tests that the value a trigger supplies is the main argument under the name configured at the
	 * trigger, or under the default name of the trigger.
	 */
	public void testTriggerValueName() throws Exception {
		assertTriggerName("", HtmlElement.Config.DEFAULT_LINK_AS);
		assertTriggerName(" " + HtmlElement.Config.LINK_AS + "='target'", "target");
	}

	private void assertTriggerName(String nameAttribute, String expectedName) throws Exception {
		ArgumentsProbe.reset();
		DefaultViewContext context = context(Map.of(OTHER, "o"));
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestConfiguredActionArguments.class);
		ConfigurationReader reader = new ConfigurationReader(instantiation,
			Collections.singletonMap("html", TypedConfiguration.getConfigurationDescriptor(HtmlElement.Config.class)));
		reader.setSource(CharacterContents.newContent(
			"<html input='" + OTHER + "'" + nameAttribute + ">"
				+ "<on-link class='" + GenericViewCommand.class.getName() + "' inputs='" + OTHER + "'>"
				+ "<execute-script function='$" + expectedName + "'>"
				+ "<with-result as='seen'>" + PROBE + "</with-result>"
				+ "</execute-script>"
				+ "</on-link>"
				+ "</html>"));
		HtmlElement.Config config = (HtmlElement.Config) reader.read();
		instantiation.checkErrors();

		ViewCommand command = ViewCommands.instantiate(instantiation, config.getOnLink(), config.getOnLinkAs());
		instantiation.checkErrors();
		ViewCommandModel model = ViewCommandModel.forCommand(context, command,
			(ViewCommand.Config) config.getOnLink(), config.getOnLinkAs());

		model.execute(context, "followed");

		ActionArguments seen = ArgumentsProbe.SEEN.get(0);
		assertEquals(List.of(OTHER, expectedName, "seen"), seen.names());
		assertEquals("followed", seen.get("seen"));
		assertEquals("o", seen.get(OTHER));
	}

	/**
	 * Tests that a write of a channel with {@code clear} empties the channel.
	 */
	public void testWriteChannelClear() throws Exception {
		DefaultViewContext context = context(Map.of(SELECTION, "s"));
		GenericViewCommand command = command(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<write-channel name='" + SELECTION + "' clear='true'/>"
				+ "</generic-command>");

		command.execute(context, "s");

		assertNull(context.resolveChannel(new ChannelRef(SELECTION)).get());
	}

	/**
	 * Tests that a write of a channel cannot both clear the channel and write an argument.
	 */
	public void testWriteChannelClearWithValueIsError() throws Exception {
		assertInstantiationError(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<write-channel name='" + SELECTION + "' clear='true' value='" + SELECTION + "'/>"
				+ "</generic-command>");
	}

	/**
	 * Tests that a script using a variable that is not defined at its position is a configuration
	 * error when it is instantiated.
	 */
	public void testUnknownVariableIsError() throws Exception {
		assertInstantiationError(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<execute-script function='$unknown'/>"
				+ "</generic-command>");
		assertInstantiationError(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<execute-script function='$" + SELECTION + "'><with-result as='first'/></execute-script>"
				+ "<execute-script function='$first'/>"
				+ "</generic-command>");
	}

	static void assertInstantiationError(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestConfiguredActionArguments.class);
		GenericViewCommand.Config config = parse(context, xml);
		context.getInstance(config);
		assertTrue("Instantiating is refused: " + xml, context.hasErrors());
	}

	static DefaultViewContext context(Map<String, Object> values) {
		DefaultViewContext context = new DefaultViewContext(null);
		for (String name : List.of(SELECTION, OTHER)) {
			DefaultViewChannel channel = new DefaultViewChannel(name);
			channel.set(values.get(name));
			context.registerChannel(name, channel);
		}
		return context;
	}

	static GenericViewCommand.Config parse(DefaultInstantiationContext context, String xml)
			throws ConfigurationException {
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap("generic-command",
			TypedConfiguration.getConfigurationDescriptor(GenericViewCommand.Config.class)));
		reader.setSource(CharacterContents.newContent(xml));
		GenericViewCommand.Config config = (GenericViewCommand.Config) reader.read();
		context.checkErrors();
		return config;
	}

	static GenericViewCommand command(String xml) throws ConfigurationException {
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
